import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';

import axios from 'axios';

import { required} from 'vuelidate/lib/validators';

import { IUploadPrecheckData, IPkcsXXData } from '@/shared/model/transfer-object.model';
import { IPipeline } from '@/shared/model/pipeline.model';

const precheckUrl = 'publicapi/describeContent';
const uploadUrl = 'api/uploadContent';

const validations: any = {
  upload: {
    pipelineId: {
    },
    passphrase: {
    },
    requestorcomment: {

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
  public precheckResponse: IPkcsXXData = <IPkcsXXData>{};

  public allWebPipelines: IPipeline[] = [];

  public responseStatus = 0;
  public isChecked = false;
  public isChecking = false;
  public isSaving = false;

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public catchDroppedFile(e: DragEvent): void {
    const droppedFiles = e.dataTransfer.files;
    if (!droppedFiles && droppedFiles.length === 0) {
      return;
    }
    const readerBase64 = new FileReader();
    const readerBinary = new FileReader();
    const blob = droppedFiles.item(0).slice(0, droppedFiles.item(0).size);
    const self = this;

    readerBase64.onloadend = function() {
        const base64Text = readerBase64.result.toString();

        if ( /^[\x00-\x7F]*$/.test(base64Text) ) {
          self.upload.content = base64Text;
          console.log('dropped ascii-only content : ' + base64Text);
          self.$forceUpdate();
          self.contentCall(precheckUrl);
        } else {
          readerBinary.readAsDataURL(blob);
        }
    };

    readerBinary.onloadend = function() {
        const base64Text = readerBinary.result.toString().split(',')[1];
        self.upload.content = base64Text;
        console.log('dropped binary content, base64 encoded : ' + base64Text);
        self.$forceUpdate();
        self.contentCall(precheckUrl);
    };

    readerBase64.readAsText(blob);
  }

  public notifyChange(_evt: Event): void {
//      alert('CSR changed');
      this.contentCall(precheckUrl);
  }

  public uploadContent(_evt: Event): void {
      this.contentCall(uploadUrl);
  }

  async contentCall(url: string) {
    // don't do a call without content
    if (this.upload.content.trim().length === 0) {
      this.precheckResponse.dataType = 'UNKNOWN';
      return;
    }

    this.responseStatus = 0;
    try {
      document.body.style.cursor = 'wait';
      const response = await axios.post(`${url}`, this.upload);
      this.precheckResponse = response.data;
      console.log(this.precheckResponse.dataType);
      document.body.style.cursor = 'default';

      if ( this.precheckResponse && this.precheckResponse.dataType === 'CSR' &&
           this.precheckResponse.csrPending ) {
        this.$router.push({name: 'CsrInfo', params: {csrId: this.precheckResponse.createdCSRId}});
      }

      if ( this.precheckResponse && this.precheckResponse.dataType === 'X509_CERTIFICATE_CREATED' &&
           this.precheckResponse.certificates[0]) {
        this.$router.push({name: 'CertInfo', params: {certificateId: this.precheckResponse.certificates[0].certificateId.toString()}});
      }

      if ( this.precheckResponse && this.precheckResponse.certificates &&
           this.precheckResponse.dataType === 'X509_CERTIFICATE' &&
           this.precheckResponse.certificates[0].pemCertrificate ) {
        this.upload.content = this.precheckResponse.certificates[0].pemCertrificate;
      }
      this.isChecked = true;
    } catch (error) {
      console.error(error);
      document.body.style.cursor = 'default';
      this.isChecked = false;
      this.responseStatus = error.response.status;
    }
  }

  public mounted(): void {
    this.fillPipelineData();
  }

  public fillPipelineData(): void {
    window.console.info('calling fillPipelineData');
    const self = this;

    axios({
      method: 'get',
      url: 'api//pipeline/getWebPipelines',
      responseType: 'stream'
    })
    .then(function(response) {
      window.console.info('getWebPipelines returns ' + response.data );
      self.allWebPipelines = response.data;
    });
  }

  public showRequestorCommentsArea(): boolean {
    window.console.info('pipelineId : ' + this.upload.pipelineId );
    return this.authenticated;
  }

  public currentPipelineInfo( pipelineId): string {
    window.console.info('currentPipelineInfo : ' + pipelineId );

    for ( let i = 0; i < this.allWebPipelines.length; i++ ) {
      window.console.info('checking pipelineId : ' + this.upload.pipelineId );
      if ( this.upload.pipelineId === this.allWebPipelines[i].id ) {
        return this.allWebPipelines[i].description;
      }
    }
    return '';
  }
}
