import { Component, Inject, Vue } from 'vue-property-decorator';
import { VERSION } from '@/constants';
import LoginService from '@/account/login.service';
import AccountService from '@/account/account.service';
import TranslationService from '@/locale/translation.service';
import axios from 'axios';
import { IUIConfigView } from '@/shared/model/transfer-object.model';

const REQUESTED_URL_KEY = 'requested-url';
const CA3S_JWT_COOKIE_NAME = 'ca3sJWT';

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
  public ssoProvider: any = [];
  public uiConfig: IUIConfigView = {};

  public mounted(): void {
    window.document.cookie = 'instantLogin';
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('showNavBar') && urlParams.get('showNavBar') === 'false') {
      this.showNavBar = false;
    }

    this.instantLogin = !(urlParams.has('instantLogin') && urlParams.get('instantLogin') === 'false');

    window.console.info('requested-url: ' + sessionStorage.getItem(REQUESTED_URL_KEY));
    if (sessionStorage.getItem(REQUESTED_URL_KEY)) {
      this.$router.replace(sessionStorage.getItem(REQUESTED_URL_KEY));
      //        sessionStorage.removeItem(REQUESTED_URL_KEY);
    }
  }

  async created() {
    if (this.$cookie.get(CA3S_JWT_COOKIE_NAME)) {
      const jwt = this.$cookie.get(CA3S_JWT_COOKIE_NAME);
      window.console.info('$cookie ca3sJWT present : ' + jwt);
      localStorage.setItem('jhi-authenticationToken', '' + jwt);
      this.accountService().retrieveAccount();
      //      this.$cookie.delete(this._ca3sJWT);
    }

    const self = this;
    axios({
      method: 'get',
      url: 'api/ui/config',
      responseType: 'stream',
    }).then(function (response) {
      window.console.info('ui/config returns ' + response.data);
      self.uiConfig = response.data;
      self.$store.commit('updateCV', self.uiConfig);
      self.ssoProvider = self.uiConfig.ssoProvider;

      if (self.uiConfig.appName) {
        document.title = self.uiConfig.appName;
      }
      window.console.info('self.authenticated: ' + self.authenticated + ', self.instantLogin ' + self.instantLogin);

      if (!self.authenticated) {
        if (self.instantLogin) {
          if (self.$store.state.uiConfigStore.config.autoSSOLogin) {
            self._doSSOLogin(self.$store.state.uiConfigStore.config.ssoProvider[0]);
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
      if (res.data.languageArr) {
        this.languages['multiLanguage'] = res.data.languageArr.length > 1;
      } else {
        this.languages['multiLanguage'] = false;
      }
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
    this.doSSOLogout();

    this.$router.push('/');
  }

  public openLogin(): void {
    this.loginService().openLogin((<any>this).$root);
  }

  public doSSOLogin() {
    this._doSSOLogin(this.$store.state.uiConfigStore.config.ssoProvider[0]);
  }
  public _doSSOLogin(ssoProviderNameMixedCase: string) {
    if (!ssoProviderNameMixedCase) {
      window.console.info('undefined SSO provider name at SSOlogin');
      return;
    }

    let ssoProviderName = ssoProviderNameMixedCase.toLowerCase();
    window.console.info('forwarding to SSO Login: ' + ssoProviderName);

    let ssoReturnUrl = window.location.pathname + window.location.search;
    window.console.info('setting requested-url to window.location.pathname: ' + ssoReturnUrl);
    sessionStorage.setItem(REQUESTED_URL_KEY, ssoReturnUrl);

    if (ssoProviderName === 'oidc') {
      this.doOIDCLogin();
    } else if (ssoProviderName === 'keycloak') {
      this.doOIDCLogin();
    } else if (ssoProviderName === 'saml') {
      this.doSAMLLogin();
    } else {
      window.console.info('unexpected SSO provider name : ' + ssoProviderName);
    }
  }

  public doSSOLogout() {
    this._doSSOLogout(this.$store.state.uiConfigStore.config.ssoProvider[0]);
  }
  public _doSSOLogout(ssoProviderNameMixedCase: string) {
    if (!ssoProviderNameMixedCase) {
      window.console.info('undefined SSO provider name at SSOlogout');
      return;
    }

    let ssoProviderName = ssoProviderNameMixedCase.toLowerCase();
    window.console.info('forwarding to SSO Login: ' + ssoProviderName);

    if (ssoProviderName === 'oidc') {
      this.doOIDCLogout();
    } else if (ssoProviderName === 'keycloak') {
      this.doOIDCLogout();
    } else if (ssoProviderName === 'saml') {
      this.doSAMLLogout();
    } else {
      window.console.info('unexpected SSO provider name : ' + ssoProviderName);
    }
  }

  public doOIDCLogin(): void {
    const uri = window.location.href;
    window.console.info('JhiNavbar ### window.location : ' + uri);

    axios
      .get('oidc/authenticate', {
        params: {
          //          initialUri: uri
          initialUri: '',
        },
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
  public doSAMLLogin(): void {
    let target = '/saml/login';
    const samlEntityBaseUrl: string = this.$store.state.uiConfigStore.config.samlEntityBaseUrl;

    if (samlEntityBaseUrl && samlEntityBaseUrl.trim().length > 0) {
      target = samlEntityBaseUrl.trim() + '/saml/login';
    }
    window.console.info('forwarding to SAML authentication url ' + target);
    window.location.href = target;
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

  public doSAMLLogout(): void {
    axios
      .post('/saml/logout')
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

  public hasTenant(): boolean {
    if (this.$store.getters.account === undefined) {
      return false;
    }

    if (!this.$store.getters.account.tenantName) {
      return false;
    }

    if (this.$store.getters.account.tenantName === undefined) {
      return false;
    }

    if (this.$store.getters.account.tenantName.length === 0) {
      return false;
    }
    return true;
  }
  public get tenant(): string {
    if (this.hasTenant()) {
      window.console.warn('hasTenant ' + this.$store.getters.account.tenantName);
      return this.$store.getters.account.tenantName;
    }
    window.console.warn('hasTenant: false');
    return '';
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
