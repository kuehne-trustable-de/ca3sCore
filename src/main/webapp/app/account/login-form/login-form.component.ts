import axios, { AxiosError } from 'axios';
import Component from 'vue-class-component';
import { Vue, Inject } from 'vue-property-decorator';
import AccountService from '@/account/account.service';
import { IPreferences, ILoginData, IAuthSecondFactor } from '@/shared/model/transfer-object.model';

const STORAGE_SECONDFACTOR = '2ndFactor';
const STORAGE_LOGIN_MODE = 'loginMode';
const STORAGE_LOGIN_DOMAIN = 'loginDomain';

@Component({
  watch: {
    $route() {
      this.$root.$emit('bv::hide::modal', 'login-page');
    },
  },
})
export default class LoginForm extends Vue {
  totpRegEx: string = '^([0-9]{6})$';

  @Inject('accountService')
  private accountService: () => AccountService;
  public authenticationError = null;
  public spnegoAuthenticationError = false;

  public loginData: ILoginData = { authSecondFactor: 'NONE' };

  public isBlocked = false;
  public isNoClientCertificate = false;
  public validatingClientCertificate = false;
  public sendingSMS = false;
  public isSmsSent = false;
  public blockedUntil = '';

  public loginMode = '';
  public loginDomain = '';

  public preferences: IPreferences = {};

  public updateCounter = 1;

  async mounted() {
    const authSecondFactorString = localStorage.getItem(STORAGE_SECONDFACTOR) || 'NONE';
    this.loginMode = localStorage.getItem(STORAGE_LOGIN_MODE) || 'password';
    this.loginDomain = localStorage.getItem(STORAGE_LOGIN_DOMAIN) || '';
    this.loginDomain = this.loginDomain.toLowerCase();
    window.console.info('loginMode: ' + this.loginMode + ' at domain ' + this.loginDomain);

    this.loginData.authSecondFactor = authSecondFactorString as IAuthSecondFactor;

    if (this.loginMode === 'spnego') {
      this.doSpnegoLogin();
    }
    if (this.loginMode === 'ldap') {
      this.loginData.authSecondFactor = 'NONE';
      this.loginData.username = this.loginDomain + '\\';
    }
    window.console.info('local storage: ' + STORAGE_SECONDFACTOR + ' : ' + this.loginData.authSecondFactor);
  }

  public notifyChange(_evt: Event): void {
    this.updateCounter++;
  }
  public retrievePreference(): void {
    axios
      .get('/api/preference/1')
      .then(result => {
        if (result && result.data) {
          this.preferences = result.data;
        }
      })
      .catch(error => {
        // Handle the error response
        console.error('----' + error);
        this.loginData.password = '';
        if (error && error.response && error.response.data) {
          const data = error.response.data;
          if (data.type === 'urn:ietf:params:trustable:error:userBlocked') {
            this.isBlocked = true;
            const dateObj = new Date(data.detail);
            this.blockedUntil = dateObj.toLocaleDateString() + ', ' + dateObj.toLocaleTimeString();
          } else {
            this.authenticationError = true;
          }
        }
      });
  }

  public showTOTPExpFieldWarning(value: string): boolean {
    const regexp = new RegExp(this.totpRegEx);
    const valid = regexp.test(value);
    console.log('showTOTPExpFieldWarning( ' + this.totpRegEx + ', "' + value + '") -> ' + valid);
    return !valid;
  }

  public showUsernameDomainWarning(): boolean {
    if (this.loginMode !== 'ldap') {
      return false;
    }

    if (!this.loginData.username) {
      return false;
    }

    console.log(
      'showUsernameDomainWarning( ' +
        this.loginData.username.toLowerCase() +
        ' : "' +
        !this.loginData.username.toLowerCase().startsWith(this.loginDomain + '\\') +
        '")'
    );
    return !this.loginData.username.toLowerCase().startsWith(this.loginDomain + '\\');
  }
  public showUsernameWarning(): boolean {
    if (!this.loginData.username || this.loginData.username.trim().length < 1) {
      return true;
    }
    return false;
  }
  public showPasswordWarning(): boolean {
    if (!this.loginData.password || this.loginData.password.trim().length < 4) {
      return true;
    }
    return false;
  }
  public showSecondFactorWarning(): boolean {
    if (
      ('TOTP' === this.loginData.authSecondFactor ||
        'EMAIL' === this.loginData.authSecondFactor ||
        'SMS' === this.loginData.authSecondFactor) &&
      (!this.loginData.secondSecret || this.loginData.secondSecret.length < 6)
    ) {
      console.info('loginData.secondSecret too short / empty');
      return true;
    }

    return false;
  }
  public loginEnabled(): boolean {
    if (this.showUsernameWarning()) {
      return false;
    }

    if (this.showUsernameDomainWarning()) {
      return false;
    }

    if (this.showPasswordWarning()) {
      return false;
    }

    if (this.showSecondFactorWarning()) {
      return false;
    }

    return true;
  }

  public doLogin(): void {
    if (this.loginData.authSecondFactor === 'CLIENT_CERT') {
      this.requestClientCert();
    } else {
      this.authenticateCredentials();
    }
  }

  public authenticateCredentials(): void {
    this.isBlocked = false;
    this.isNoClientCertificate = false;

    const target: string = this.loginMode === 'ldap' ? 'api/authenticateLDAP' : 'api/authenticate';
    const self = this;

    axios
      .post(target, this.loginData)
      .then(result => {
        if (result && result.headers) {
          const bearerToken = result.headers.authorization;
          if (bearerToken && bearerToken.slice(0, 7) === 'Bearer ') {
            const jwt = bearerToken.slice(7, bearerToken.length);
            if (self.loginData.rememberMe) {
              localStorage.setItem('jhi-authenticationToken', jwt);
              sessionStorage.removeItem('jhi-authenticationToken');
            } else {
              sessionStorage.setItem('jhi-authenticationToken', jwt);
              localStorage.removeItem('jhi-authenticationToken');
            }
          }
          self.authenticationError = false;
          self.$root.$emit('bv::hide::modal', 'login-page');
          self.accountService().retrieveAccount();

          localStorage.setItem(STORAGE_SECONDFACTOR, self.loginData.authSecondFactor);
        }
      })
      .catch(error => {
        // Handle the error response
        console.error('----' + error);
        self.loginData.password = '';
        self.loginData.secondSecret = '';
        self.isSmsSent = false;

        if (error && error.response && error.response.data) {
          const data = error.response.data;
          if (data.type === 'urn:ietf:params:trustable:error:userBlocked') {
            self.isBlocked = true;
            const dateObj = new Date(data.detail);
            self.blockedUntil = dateObj.toLocaleDateString() + ', ' + dateObj.toLocaleTimeString();
          } else {
            self.authenticationError = true;
          }
        }
      });
  }

  public canUseSecondFactor(secondFactorType: string): boolean {
    if (this.$store.state.uiConfigStore.config.scndFactorTypes) {
      return this.$store.state.uiConfigStore.config.scndFactorTypes.includes(secondFactorType);
    }
    return false;
  }

  public doSpnegoLogin(): void {
    const self = this;

    localStorage.removeItem('jhi-authenticationToken');
    sessionStorage.removeItem('jhi-authenticationToken');
    this.spnegoAuthenticationError = false;

    axios
      .get('/spnego/login')
      .then(result => {
        self.extractAuthorization(result.headers);
        //        self.$router.push('/');
        self.setTimeoutPromise(() => {
          window.console.warn('retrieving account details after successful Kerberos login.');
          self.accountService().retrieveAccount();
          self.$root.$emit('bv::hide::modal', 'login-page');
        }, 1000);
      })
      .catch(reason => {
        const message = self.$t('global.messages.error.authenticationError');
        window.console.warn('problem doing Kerberos login. ' + reason);
        self.spnegoAuthenticationError = true;
      });
  }

  setTimeoutPromise(callback: () => void, ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms)).then(callback);
  }

  public extractAuthorization(headers): void {
    const bearerToken = headers.authorization;
    if (bearerToken && bearerToken.slice(0, 7) === 'Bearer ') {
      window.console.warn('extractAuthorization: bearer token present!');
      const jwt = bearerToken.slice(7, bearerToken.length);
      localStorage.setItem('jhi-authenticationToken', jwt);
    }
  }

  public convertDateTimeFromServer(value: Date): string {
    if (value) {
      const dateObj = new Date(value);
      return dateObj.toLocaleDateString() + ', ' + dateObj.toLocaleTimeString();
    }
    return null;
  }

  public requestClientCert(): void {
    const clientAuthTarget = this.$store.state.uiConfigStore.config.cryptoConfigView.clientAuthTarget;

    this.loginData.secondSecret = '';
    this.validatingClientCertificate = true;
    this.isNoClientCertificate = false;
    const self = this;

    axios
      .get(clientAuthTarget + '/publicapi/clientAuth')
      .then(result => {
        console.info('connected to client auth port');
        if (result.data && result.data.id_token) {
          self.loginData.secondSecret = result.data.id_token;
          console.info('client_cert_token: ' + self.loginData.secondSecret);
          self.authenticateCredentials();
        }
        self.validatingClientCertificate = false;
      })
      .catch(error => {
        // Handle the error response
        console.error('----' + error);
        self.isNoClientCertificate = true;
        self.validatingClientCertificate = false;
        self.loginData.secondSecret = '';
      });
  }

  public sendSMS(): void {
    this.isSmsSent = false;
    this.sendingSMS = true;
    const self = this;

    axios
      .post('/publicapi/smsDelivery/' + encodeURIComponent(this.loginData.username), this.loginData)
      .then(response => {
        self.isSmsSent = true;
        self.sendingSMS = false;
      })
      .catch(function (error) {
        console.log(error);
        const message = self.$t('problem processing request: ' + error);

        self.sendingSMS = false;

        const err = error as AxiosError;
        if (err.response) {
          console.log(err.response.status);
          console.log(err.response.data);
          if (err.response.status === 401) {
            console.info('Action not allowed', 'warn');
          } else {
            console.info(message, 'info');
          }
        } else {
          console.info(message, 'info');
        }
      });
  }
}
