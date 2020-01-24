import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';

@Component
export default class PKCSXX extends Vue {

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

}
