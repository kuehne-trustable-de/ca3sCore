import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';

import axios from 'axios';

import { required} from 'vuelidate/lib/validators';

//import { IUploadPrecheckData, UploadPrecheckData } from '@/shared/model/upload-precheck-data.model';

import { IUploadPrecheckData, IPkcsXXData, PkcsXXData } from '@/shared/model/transfer-object.model';

const precheckUrl = 'publicapi/describeContent';
const uploadUrl = 'api/uploadContent';

const validations: any = {
  upload: {
    passphrase: {
    },
    content: {
      required
    }
  }
};

@Component({
  validations
})
export default class PKCSXX extends Vue {

  public upload: IUploadPrecheckData = <IUploadPrecheckData>{};
  public precheckResponse: IPkcsXXData = new PkcsXXData();

  public isChecked = false;
  public isChecking = false;
  public isSaving = false;

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public notifyChange(_evt: Event): void {
//      alert('CSR changed');
      this.contentCall(precheckUrl);
  }

  public uploadContent(_evt: Event): void {
      this.contentCall(uploadUrl);
  }

  async contentCall(url: string) {
    try {
      let response =  await axios.post(`${url}`, this.upload);
      this.precheckResponse = response.data; 
      console.log(this.precheckResponse.dataType);
      this.isChecked = true;
    } catch (error) {
      console.error(error);
      this.isChecked = false;
    }
  }

}
