import axios from 'axios';
import Component from 'vue-class-component';
import { Vue, Inject } from 'vue-property-decorator';
import AccountService from '@/account/account.service';

@Component({
  watch: {
    $route() {
      this.$root.$emit('bv::hide::modal', 'login-page');
    },
  },
})
export default class LoginForm extends Vue {
  @Inject('accountService')
  private accountService: () => AccountService;
  public authenticationError = null;
  public login: string = null;
  public password: string = null;
  public rememberMe: boolean = null;
  public isBlocked = false;
  public blockedUntil = '';

  public doLogin(): void {
    const data = { username: this.login, password: this.password, rememberMe: this.rememberMe };
    this.isBlocked = false;
    axios
      .post('api/authenticate', data)
      .then(result => {
        if (result && result.headers) {
          const bearerToken = result.headers.authorization;
          if (bearerToken && bearerToken.slice(0, 7) === 'Bearer ') {
            const jwt = bearerToken.slice(7, bearerToken.length);
            if (this.rememberMe) {
              localStorage.setItem('jhi-authenticationToken', jwt);
              sessionStorage.removeItem('jhi-authenticationToken');
            } else {
              sessionStorage.setItem('jhi-authenticationToken', jwt);
              localStorage.removeItem('jhi-authenticationToken');
            }
          }
          this.authenticationError = false;
          this.$root.$emit('bv::hide::modal', 'login-page');
          this.accountService().retrieveAccount();
        }
      })
      .catch(error => {
        // Handle the error response
        console.error('----' + error);
        this.password = '';
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

  public convertDateTimeFromServer(value: Date): string {
    if (value) {
      const dateObj = new Date(value);
      return dateObj.toLocaleDateString() + ', ' + dateObj.toLocaleTimeString()
    }
    return null;
  }

  public requestClientCert(): void {
    const clientAuthTarget = this.$store.state.uiConfigStore.config.cryptoConfigView.clientAuthTarget;

    axios
      //      .post(clientAuthTarget + '/publicapi/clientAuth', userLoginData
      .get(clientAuthTarget + '/publicapi/clientAuth')
      .then(result => {
        console.info('connected to client auth port');
        if (result && result.headers) {
          //          const clientAuthToken = result.headers;
        }
      })
      .catch(error => {
        // Handle the error response
        console.error('----' + error);
      });
  }
}
