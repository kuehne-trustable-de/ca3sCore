import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';

import { required} from 'vuelidate/lib/validators';

import { IUpload, Upload } from '@/shared/model/upload.model';

const validations: any = {
  upload: {
    user: {
    },
    password: {
    },
    csr: {
      required
    },
    checkResult: {
    }
  }
};

@Component({
  validations
})
export default class PKCSXX extends Vue {
 
  public upload: IUpload = new Upload();

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public notifyChange(evt: Event): void {
      alert("CSR changed");
  }

}
