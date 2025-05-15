import Component from 'vue-class-component';
import { Inject, Vue } from 'vue-property-decorator';
import LoginService from '@/account/login.service';
import AccountService from '@/account/account.service';

// import VueAxios from 'vue-axios'
// Vue.use(VueAxios, axios)

/*
 *
 * Currently this class is not active!!
 * The code section for this apge is includedn the vue file, directly!
 *
 */
@Component
export default class Home extends Vue {
  @Inject('loginService')
  private loginService: () => LoginService;

  @Inject('accountService') private accountService: () => AccountService;

  public openLogin(): void {
    this.loginService().openLogin((<any>this).$root);
  }

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public infoMsg(): string {
    return this.$store.state.uiConfigStore.config.infoMsg ? this.$store.state.uiConfigStore.config.infoMsg : '';
  }

  beforeRouteEnter(to, from, next) {
    next(vm => {
      window.console.info('################ Home to.params : ' + to.params.bearer);
    });
  }

  public mounted(): void {
    window.console.info('++++++++++++++++++ Home route.query : ' + this.$route.query.bearer);

    const token: string = this.$route.query.bearer[0];
    if (token) {
      window.console.info('setting bearer token to local storage : ' + token);
      localStorage.setItem('jhi-authenticationToken', token);
      this.accountService().retrieveAccount();
    }
  }

  public get chartdata(): Object {
    return {
      labels: ['January', 'February'],
      datasets: [
        {
          label: 'Data One',
          backgroundColor: '#f87979',
          data: [40, 20]
        }
      ]
    };
  }

  public get options(): Object {
    return {
      responsive: true,
      maintainAspectRatio: false
    };
  }

  el() {
    return '#vue-home';
  }

  data() {
    return {
      chartData: {
        type: 'line',
        series: [
          {
            values: [4, 5, 3, 3, 4, 4]
          }
        ]
      }
    };
  }
}
