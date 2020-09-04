import { Component, Inject } from 'vue-property-decorator';
import { Vue } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';

import axios from 'axios';

import { required, minLength} from 'vuelidate/lib/validators';

import { IUploadPrecheckData, IPkcsXXData, INamedValues, ICreationMode, IKeyAlgoLength, IPipelineView } from '@/shared/model/transfer-object.model';
import { IPipelineRestrictions, PipelineRestrictions } from '@/shared/model/pipeline-restrictions';
import { IPipelineRestriction, PipelineRestriction } from '@/shared/model/pipeline-restriction';

const precheckUrl = 'publicapi/describeContent';
const uploadUrl = 'api/uploadContent';

/*
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
  }
};

@Component({
  validations
})
*/

@Component
export default class PKCSXX extends mixins(AlertMixin, Vue) {

  public upload: IUploadPrecheckData = <IUploadPrecheckData>{};
  public precheckResponse: IPkcsXXData = <IPkcsXXData>{};

  public allWebPipelines: IPipelineView[] = [];
  public selectPipelineView: IPipelineView = {};
  public rdnRestrictions: IPipelineRestriction[] = [];
  public araRestrictions: IPipelineRestriction[] = [];

  public selectPipelineInfo = '';

  public pipelineRestrictions: IPipelineRestrictions = new PipelineRestrictions();

  public creationTool = 'keytool';
  public secretRepeat = '';
  public secret = '';
  public creationMode: ICreationMode = 'CSR_AVAILABLE';
  public keyAlgoLength: IKeyAlgoLength = 'RSA_2048';

  public cmdline = '';

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

  public alignRDNArraySize(restrictionIndex: number, valueIndex: number): void {
    window.console.info('in alignRDNArraySize(' + restrictionIndex + ', ' + valueIndex + ')');
    const restriction = this.rdnRestrictions[restrictionIndex];

    if ( restriction.multipleValues ) {

      const namedValue = this.upload.certificateAttributes[restrictionIndex];
      const currentSize = namedValue.values.length;
      const currentValue = namedValue.values[valueIndex] || '';

      if ( currentValue.trim().length === 0 ) {
        if ( currentSize > 1 ) {
          // preserve last element
          namedValue.values.splice(valueIndex, 1);
          window.console.info('in alignRDNArraySize(' + valueIndex + '): dropped empty element');
        }
      } else {
        if ( valueIndex + 1 === currentSize ) {
          namedValue.values.push('');
          window.console.info('in alignRDNArraySize(' + valueIndex + '): appended one element');
        }
      }
    }
    this.updateCmdLine();
  }

  public updateCmdLine(): void{
    this.cmdline = this.buildCommandLine();
  }

  public updatePipelineRestrictions(evt: any): void {
    const idx = evt.currentTarget.selectedIndex;
    this.updatePipelineRestrictionsByPipelineInfo(this.allWebPipelines[idx]);
  }

  public updatePipelineRestrictionsByPipelineInfo(pipeline: IPipelineView): void {

    this.selectPipelineView = pipeline;
    this.selectPipelineInfo = pipeline.description;

    this.rdnRestrictions = new Array<PipelineRestriction>();

    for (const rr of pipeline.rdnRestrictions) {
      if ( rr.cardinalityRestriction === 'NOT_ALLOWED') {
        // ignore this
      } else {
        this.rdnRestrictions.push( new PipelineRestriction(rr.rdnName, rr.cardinalityRestriction, rr.contentTemplate, rr.regExMatch));
      }
    }

    window.console.info('calling updatePipelineRestrictions 1: ' + this.selectPipelineInfo);

    this.pipelineRestrictions.c.alignContent();
    this.pipelineRestrictions.cn.alignContent();
    this.pipelineRestrictions.o.alignContent();
    this.pipelineRestrictions.ou.alignContent();
    this.pipelineRestrictions.l.alignContent();
    this.pipelineRestrictions.st.alignContent();
    this.pipelineRestrictions.san.alignContent();

    this.upload.secret = '';
    this.upload.certificateAttributes = new Array<INamedValues>();
    this.upload.arAttributes = new Array<INamedValues>();

    for (const rr of this.rdnRestrictions) {
      const nv: INamedValues = {};
      nv.name = rr.name;
      if ( rr.readOnly) {
        nv.values = [rr.template];
      } else {
        nv.values = [''];
      }
      this.upload.certificateAttributes.push(nv);
    }

    this.araRestrictions = new Array<PipelineRestriction>();

    for (const rr of pipeline.araRestrictions) {
      const cardinalityRestriction = rr.required ? 'ONE' : 'ZERO_OR_ONE';
      this.araRestrictions.push( new PipelineRestriction(rr.name, cardinalityRestriction, rr.contentTemplate, rr.regExMatch ));
    }

    for (const rr of this.araRestrictions) {
      const nv: INamedValues = {};
      nv.name = rr.name;
      if ( rr.readOnly) {
        nv.values = [rr.template];
      } else {
        nv.values = [''];
      }
      this.upload.arAttributes.push(nv);
    }

//    this.creationMode = 'CSR_AVAILABLE';
    this.upload.containerType = 'PKCS_12';
    this.keyAlgoLength = 'RSA_2048';
  }

  public buildCommandLine(): string {

    let cmdline = '';

    let nvSAN: INamedValues;

    for (const nv of this.upload.certificateAttributes) {
      if ( nv.name === 'SAN') {
        nvSAN = nv;
        break;
      }
    }

    let algo = 'undefined';
    if ( this.keyAlgoLength.startsWith('RSA')) {
      algo =  'RSA';
    }
    let keyLen = '2048';
    if ( this.keyAlgoLength.endsWith('4096')) {
      keyLen = '4096';
    }
    //
    // java keytool
    //
    if ( this.creationTool === 'keytool') {
      cmdline = 'keytool -genkeypair -keyalg ' + algo;
      cmdline += ' -keysize ' + keyLen;

      cmdline += ' -alias keyAlias -keystore test.p12 -storetype pkcs12';

      let dname = '';
      for (const nv of this.upload.certificateAttributes) {
          const name = nv.name;
          if ( name === 'SAN') {
            // handle SANS specially, see below
            continue;
          }

          for (const value of nv.values) {
            if ( value.length > 0) {
              if ( dname.length > 0) {
                dname += ', ';
              }
              dname += name + '=' + value;
            }
          }
      }
      cmdline += ' -dname "' + dname + '"\n\n';

      cmdline += 'keytool -certreq -keystore test.p12 -alias keyAlias';

      if (nvSAN !== undefined && nvSAN.values.length > 0 && nvSAN.values[0].length > 0) {
        let sans = '';
        for (const san of nvSAN.values) {
          if ( san.length > 0) {
            if ( sans.length > 0) {
              sans += ',';
            }
            if ( san.includes(':')) {
              sans += san;
            } else {
              sans += 'dns:' + san;
            }
          }
        }
        if ( sans.length > 0) {
          cmdline += ' -ext "SAN=' + sans + '"';
        }
      }
      cmdline += ' -file server.csr';
    } else {
      //
      // openssl
      //
      cmdline = 'openssl req -newkey ' + algo + ':' + keyLen;
      cmdline += ' -days 365 -nodes -subj ';

      let subject = '';
      for (const nv of this.upload.certificateAttributes) {
          const name = nv.name;
          if ( name === 'SAN') {
            // handle SANS specially, see below
            continue;
          }
          for (const value of nv.values) {
            if ( value.length > 0) {
              subject += '/' + name.toUpperCase() + '=' + value;
            }
          }
      }
      if ( subject.length > 0) {
        cmdline += '"' + subject + '"';
      }

      if (nvSAN !== undefined && nvSAN.values.length > 0 && nvSAN.values[0].length > 0) {
        cmdline += ' -extensions SAN -config <( cat $( echo /etc/ssl/openssl.cnf  ) <(printf "[SAN]\nsubjectAltName=\'';
        let sans = '';
        let idx = 1;
        for (const san of nvSAN.values) {
          if ( san.length > 0) {
            if ( sans.length > 0) {
              sans += ',';
            }
            const parts = san.split(':', 2);
            if ( parts.length < 2) {
              sans += 'DNS.' + idx + ':' + san;
            } else {
              sans += parts[0] + '.' + idx + ':' + parts[1];
            }
          }
          idx++;
        }
        cmdline += sans + '\'"))';
      }

      cmdline += ' -keyout private_key.pem -out server.csr';

    }

    return cmdline;
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

    this.upload.creationMode = this.creationMode;
    this.upload.keyAlgoLength = this.keyAlgoLength;
    this.upload.secret = this.secret;

    if ( this.creationMode === 'CSR_AVAILABLE' && this.upload.content.trim().length === 0) {
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

        for (const nv of this.upload.certificateAttributes) {
          if ( nv.name === 'SAN') {
            nv.values = this.precheckResponse.certificates[0].sans;
          } else {
            for (const subjectPart of this.precheckResponse.certificates[0].subjectParts) {
              if ( subjectPart.name.toUpperCase() === nv.name.toUpperCase()) {
                nv.values = subjectPart.values;
              }
            }
          }
        }
      }
      this.isChecked = true;
    } catch (error) {
      console.error(error);
      document.body.style.cursor = 'default';
      this.isChecked = false;
      this.responseStatus = error.response.status;
      const message = this.$t('problem processing request: ' + error);
      this.alertService().showAlert(message, 'info');

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
        (this.isRAOfficer() || this.isAdmin()) ) {
      return !this.precheckResponse.certificates[0].certificatePresentInDB;
    }
    return false;
  }

  public disableCertificateRequest(): boolean {

    if (this.creationMode === 'CSR_AVAILABLE') {
      if (this.precheckResponse.csrPublicKeyPresentInDB ) {
        return true;
      }
      return false;

    } else if (this.creationMode === 'SERVERSIDE_KEY_CREATION') {
      window.console.info('upload.secret : "' + this.secret + '" , secretRepeat : "' + this.secretRepeat + '"' );
      if (this.secret.trim() === this.secretRepeat.trim()) {
        return false;
      }
    }
    return true;
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
    return this.hasRole('ROLE_RA');
  }

  public isAdmin() {
    return this.hasRole('ROLE_ADMIN');
  }

  public hasRole(targetRole: string) {
    for (const role of this.$store.getters.account.authorities) {
      if ( targetRole === role) {
        return true;
      }
    }
    return false;
  }

  public get roles(): string {
    return this.$store.getters.account ? this.$store.getters.account.authorities[0] : '';
  }

  public updateValue(key, value) {
    window.console.info('updateValue for ' + key );
    this.$emit('change', { ...this.upload, [key]: value });
  }
}
