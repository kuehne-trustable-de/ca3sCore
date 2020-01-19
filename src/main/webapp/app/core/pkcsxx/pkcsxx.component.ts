import Component from 'vue-class-component';
import { Inject, Vue } from 'vue-property-decorator';
import LoginService from '@/account/login.service';


import axios from 'axios'

// import VueAxios from 'vue-axios'
// Vue.use(VueAxios, axios)


@Component
export default class PKCSXX extends Vue {
	
  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

}
