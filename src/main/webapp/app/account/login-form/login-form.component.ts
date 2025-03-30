import axios, { AxiosError } from 'axios';
import Component from 'vue-class-component';
import { Vue, Inject } from 'vue-property-decorator';
import AccountService from '@/account/account.service';
import { IPreferences, ILoginData, IAuthSecondFactor } from '@/shared/model/transfer-object.model';

const STORAGE_SECONDFACTOR = '2ndFactor';

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

  public loginData: ILoginData = { authSecondFactor: 'NONE' };

  public isBlocked = false;
  public isSmsSent = false;
  public blockedUntil = '';

  public preferences: IPreferences = {};

  async mounted() {
    const authSecondFactorString = localStorage.getItem(STORAGE_SECONDFACTOR) || 'NONE';
    this.loginData.authSecondFactor = authSecondFactorString;
    window.console.info('local storage: ' + STORAGE_SECONDFACTOR + ' : ' + this.loginData.authSecondFactor);
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
  public showUsernameWarning(): boolean {
    if (!this.loginData.username || this.loginData.username.trim().length < 4) {
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

    if (this.showPasswordWarning()) {
      return false;
    }

    if (this.showSecondFactorWarning()) {
      return false;
    }

    return true;
  }

  public doLogin(): void {
    if (this.loginData.authSecondFactor == 'CLIENT_CERT') {
      this.requestClientCert();
    } else {
      this.authenticateCredentials();
    }
  }
  public authenticateCredentials(): void {
    this.isBlocked = false;
    const self = this;

    axios
      .post('api/authenticate', this.loginData)
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
    return this.$store.state.uiConfigStore.config.scndFactorTypes.includes(secondFactorType);
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
    const self = this;

    axios
      //      .post(clientAuthTarget + '/publicapi/clientAuth', userLoginData
      .get(clientAuthTarget + '/publicapi/clientAuth')
      .then(result => {
        console.info('connected to client auth port');
        if (result.data && result.data.id_token) {
          self.loginData.secondSecret = result.data.id_token;
          console.info('client_cert_token: ' + self.loginData.secondSecret);
          self.authenticateCredentials();
        }
      })
      .catch(error => {
        // Handle the error response
        console.error('----' + error);
        self.loginData.secondSecret = '';
      });
  }

  public sendSMS(): void {
    this.isSmsSent = false;
    const self = this;
    axios
      .post('/publicapi/smsDelivery/' + encodeURIComponent(this.loginData.username), this.loginData)
      .then(response => {
        self.isSmsSent = true;
      })
      .catch(function (error) {
        console.log(error);
        const message = self.$t('problem processing request: ' + error);

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
