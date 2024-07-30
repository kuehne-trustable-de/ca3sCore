import axios from 'axios';
import Component from 'vue-class-component';
import { Vue, Inject } from 'vue-property-decorator';
import AccountService from '@/account/account.service';
import { IUserLoginData } from '../../../../../../target/generated-sources/typescript/transfer-object.model';

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
        if (error && error.response && error.response.data) {
          const data = error.response.data;
          if (data.type === 'urn:ietf:params:trustable:error:userBlocked') {
            this.isBlocked = true;
            const dateObj = new Date(data.detail);
            this.blockedUntil = dateObj.toLocaleDateString() + ', ' + dateObj.toLocaleTimeString();
          }
        }
      });
  }

  public requestClientCert(): void {
    const userLoginData: IUserLoginData = { login: this.login, password: this.password, rememberMe: this.rememberMe };

    axios
      .post('https://localhost:8442/publicapi/clientAuth', userLoginData)
      .then(result => {
        console.info('connected to client auth port');
      })
      .catch(error => {
        // Handle the error response
        console.error('----' + error);
      });
  }
}
