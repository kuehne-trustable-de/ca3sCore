import { Component, Inject, Vue } from 'vue-property-decorator';
import { VERSION } from '@/constants';
import LoginService from '@/account/login.service';
import AccountService from '@/account/account.service';
import TranslationService from '@/locale/translation.service';
import axios from 'axios';
import { IUIConfigView } from '@/shared/model/transfer-object.model';

@Component
export default class JhiNavbar extends Vue {
  @Inject('loginService')
  private loginService: () => LoginService;
  @Inject('translationService') private translationService: () => TranslationService;

  @Inject('accountService') private accountService: () => AccountService;

  public version = VERSION ? 'v' + VERSION : '';
  private currentLanguage = this.$store.getters.currentLanguage;
  public languages: any = this.$store.getters.languages;

  public showNavBar = true;
  public instantLogin = true;

  public mounted(): void {
    window.document.cookie = 'instantLogin';
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('showNavBar') && urlParams.get('showNavBar') === 'false') {
      this.showNavBar = false;
    }

    this.instantLogin = !(urlParams.has('instantLogin') && urlParams.get('instantLogin') === 'false');
  }

  async created() {
    const self = this;
    axios({
      method: 'get',
      url: 'api/ui/config',
      responseType: 'stream'
    }).then(function(response) {
      window.console.info('ui/config returns ' + response.data);
      const uiConfig: IUIConfigView = response.data;
      self.$store.commit('updateCV', uiConfig);

      window.console.info('self.authenticated: ' + self.authenticated + ', self.instantLogin ' + self.instantLogin);

      if (!self.authenticated) {
        if (self.instantLogin) {
          if (self.$store.state.uiConfigStore.config.autoSSOLogin) {
            window.console.info('forwarding to SSO Login ');
            self.doOIDCLogin();
          }
        } else {
          window.console.info('logged out recently, no automatic forward to SSO Login ');
        }
      }
    });

    const res = await this.translationService().refreshLanguages();

    if (res.data) {
      this.languages = {};
      for (const lang of res.data.languageArr) {
        window.console.log('adding available language "' + lang + '" ...');
        if (lang === 'en') {
          this.languages['en'] = { name: 'English' };
        } else if (lang === 'de') {
          this.languages['de'] = { name: 'Deutsch' };
        } else if (lang === 'pl') {
          this.languages['pl'] = { name: 'Polski' };
        } else {
          window.console.warn('unexpected language "' + lang + '" found');
        }
      }
      this.languages['multiLanguage'] = res.data.languageArr.length > 1;
    }

    this.translationService().refreshTranslation(this.currentLanguage);
  }

  public subIsActive(input) {
    const paths = Array.isArray(input) ? input : [input];
    return paths.some(path => {
      return this.$route.path.indexOf(path) === 0; // current path starts with this path string
    });
  }

  public changeLanguage(newLanguage: string): void {
    this.translationService().refreshTranslation(newLanguage);
  }

  public isActiveLanguage(key: string): boolean {
    return key === this.$store.getters.currentLanguage;
  }

  public logout(): void {
    localStorage.removeItem('jhi-authenticationToken');
    sessionStorage.removeItem('jhi-authenticationToken');
    this.$store.commit('logout');
    this.doOIDCLogout();

    this.$router.push('/');
  }

  public openLogin(): void {
    this.loginService().openLogin((<any>this).$root);
  }

  public doOIDCLogin(): void {
    const uri = window.location.href;
    window.console.info('JhiNavbar ### window.location : ' + uri);

    axios
      .get('oidc/authenticate', {
        params: {
          initialUri: uri
        }
      })
      .then(result => {
        const location = result.headers.location;
        if (location) {
          window.console.info('forwarding to OIDC authentication url.');
          window.location.href = location;
        }

        const bearerToken = result.headers.authorization;
        if (bearerToken && bearerToken.slice(0, 7) === 'Bearer ') {
          const jwt = bearerToken.slice(7, bearerToken.length);
          localStorage.setItem('jhi-authenticationToken', jwt);
        }
      })
      .catch(() => {
        window.console.warn('problem doing OIDC authentication.');
      });
  }
  public doOIDCLogout(): void {
    axios
      .post('oidc/logout')
      .then(result => {
        const location = result.headers.location;
        if (location) {
          window.console.info('forwarding to OIDC logout url.');
          window.location.href = location;
        }
      })
      .catch(() => {
        window.console.warn('problem doing OIDC logout.');
      });
  }

  public get authenticated(): boolean {
    if (!this.$store.getters.authenticated) {
      return false;
    }
    if (this.username === 'anonymous') {
      return false;
    }
    return this.$store.getters.authenticated;
  }

  public get headerColor(): string {
    window.console.warn('headerColor: #00ffff');
    return '#8fffff';
  }

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public get roles(): string {
    let roles = '';
    if (this.$store.getters.account) {
      for (const role of this.$store.getters.account.authorities) {
        if (roles.length > 0) {
          roles += ', ';
        }
        roles += this.$t(role);
      }
    }
    return roles;
  }

  public hasAnyAuthority(authorities: any): boolean {
    return this.accountService().hasAnyAuthority(authorities);
  }

  public get swaggerEnabled(): boolean {
    return this.$store.getters.activeProfiles.indexOf('swagger') > -1;
  }

  public get inProduction(): boolean {
    return this.$store.getters.activeProfiles.indexOf('prod') > -1;
  }
}
