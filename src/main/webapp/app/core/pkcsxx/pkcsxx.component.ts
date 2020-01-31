import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';

import axios from 'axios';

import { required} from 'vuelidate/lib/validators';

import { IUpload, Upload } from '@/shared/model/upload.model';
import { IUploadPrecheckResponse, UploadPrecheckResponse } from '@/shared/model/upload-precheck-response.model';

const baseApiUrl = 'publicapi/acme-orders';

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
  public precheckResponse: IUploadPrecheckResponse = new UploadPrecheckResponse();

  public isChecking = false;
  public isSaving = false;

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public notifyChange(_evt: Event): void {
      alert('CSR changed');
      this.precheck();
  }

  async precheck() {
    try {
      this.precheckResponse =  await axios.post(`${baseApiUrl}`, this.upload);
      console.log(this.precheckResponse.checkResult);
    } catch (error) {
      console.error(error);
    }
  }

}
