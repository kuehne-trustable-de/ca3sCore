import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';

import axios from 'axios';

import { required, minLength} from 'vuelidate/lib/validators';

import { IUploadPrecheckData, IPkcsXXData, INamedValues } from '@/shared/model/transfer-object.model';
import { IPipelineRestrictions, PipelineRestrictions } from '@/shared/model/pipeline-restrictions';
import { IPipeline } from '@/shared/model/pipeline.model';

const precheckUrl = 'publicapi/describeContent';
const uploadUrl = 'api/uploadContent';

const validations: any = {
  upload: {
    pipelineId: {
      id: {required},
      name: {required}
    },
    passphrase: {
    },
    requestorcomment: {
    },
    content: {
      required
    }
  },
  certRequest: {
    c: {},
    cn: {},
    o: {},
    ou: {},
    st: {},
    l: {},
  }
};

@Component({
  validations
})
export default class PKCSXX extends Vue {

  public upload: IUploadPrecheckData = <IUploadPrecheckData>{};
  public precheckResponse: IPkcsXXData = <IPkcsXXData>{};

  public allWebPipelines: IPipeline[] = [];
  public selectPipelineInfo = '';

  public pipelineRestrictions: IPipelineRestrictions = new PipelineRestrictions();

  public creationMode = 'csrAvailable';

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

  public updatePipelineRestrictions(evt: any): void {
    const idx = evt.currentTarget.selectedIndex;
    this.updatePipelineRestrictionsByPipelineInfo(this.allWebPipelines[idx]);
  }

  public updatePipelineRestrictionsByPipelineInfo(pipeline: IPipeline): void {

    this.selectPipelineInfo = pipeline.description;

    window.console.info('calling updatePipelineRestrictions 1: ' + this.selectPipelineInfo);

    for (const pAtt of pipeline.pipelineAttributes) {
      if ( pAtt.name === 'RESTR_C_CARDINALITY') {
        this.pipelineRestrictions.c.cardinality = pAtt.value;
      } else if ( pAtt.name === 'RESTR_C_TEMPLATE') {
        this.pipelineRestrictions.c.template = pAtt.value;
      } else if ( pAtt.name === 'RESTR_C_REGEXMATCH') {
        this.pipelineRestrictions.c.regex = pAtt.value.toLowerCase() === 'true';
      } else if ( pAtt.name === 'RESTR_CN_CARDINALITY') {
        this.pipelineRestrictions.cn.cardinality = pAtt.value;
      } else if ( pAtt.name === 'RESTR_CN_TEMPLATE') {
        this.pipelineRestrictions.cn.template = pAtt.value;
      } else if ( pAtt.name === 'RESTR_CN_REGEXMATCH') {
        this.pipelineRestrictions.cn.regex = pAtt.value.toLowerCase() === 'true';
      } else if ( pAtt.name === 'RESTR_O_CARDINALITY') {
        this.pipelineRestrictions.o.cardinality = pAtt.value;
      } else if ( pAtt.name === 'RESTR_O_TEMPLATE') {
        this.pipelineRestrictions.o.template = pAtt.value;
      } else if ( pAtt.name === 'RESTR_O_REGEXMATCH') {
        this.pipelineRestrictions.o.regex = pAtt.value.toLowerCase() === 'true';
      } else if ( pAtt.name === 'RESTR_OU_CARDINALITY') {
        this.pipelineRestrictions.ou.cardinality = pAtt.value;
      } else if ( pAtt.name === 'RESTR_OU_TEMPLATE') {
        this.pipelineRestrictions.ou.template = pAtt.value;
      } else if ( pAtt.name === 'RESTR_OU_REGEXMATCH') {
        this.pipelineRestrictions.ou.regex = pAtt.value.toLowerCase() === 'true';
      } else if ( pAtt.name === 'RESTR_L_CARDINALITY') {
        this.pipelineRestrictions.l.cardinality = pAtt.value;
      } else if ( pAtt.name === 'RESTR_L_TEMPLATE') {
        this.pipelineRestrictions.l.template = pAtt.value;
      } else if ( pAtt.name === 'RESTR_L_REGEXMATCH') {
        this.pipelineRestrictions.l.regex = pAtt.value.toLowerCase() === 'true';
      } else if ( pAtt.name === 'RESTR_ST_CARDINALITY') {
        this.pipelineRestrictions.st.cardinality = pAtt.value;
      } else if ( pAtt.name === 'RESTR_ST_TEMPLATE') {
        this.pipelineRestrictions.st.template = pAtt.value;
      } else if ( pAtt.name === 'RESTR_ST_REGEXMATCH') {
        this.pipelineRestrictions.st.regex = pAtt.value.toLowerCase() === 'true';
      }
    }
    this.pipelineRestrictions.c.alignContent();
    this.pipelineRestrictions.cn.alignContent();
    this.pipelineRestrictions.o.alignContent();
    this.pipelineRestrictions.ou.alignContent();
    this.pipelineRestrictions.l.alignContent();
    this.pipelineRestrictions.st.alignContent();

    this.upload.certificateAttributes = [<INamedValues>{}, <INamedValues>{}, <INamedValues>{}, <INamedValues>{}, <INamedValues>{}, <INamedValues>{}, <INamedValues>{}];
/*
    this.upload.certificateAttributes[0] = <INamedValues>{};
    this.upload.certificateAttributes[1] = <INamedValues>{};
    this.upload.certificateAttributes[2] = <INamedValues>{};
    this.upload.certificateAttributes[3] = <INamedValues>{};
    this.upload.certificateAttributes[4] = <INamedValues>{};
    this.upload.certificateAttributes[5] = <INamedValues>{};
    this.upload.certificateAttributes[6] = <INamedValues>{};
*/
    this.upload.certificateAttributes[0].name = 'c';
    this.upload.certificateAttributes[1].name = 'cn';
    this.upload.certificateAttributes[2].name = 'o';
    this.upload.certificateAttributes[3].name = 'ou';
    this.upload.certificateAttributes[4].name = 'l';
    this.upload.certificateAttributes[5].name = 'st';
    this.upload.certificateAttributes[6].name = 'san';

    if ( this.pipelineRestrictions.c.readOnly) {
      this.upload.certificateAttributes[0].values = [this.pipelineRestrictions.c.template];
    } else {
      this.upload.certificateAttributes[0].values = [''];
    }
    if ( this.pipelineRestrictions.cn.readOnly) {
      this.upload.certificateAttributes[1].values = [this.pipelineRestrictions.cn.template];
    } else {
      this.upload.certificateAttributes[1].values = [''];
    }
    if ( this.pipelineRestrictions.o.readOnly) {
      this.upload.certificateAttributes[2].values = [this.pipelineRestrictions.o.template];
    } else {
      this.upload.certificateAttributes[2].values = [''];
    }
    if ( this.pipelineRestrictions.ou.readOnly) {
      this.upload.certificateAttributes[3].values = [this.pipelineRestrictions.ou.template];
    } else {
      this.upload.certificateAttributes[3].values = [''];
    }
    if ( this.pipelineRestrictions.l.readOnly) {
      this.upload.certificateAttributes[4].values = [this.pipelineRestrictions.l.template];
    } else {
      this.upload.certificateAttributes[4].values = [''];
    }
    if ( this.pipelineRestrictions.st.readOnly) {
      this.upload.certificateAttributes[5].values = [this.pipelineRestrictions.st.template];
    } else {
      this.upload.certificateAttributes[5].values = [''];
    }
    this.upload.certificateAttributes[6].values = [''];
    window.console.info('calling updatePipelineRestrictions 2: ' + this.selectPipelineInfo);

  }

  public notifyChange(_evt: Event): void {
      this.contentCall(precheckUrl);
  }

  // handle the selection of a file
  public notifyFileChange(evt: any): void {

    const self = this;
    const selectedFile = evt.target.files[0];
    const readerBase64 = new FileReader();
      readerBase64.onload =
        function(_result) {
          const base64Text = readerBase64.result.toString();

          // check, whether this is base64 encoded content
          if ( /^[\x00-\x7F]*$/.test(base64Text) ) {
            self.upload.content = base64Text;
            self.contentCall(precheckUrl);
          } else {
            // binary, start re-reading it as base64-encoded comntent
            const readerBinary = new FileReader();
            readerBinary.onload =
              function(__result) {
                const base64Bin = readerBinary.result.toString().split(',')[1];
                self.upload.content = base64Bin;
                self.contentCall(precheckUrl);
              };
            readerBinary.readAsDataURL(selectedFile);
          }
        };
      readerBase64.readAsText(selectedFile);

  }

  // handle any changes affecting the plain content
  public uploadContent(_evt: Event): void {
      this.contentCall(uploadUrl);
  }

  // run for the backend and tell about all the bytes to process
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
      url: 'api/pipeline/getWebPipelines',
      responseType: 'stream'
    })
    .then(function(response) {
      window.console.info('getWebPipelines returns ' + response.data );
      self.allWebPipelines = response.data;
      if ( self.allWebPipelines.length > 0 ) {
        self.upload.pipelineId = self.allWebPipelines[0].id;
        self.updatePipelineRestrictionsByPipelineInfo(self.allWebPipelines[0]);
      }
    });
  }

  public showCSRRelatedArea(): boolean {
//    window.console.info('pipelineId : ' + this.upload.pipelineId );
    if ( this.precheckResponse &&
      this.precheckResponse.dataType === 'CSR' &&
      this.authenticated ) {
        return true;
    }
    return false;
  }

  public showCertificateUpload(): boolean {
    if ( this.precheckResponse &&
         ( this.precheckResponse.dataType === 'X509_CERTIFICATE' || this.precheckResponse.dataType === 'CONTAINER' ) &&
        this.isRAOfficer() ) {
      return !this.precheckResponse.certificates[0].certificatePresentInDB;
    }
    return false;
  }

  public currentPipelineInfo( pipelineId: number): string {
    window.console.info('currentPipelineInfo : ' + pipelineId );

    for ( let i = 0; i < this.allWebPipelines.length; i++ ) {
      window.console.info('checking pipelineId : ' + pipelineId );
      if ( pipelineId === this.allWebPipelines[i].id ) {
        return this.allWebPipelines[i].description;
      }
    }
    return '';
  }

  public isRAOfficer() {
      return this.roles === 'ROLE_RA';
  }

  public get roles(): string {
    return this.$store.getters.account ? this.$store.getters.account.authorities[0] : '';
  }

}
