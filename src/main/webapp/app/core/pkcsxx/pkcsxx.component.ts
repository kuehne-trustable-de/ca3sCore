import { Component } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';
import { Vue } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';

import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';

import axios from 'axios';

import { required } from 'vuelidate/lib/validators';

import {
  IUploadPrecheckData,
  IPkcsXXData,
  INamedValues,
  ICreationMode,
  IKeyAlgoLength,
  IPipelineView,
  IPreferences
} from '@/shared/model/transfer-object.model';
import { IPipelineRestrictions, PipelineRestrictions } from '@/shared/model/pipeline-restrictions';
import { IPipelineRestriction, PipelineRestriction } from '@/shared/model/pipeline-restriction';

const precheckUrl = 'publicapi/describeContent';
const uploadUrl = 'api/uploadContent';

const validations: any = {
  upload: {
    certificateAttributes: {
      $each: {
        certificateAttributes: {
          $each: {
            values: { required }
          }
        }
      }
    },
    arAttributes: {
      $each: {
        arAttributes: {
          $each: {
            values: { required }
          }
        }
      }
    },
    pipelineId: {
      id: { required },
      name: { required }
    },
    passphrase: {},
    requestorcomment: {},
    content: {
      required
    }
  }
};

@Component({
  validations,
  components: {
    Fragment,
    CopyClipboardButton,
    HelpTag
  }
})
export default class PKCSXX extends mixins(AlertMixin, Vue) {
  public preselectedPipelineId = -1;

  public upload: IUploadPrecheckData = <IUploadPrecheckData>{};
  public precheckResponse: IPkcsXXData = <IPkcsXXData>{};

  public preferences: IPreferences = {};
  public allWebPipelines: IPipelineView[] = [];
  public selectPipelineView: IPipelineView = { csrUsage: 'TLS_SERVER' };
  public rdnRestrictions: IPipelineRestriction[] = [];
  public araRestrictions: IPipelineRestriction[] = [];

  public selectPipelineInfo = '';

  public pipelineRestrictions: IPipelineRestrictions = new PipelineRestrictions();

  public creationTool = 'keytool';
  public cnAsSAN = false;
  public secretRepeat = '';
  public secret = '';
  public creationMode: ICreationMode = 'CSR_AVAILABLE';
  public keyAlgoLength: IKeyAlgoLength = 'RSA_2048';

  public cmdline = '';
  public cmdline0 = '';
  public cmdline1 = '';
  public cmdline0Required = false;
  public cmdline1Required = false;
  public reqConf = '';
  public reqConfRequired = false;

  public responseStatus = 0;
  public isChecked = false;
  public isChecking = false;
  public isSaving = false;
  public warnings: string[] = [];

  public updateCounter = 1;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.query.pipelineId) {
        vm.init(Number(to.query.pipelineId));
      } else {
        vm.init(-1);
      }
    });
  }

  public init(pipelineId: number): void {
    this.preselectedPipelineId = pipelineId;
    console.log('initialized with pipelineId : ' + pipelineId);
  }
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

      if (/^[\x00-\x7F]*$/.test(base64Text)) {
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

  public showProblemWarning(rr: IPipelineRestriction, valueIndex: number, value: string): boolean {
    if (this.showRequiredWarning(rr.required, value)) {
      return true;
    }
    if (this.showContentWarning(rr, valueIndex, value)) {
      return true;
    }
    if (this.showRegExpWarning(rr, valueIndex, value)) {
      return true;
    }
    if (this.showContentOrSANWarning(rr, valueIndex, value)) {
      return true;
    }
    return false;
  }

  public showRequiredWarning(isRequired: boolean, value: string): boolean {
    console.log('showRequiredWarning( ' + isRequired + ', "' + value + '"');
    if (isRequired) {
      if (value.trim().length === 0) {
        return true;
      }
    }
    return false;
  }

  public showContentWarning(rr: IPipelineRestriction, valueIndex: number, value: string): boolean {
    console.log('showContentWarning( ' + rr.required + ', ' + valueIndex + ', "' + value + '")');
    if (rr.required && valueIndex === 0 && value.trim().length === 0) {
      console.log('showContentWarning( ' + rr.required + ', ' + valueIndex + ', "' + value + '") does not match');
      return true;
    }
    return false;
  }

  public showContentOrSANWarning(rr: IPipelineRestriction, valueIndex: number, value: string): boolean {
    console.log('showContentOrSANWarning( ' + rr.required + ', ' + valueIndex + ', "' + value + '")');
    if (rr.cardinality === 'ONE_OR_SAN' && valueIndex === 0 && value.trim().length === 0) {
      for (const nv of this.upload.certificateAttributes) {
        if (nv.name === 'SAN' && nv.values.length > 0 && nv.values[0].trim().length > 0) {
          console.log('showContentWarning: CN empty, SAN present');
          return false;
        }
      }
      console.log('showContentOrSANWarning( ' + rr.required + ', ' + valueIndex + ', "' + value + '") does not match');
      return true;
    }
    return false;
  }

  public showRegExpWarning(rr: IPipelineRestriction, valueIndex: number, value: string): boolean {
    console.log('showRegExpWarning( ' + rr.regExMatch + ', ' + valueIndex + ', "' + value + '")');
    console.log('showRegExpWarning : rr.regEx = ' + rr.regEx);
    if (rr.regExMatch && valueIndex === 0 && rr.regEx.trim().length > 0) {
      const regexp = new RegExp(rr.regEx);
      const valid = regexp.test(value);
      console.log('showRegExpWarning( ' + rr.regExMatch + ', ' + valueIndex + ', "' + value + '") -> ' + valid);
      return !valid;
    }
    return false;
  }

  public alignRDNArraySize(restrictionIndex: number, valueIndex: number): void {
    window.console.info('in alignRDNArraySize(' + restrictionIndex + ', ' + valueIndex + ')');
    const restriction = this.rdnRestrictions[restrictionIndex];

    if (restriction.multipleValues) {
      const namedValue = this.upload.certificateAttributes[restrictionIndex];
      const currentSize = namedValue.values.length;
      const currentValue = namedValue.values[valueIndex] || '';

      if (currentValue.trim().length === 0) {
        if (currentSize > 1) {
          // preserve last element
          namedValue.values.splice(valueIndex, 1);
          window.console.info('in alignRDNArraySize(' + valueIndex + '): dropped empty element');
        }
      } else {
        if (valueIndex + 1 === currentSize) {
          namedValue.values.push('');
        }
      }
    }
    this.updateForm();
    this.updateCmdLine();
  }

  public updateForm(): void {
    window.console.info('in updateForm, incrementing this.updateCounter:  ' + this.updateCounter);
    this.updateCounter += 1;
  }

  public updateCmdLine(): void {
    this.cmdline = this.buildCommandLine();
    this.updateForm();
  }

  public updateAdditionalRestriction(): void {
    window.console.info('in updateAdditionalRestriction ... ');
    this.contentCall(precheckUrl);

    this.updateCmdLine();
  }

  public updateCurrentPipelineRestrictions(): void {
    let pipelineView = this.allWebPipelines[0];

    for (const pv of this.allWebPipelines) {
      if (this.upload.pipelineId === pv.id) {
        pipelineView = pv;
        break;
      }
    }

    //    window.console.info('pipelineView  id: ' + pipelineView.id + ', name: ' + pipelineView.name );

    this.updatePipelineRestrictionsByPipelineInfo(pipelineView);
    this.precheckResponse.dataType = 'UNKNOWN';
    this.precheckResponse.warnings = [];
    if (this.creationMode !== 'CSR_AVAILABLE') {
      this.upload.content = '';
    }
    this.isChecked = false;
  }

  public updatePipelineRestrictionsByPipelineInfo(pipeline: IPipelineView): void {
    if (!pipeline) {
      window.console.info('calling updatePipelineRestrictions with NO pipeline!');
      return;
    }

    window.console.info('calling updatePipelineRestrictions for pipeline ' + pipeline.name);

    this.selectPipelineView = pipeline;
    if (pipeline.description) {
      this.selectPipelineInfo = pipeline.description;
    } else {
      this.selectPipelineInfo = '';
    }

    this.rdnRestrictions = new Array<PipelineRestriction>();

    if (pipeline.rdnRestrictions) {
      for (const rr of pipeline.rdnRestrictions) {
        if (rr.cardinalityRestriction === 'NOT_ALLOWED') {
          // ignore this
        } else {
          this.rdnRestrictions.push(
            new PipelineRestriction(rr.rdnName, rr.cardinalityRestriction, rr.contentTemplate, rr.regExMatch, rr.regEx)
          );
        }
      }
    }

    window.console.info('calling updatePipelineRestrictions 1: ' + this.selectPipelineInfo);

    this.pipelineRestrictions.c.alignContent();
    this.pipelineRestrictions.cn.alignContent();
    this.pipelineRestrictions.o.alignContent();
    this.pipelineRestrictions.ou.alignContent();
    this.pipelineRestrictions.l.alignContent();
    this.pipelineRestrictions.st.alignContent();
    this.pipelineRestrictions.e.alignContent();
    this.pipelineRestrictions.san.alignContent();

    this.upload.secret = '';
    this.upload.certificateAttributes = new Array<INamedValues>();
    this.upload.arAttributes = new Array<INamedValues>();

    for (const rr of this.rdnRestrictions) {
      const nv: INamedValues = {};
      nv.name = rr.name;
      if (rr.readOnly) {
        nv.values = [rr.template];
      } else {
        nv.values = [''];
      }
      this.upload.certificateAttributes.push(nv);
    }

    this.araRestrictions = new Array<PipelineRestriction>();

    if (pipeline.araRestrictions) {
      for (const rr of pipeline.araRestrictions) {
        const cardinalityRestriction = rr.required ? 'ONE' : 'ZERO_OR_ONE';
        this.araRestrictions.push(new PipelineRestriction(rr.name, cardinalityRestriction, rr.contentTemplate, rr.regExMatch, rr.regEx));
      }
    }

    for (const rr of this.araRestrictions) {
      const nv: INamedValues = {};
      nv.name = rr.name;
      if (rr.readOnly) {
        nv.values = [rr.template];
      } else {
        nv.values = [''];
      }
      this.upload.arAttributes.push(nv);
    }

    //    this.creationMode = 'CSR_AVAILABLE';
    this.upload.containerType = 'PKCS_12';
    this.keyAlgoLength = 'RSA_4096';
  }

  public buildCommandLine(): string {
    let cmdline = '';
    let reqConf = '';
    let cmdline0 = '';

    this.reqConfRequired = false;
    this.cmdline0Required = false;
    this.cmdline1Required = false;

    let nvCN: INamedValues;
    let nvSAN: INamedValues;

    let soon: Date = new Date();

    let fileName = 'file';

    for (const nv of this.upload.certificateAttributes) {
      if (nv.name === 'CN') {
        nvCN = nv;
      }
    }

    for (const nv of this.upload.certificateAttributes) {
      if (nv.name === 'SAN') {
        nvSAN = nv;

        if (this.cnAsSAN) {
          if (
            nv.values.indexOf(nvCN.values[0]) === -1 &&
            nv.values.indexOf('DNS:' + nvCN.values[0]) === -1 &&
            nv.values.indexOf('IP:' + nvCN.values[0]) === -1
          ) {
            nvSAN = {};
            nvSAN.name = nv.name;
            const cnVal = 'DNS:' + nvCN.values[0];
            if (nv.values.length === 0) {
              nvSAN.values = [cnVal];
            } else {
              nvSAN.values = [cnVal, ...nv.values];
            }
          }
        }
        break;
      }
    }

    // is there a SAN set?
    const hasSAN = nvSAN && nvSAN.values && nvSAN.values.length > 0 && nvSAN.values[0].trim().length > 0;

    if (nvCN && nvCN.values.length > 0 && nvCN.values[0].trim().length > 0) {
      fileName = nvCN.values[0];
    } else if (nvSAN && nvSAN.values && nvSAN.values.length > 0 && nvSAN.values[0].trim().length > 0) {
      fileName = nvSAN.values[0];
    }
    fileName += '_' + soon.toISOString().substring(0, 10);

    const re = new RegExp(/[ .]/, 'g');
    fileName = fileName.replace(re, '_');

    let algo = 'undefined';
    if (this.keyAlgoLength.startsWith('RSA')) {
      algo = 'RSA';
    }
    let keyLen = '4096';
    if (this.keyAlgoLength.endsWith('2048')) {
      keyLen = '2048';
    }

    let hashAlgo = 'sha256';

    //
    // java keytool
    //
    if (this.creationTool === 'keytool') {
      cmdline0 = 'keytool -genkeypair -keyalg ' + algo;
      cmdline0 += ' -keysize ' + keyLen;

      const aliasP12Type = ' -alias keyAlias -keystore ' + fileName + '.p12 -storetype pkcs12';
      cmdline0 += aliasP12Type;

      let dname = '';
      for (const nv of this.upload.certificateAttributes) {
        let name = nv.name;
        if (name === 'SAN') {
          // handle SANS specially, see below
          continue;
        }

        if ('E' === name.toUpperCase()) {
          name = 'EMAILADDRESS';
        }

        for (const value of nv.values) {
          if (value.length > 0) {
            if (dname.length > 0) {
              dname += ', ';
            }
            dname += name + '=' + value;
          }
        }
      }
      cmdline0 += ' -dname "' + dname + '"\n\n';

      this.cmdline0 = cmdline0;
      this.cmdline0Required = true;

      this.cmdline1 = 'keytool -importcert -file ' + fileName + '.cer' + aliasP12Type;
      this.cmdline1Required = true;

      cmdline += 'keytool -certreq' + aliasP12Type;

      if (nvSAN !== undefined && nvSAN.values.length > 0 && nvSAN.values[0].length > 0) {
        let sans = '';
        for (const san of nvSAN.values) {
          if (san.length > 0) {
            if (sans.length > 0) {
              sans += ',';
            }
            if (san.includes(':')) {
              sans += san;
            } else {
              sans += 'DNS:' + san;
            }
          }
        }
        if (sans.length > 0) {
          cmdline += ' -ext "SAN=' + sans + '"';
        }
      }
      cmdline += ' -file ' + fileName + '.csr';
    } else if (this.creationTool === 'openssl_ge_1.1.1') {
      //
      // openssl >= 1.1.1
      //
      cmdline = this.getOpensslCommon(cmdline, algo, hashAlgo, keyLen, true);

      if (nvSAN !== undefined && nvSAN.values.length > 0 && nvSAN.values[0].length > 0) {
        cmdline += ' -addext "subjectAltName = ';
        let sans = '';
        let idx = 1;
        for (const san of nvSAN.values) {
          if (san.length > 0) {
            if (sans.length > 0) {
              sans += ',';
            }
            const parts = san.split(':', 2);
            if (parts.length < 2) {
              sans += 'DNS:' + san;
            } else {
              sans += parts[0] + ':' + parts[1];
            }
          }
          idx++;
        }
        cmdline += sans + '"';
      }
      cmdline += ' -keyout ' + fileName + '.private_key.pem -out ' + fileName + '.csr';
    } else if (this.creationTool === 'certreq') {
      cmdline += 'certreq –new requestconfig.inf ' + fileName + '.csr';

      this.reqConfRequired = true;

      let dnLines = '';

      for (const nv of this.upload.certificateAttributes) {
        const name = nv.name;

        for (const value of nv.values) {
          if (value.length > 0) {
            if (name === 'SAN') {
              // handle SANS specially, see below
              continue;
            }

            if (dnLines.length > 0) {
              dnLines += ', ';
            }
            dnLines += name + '=' + value;
          }
        }
      }

      reqConf = '[NewRequest]\n';
      reqConf += 'Subject = "' + dnLines + '"\n';
      reqConf += 'KeyLength = ' + keyLen + '\n';
      reqConf += 'HashAlgorithm = ' + hashAlgo + '\n';
      reqConf += 'FriendlyName = ' + fileName + '\n';

      if (hasSAN) {
        reqConf += '[Extensions]\n';
        reqConf += '2.5.29.17 = "{text}"\n';

        for (const value of nvSAN.values) {
          const parts = value.split(':', 2);
          let type = 'dns';
          let sanValue = value;
          window.console.info('parts.length : ' + parts.length);

          if (parts.length < 2) {
            // defaults match
          } else if (parts[0].toUpperCase().trim() === 'DNS') {
            sanValue = parts[1];
          } else {
            type = 'IP';
            sanValue = parts[1];
          }

          if (value.length > 0) {
            reqConf += '_continue_ = "' + type + '=' + sanValue + '"&\n';
          }
        }
      }

      this.reqConf = reqConf;
      return cmdline;
    } else {
      //
      // openssl
      //
      cmdline = this.getOpensslCommon(cmdline, algo, hashAlgo, keyLen, false);

      cmdline += ' -config request.conf -keyout ' + fileName + '.private_key.pem -out ' + fileName + '.csr';

      this.reqConfRequired = true;

      let dnLines = '';

      for (const nv of this.upload.certificateAttributes) {
        const name = nv.name;

        for (const value of nv.values) {
          if (value.length > 0) {
            if (name === 'SAN') {
              // handle SANS specially, see below
              continue;
            }
            if ('E' === name.toUpperCase()) {
              dnLines += 'emailAddress=' + value + '\n';
            } else {
              dnLines += name + '=' + value + '\n';
            }
          }
        }
      }
      reqConf = '[req]\n';
      reqConf += 'distinguished_name = req_distinguished_name\n';
      if (hasSAN) {
        // insert forward reference only if required
        reqConf += 'req_extensions = v3_req\n';
      }
      reqConf += 'prompt = no\n';
      reqConf += '[req_distinguished_name]\n';
      reqConf += dnLines;

      if (hasSAN) {
        reqConf += '[v3_req]\n';
        reqConf += 'subjectAltName = @alt_names\n';
        reqConf += '[alt_names]\n';

        let dnsNo = 1;
        let ipNo = 1;
        for (const value of nvSAN.values) {
          const parts = value.split(':', 2);
          let idx = 1;
          let type = 'DNS';
          let sanValue = value;
          window.console.info('parts.length : ' + parts.length);

          if (parts.length < 2) {
            // defaults match
            idx = dnsNo;
            dnsNo++;
          } else if (parts[0].toUpperCase().trim() === 'DNS') {
            sanValue = parts[1];
            idx = dnsNo;
            dnsNo++;
          } else {
            type = 'IP';
            sanValue = parts[1];
            idx = ipNo;
            ipNo++;
          }

          if (value.length > 0) {
            reqConf += type + '.' + idx + ' = ' + sanValue + '\n';
          }
        }

        /*
        for (const nv of this.upload.certificateAttributes) {
          const name = nv.name;
          if (name === 'SAN') {
            for (const value of nv.values) {
              const parts = value.split(':', 2);
              let idx = 1;
              let type = 'DNS';
              let sanValue = value;
              window.console.info('parts.length : ' + parts.length);

              if (parts.length < 2) {
                // defaults match
                idx = dnsNo;
                dnsNo++;
              } else if (parts[0].toUpperCase().trim() === 'DNS') {
                sanValue = parts[1];
                idx = dnsNo;
                dnsNo++;
              } else {
                type = 'IP';
                sanValue = parts[1];
                idx = ipNo;
                ipNo++;
              }

              if (value.length > 0) {
                reqConf += type + '.' + idx + ' = ' + sanValue + '\n';
              }
            }
          }
        }
*/
      }
    }

    this.reqConf = reqConf;
    return cmdline;
  }

  private getOpensslCommon(cmdline: string, algo: string, hashAlgo: string, keyLen: string, addSubject: boolean) {
    cmdline = 'openssl req -newkey ' + algo + ':' + keyLen + ' -' + hashAlgo;
    cmdline += ' -nodes';

    if (addSubject) {
      cmdline += ' -subj ';
      let subject = '';
      for (const nv of this.upload.certificateAttributes) {
        const name = nv.name;
        if (name === 'SAN') {
          // handle SANS specially, see below
          continue;
        }
        for (const value of nv.values) {
          if (value.length > 0) {
            if ('E' === name.toUpperCase()) {
              subject += '/emailAddress=';
            } else {
              subject += '/' + name.toUpperCase() + '=';
            }
            const re = new RegExp(/\//, 'g');
            subject += value.replace(re, '\\/');
          }
        }
      }
      if (subject.length > 0) {
        cmdline += '"' + subject + '"';
      }
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
    readerBase64.onload = function(_result) {
      const base64Text = readerBase64.result.toString();

      // check, whether this is base64 encoded content
      if (/^[\x00-\x7F]*$/.test(base64Text)) {
        self.upload.content = base64Text;
        self.contentCall(precheckUrl);
      } else {
        // binary, start re-reading it as base64-encoded comntent
        const readerBinary = new FileReader();
        readerBinary.onload = function(__result) {
          self.upload.content = readerBinary.result.toString().split(',')[1];
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

    if (this.creationMode === 'CSR_AVAILABLE' && this.upload.content && this.upload.content.trim().length === 0) {
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

      if (this.precheckResponse && this.precheckResponse.dataType === 'CSR' && this.precheckResponse.csrPending) {
        this.$router.push({ name: 'CsrInfo', params: { csrId: this.precheckResponse.createdCSRId } });
      }

      if (this.precheckResponse && this.precheckResponse.dataType === 'X509_CERTIFICATE_CREATED' && this.precheckResponse.certificates[0]) {
        this.$router.push({ name: 'CertInfo', params: { certificateId: this.precheckResponse.certificates[0].certificateId.toString() } });
      }

      this.warnings = this.precheckResponse.warnings;

      if (
        this.precheckResponse &&
        this.precheckResponse.certificates &&
        this.precheckResponse.dataType === 'X509_CERTIFICATE' &&
        this.precheckResponse.certificates[0].pemCertrificate
      ) {
        this.upload.content = this.precheckResponse.certificates[0].pemCertrificate;

        for (const nv of this.upload.certificateAttributes) {
          if (nv.name === 'SAN') {
            nv.values = this.precheckResponse.certificates[0].sans;
          } else {
            for (const subjectPart of this.precheckResponse.certificates[0].subjectParts) {
              if (subjectPart.name.toUpperCase() === nv.name.toUpperCase()) {
                nv.values = subjectPart.values;
              }
            }
          }
        }
      }
      this.isChecked = true;
    } catch (error) {
      console.error('####################' + error);
      document.body.style.cursor = 'default';
      this.isChecked = false;
      this.responseStatus = error.response.status;

      const message = this.$t('problem processing request: ' + error);
      this.alertService().showAlert(message, 'info');
    }
  }

  public mounted(): void {
    this.getPreference();
    this.fillPipelineData();
  }

  public fillPipelineData(): void {
    window.console.info('calling fillPipelineData');
    const self = this;

    axios({
      method: 'get',
      url: 'api/pipeline/getActiveWebPipelines',
      responseType: 'stream'
    }).then(function(response) {
      window.console.info('getWebPipelines returns ' + response.data);
      self.upload.pipelineId = -1;
      self.allWebPipelines = response.data;

      /*
      if (self.allWebPipelines.length > 0) {
        self.upload.pipelineId = self.allWebPipelines[0].id;
        self.updatePipelineRestrictionsByPipelineInfo(self.allWebPipelines[0]);
      }

 */

      for (const pipeline of self.allWebPipelines) {
        window.console.info('pipeline.id: ' + pipeline.id + ' / self.preselectedPipeline : ' + self.preselectedPipelineId);
        if (pipeline.id === self.preselectedPipelineId) {
          self.updatePipelineRestrictionsByPipelineInfo(pipeline);
          self.upload.pipelineId = pipeline.id;
          break;
        }
      }
    });
  }

  public getPreference(): void {
    window.console.info('calling getPreference');
    const self = this;

    axios({
      method: 'get',
      url: 'api/admin/preference/1', // 1 represents system settings
      responseType: 'stream'
    }).then(function(response) {
      window.console.info('getPreference returns ' + response.data);
      self.preferences = response.data;
    });
  }

  public showCSRRelatedArea(): boolean {
    //    window.console.info('pipelineId : ' + this.upload.pipelineId );
    return this.precheckResponse && this.precheckResponse.dataType === 'CSR' && this.authenticated;
  }

  public showCertificateUpload(): boolean {
    if (
      this.precheckResponse &&
      (this.precheckResponse.dataType === 'X509_CERTIFICATE' || this.precheckResponse.dataType === 'CONTAINER') &&
      (this.isRAOfficer() || this.isAdmin())
    ) {
      return !this.precheckResponse.certificates[0].certificatePresentInDB;
    }
    return false;
  }

  public disableCertificateRequest(): boolean {
    window.console.info('in disableCertificateRequest()');

    if (this.creationMode === 'CSR_AVAILABLE') {
      // if the key is present in the DB, reject a CSR upload
      if (this.precheckResponse.csrPublicKeyPresentInDB) {
        return true;
      }
    } else if (this.creationMode === 'SERVERSIDE_KEY_CREATION') {
      if (this.secret.trim().length === 0) {
        window.console.info('upload.secret not present');
        return true;
      }

      //      window.console.info('upload.secret : "' + this.secret + '" , secretRepeat : "' + this.secretRepeat + '"');
      if (this.secret.trim() !== this.secretRepeat.trim()) {
        window.console.info('upload.secret does not match secretRepeat');
        return true;
      }
    }

    // the CSR does not fill the RDN values, don't check !!
    if (this.creationMode !== 'CSR_AVAILABLE') {
      for (let rdnIndex = 0; rdnIndex < this.rdnRestrictions.length; rdnIndex++) {
        for (let valueIndex = 0; valueIndex < this.upload.certificateAttributes[rdnIndex].values.length; valueIndex++) {
          if (
            this.showContentWarning(
              this.rdnRestrictions[rdnIndex],
              valueIndex,
              this.upload.certificateAttributes[rdnIndex].values[valueIndex]
            ) ||
            this.showContentOrSANWarning(
              this.rdnRestrictions[rdnIndex],
              valueIndex,
              this.upload.certificateAttributes[rdnIndex].values[valueIndex]
            ) ||
            this.showRegExpWarning(
              this.rdnRestrictions[rdnIndex],
              valueIndex,
              this.upload.certificateAttributes[rdnIndex].values[valueIndex]
            )
          ) {
            window.console.info('attribute "' + this.rdnRestrictions[rdnIndex].name + '" does not match requirements!');
            return true;
          }
        }
      }
    }

    for (let araIndex = 0; araIndex < this.araRestrictions.length; araIndex++) {
      if (
        this.showContentWarning(this.araRestrictions[araIndex], 0, this.upload.arAttributes[araIndex].values[0]) ||
        this.showRegExpWarning(this.araRestrictions[araIndex], 0, this.upload.arAttributes[araIndex].values[0])
      ) {
        window.console.info('attribute "' + this.araRestrictions[araIndex].name + '" does not match requirements!');
        return true;
      }
    }
    if (this.precheckResponse && this.precheckResponse.warnings && this.precheckResponse.warnings.length > 0) {
      window.console.info('precheckResponse.warnings present! Request disabled');
      return true;
    }

    return false;
  }

  public isRAOfficer() {
    return this.hasRole('ROLE_RA') || this.hasRole('ROLE_RA_DOMAIN');
  }

  public isAdmin() {
    return this.hasRole('ROLE_ADMIN');
  }

  public hasRole(targetRole: string) {
    for (const role of this.$store.getters.account.authorities) {
      if (targetRole === role) {
        return true;
      }
    }
    return false;
  }

  public get roles(): string {
    return this.$store.getters.account ? this.$store.getters.account.authorities[0] : '';
  }

  public isSANAllowed(): boolean {
    for (const nv of this.upload.certificateAttributes) {
      if (nv.name === 'SAN') {
        return true;
      }
    }
    return false;
  }

  public updateValue(key, value) {
    window.console.info('updateValue for ' + key);
    this.$emit('change', { ...this.upload, [key]: value });
  }
}
