import Component from 'vue-class-component';
import { Inject, Vue } from 'vue-property-decorator';
import LoginService from '@/account/login.service';

// import VueAxios from 'vue-axios'
// Vue.use(VueAxios, axios)

@Component
export default class Home extends Vue {
  @Inject('loginService')
  private loginService: () => LoginService;

  public openLogin(): void {
    this.loginService().openLogin((<any>this).$root);
  }

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
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

  el() { return '#vue-home'; }

  data() {
    return {
        chartData: {
            type: 'line',
            series: [{
                values: [4, 5, 3, 3, 4, 4]
            }]
        }
    };
  }
}
