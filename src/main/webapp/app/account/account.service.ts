import axios from 'axios';
import { Store } from 'vuex';
import VueRouter from 'vue-router';
import TranslationService from '@/locale/translation.service';

import TrackerService from '@/admin/tracker/tracker.service';

export default class AccountService {
  constructor(
    private store: Store<any>,
    private translationService: TranslationService,
    private trackerService: TrackerService,
    private router: VueRouter
  ) {
    this.init();
  }

  public init(): void {
    window.console.info('++++++++ AccountService.init : ');

    this.router.beforeEach((to, from, next) => {
      const bearer = to.query.bearer;
      if (bearer) {
        window.console.info('setting bearer token to local storage : ' + bearer);
        localStorage.setItem('jhi-authenticationToken', '' + bearer);
        this.retrieveAccount();
      } else {
        const fragmentParts = to.hash.split('&');
        let access_token = null;

        for (let i = 0; i < fragmentParts.length; i++) {
          const fragmentPartArray = fragmentParts[i].split('=');

          if (fragmentPartArray.length > 1) {
            if ('access_token' === fragmentPartArray[0]) {
              access_token = fragmentPartArray[1];
            }
          }
        }
        if (access_token) {
          window.console.info('++++++++++++++++++ access_token : ' + access_token);
          this.forwardToken(access_token);
        }
      }

      next();
    });
    /*
    const token = this.$route.query.bearer;
    if (token) {
      window.console.info('setting bearer token to local storage : ' + token);
      localStorage.setItem('jhi-authenticationToken', token);
      this.AccountService().retrieveAccount();
    }
*/
    const token = localStorage.getItem('jhi-authenticationToken') || sessionStorage.getItem('jhi-authenticationToken');
    if (!this.store.getters.account && !this.store.getters.logon && token) {
      this.retrieveAccount();
    }
    this.retrieveProfiles();
  }

  public retrieveProfiles(): void {
    axios.get('actuator/info').then(res => {
      if (res.data && res.data.activeProfiles) {
        this.store.commit('setRibbonOnProfiles', res.data['display-ribbon-on-profiles']);
        this.store.commit('setActiveProfiles', res.data['activeProfiles']);
      }
    });
  }

  public forwardToken(token: string): void {
    axios
      .get('oidc/tokenImplicit', { params: { access_token: token } })
      .then(response => {
        //        const account = response.data;
        window.console.info('token accepted');
      })
      .catch(() => {
        window.console.info('token rejected');
      });
  }

  public retrieveAccount(): void {
    this.store.commit('authenticate');
    axios
      .get('api/account')
      .then(response => {
        const account = response.data;
        if (account) {
          this.store.commit('authenticated', account);
          if (this.store.getters.currentLanguage !== account.langKey) {
            this.store.commit('currentLanguage', account.langKey);
          }
          window.console.info('requested-url: ' + sessionStorage.getItem('requested-url'));
          if (sessionStorage.getItem('requested-url')) {
            this.router.replace(sessionStorage.getItem('requested-url'));
            sessionStorage.removeItem('requested-url');
          }
          this.trackerService.connect();
        } else {
          this.store.commit('logout');
          sessionStorage.removeItem('requested-url');

          if (this.store.state.uiConfigStore.config.autoSSOLogin) {
            window.console.info('forwading to SSO Login ');
            this.router.push('/');
          } else {
            this.router.push('/');
          }
        }
        this.translationService.refreshTranslation(this.store.getters.currentLanguage);
      })
      .catch(() => {
        localStorage.removeItem('jhi-authenticationToken');
        sessionStorage.removeItem('jhi-authenticationToken');
        this.store.commit('logout');
        this.router.push('/');
      });
  }

  public hasAnyAuthority(authorities: any): boolean {
    if (typeof authorities === 'string') {
      authorities = [authorities];
    }
    if (!this.authenticated || !this.userAuthorities) {
      return false;
    }

    for (let i = 0; i < authorities.length; i++) {
      if (this.userAuthorities.includes(authorities[i])) {
        return true;
      }
    }

    return false;
  }

  public get authenticated(): boolean {
    return this.store.getters.authenticated;
  }

  public get userAuthorities(): any {
    return this.store.getters.account.authorities;
  }
}
