import Component from 'vue-class-component';
import { Inject, Vue } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';

import AlertMixin from '@/shared/alert/alert.mixin';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

import BPNMProcessInfoService from '../../entities/bpnm-process-info/bpnm-process-info.service';
import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';

import VueBpmn from 'vue-bpmn';
import axios, { AxiosError } from 'axios';
import { IBpmnCheckResult, IBPMNUpload } from '@/shared/model/transfer-object.model';

const bpmnUrl = 'api/bpmn';

// const semVerRegEx ='^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$\n';
const semVerRegEx = '^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$';

@Component({
  components: {
    CopyClipboardButton,
    HelpTag,
    AuditTag,
    VueBpmn,
  },
})
export default class BpmnInfo extends mixins(AlertMixin, Vue) {
  //  @Inject('alertService') private alertService: () => AlertService;
  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;

  public bPNMProcessInfo: IBPMNProcessInfo = {};
  public bpmnUpload: IBPMNUpload = { type: 'CA_INVOCATION' };

  public bpmnCheckResult: IBpmnCheckResult = {};

  public updateMethod: string;
  public bpmnBlobUrl: string = null;
  public bpmnFileUploaded = false;

  // strange hack, to ensure that the update process works
  public bpmnUploadedVersion = '';
  public warningMessage: string = null;

  public csrId = 1;
  public certificateId = 1;

  public options: {
    propertiesPanel: {};
    additionalModules: [];
    moddleExtensions: [];
  };

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.bpmnId) {
        vm.retrieveBpmnInfo(to.params.bpmnId);
      }
    });
  }

  public retrieveBpmnInfo(bpmnId) {
    this.bPNMProcessInfoService()
      .find(bpmnId)
      .then(res => {
        this.bPNMProcessInfo = res;
        this.getBpmnUrl(this.bPNMProcessInfo.processId);
        this.bpmnUpload.id = this.bPNMProcessInfo.id;
        this.bpmnUpload.name = this.bPNMProcessInfo.name;
        this.bpmnUpload.type = this.bPNMProcessInfo.type;
        this.bpmnUpload.version = this.bPNMProcessInfo.version;
        this.bpmnUploadedVersion = this.bPNMProcessInfo.version;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public mounted(): void {
    window.console.info('in mounted()) ');
  }

  public showSemVerRegExpFieldWarning(value: string): boolean {
    const regexp = new RegExp(semVerRegEx);
    const valid = regexp.test(value);
    console.log('showRegExpFieldWarning( ' + semVerRegEx + ', "' + value + '") -> ' + valid);
    return !valid;
  }

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
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

  public getUsername(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public saveBpmn(): void {
    const self = this;

    this.bpmnUpload.version = this.bpmnUploadedVersion;

    this.updateMethod = 'POST';
    if (this.bPNMProcessInfo.id) {
      this.updateMethod = 'PUT';
    }

    axios({
      method: this.updateMethod,
      url: bpmnUrl,
      data: this.bpmnUpload,
    })
      .then(function (response) {
        console.log(response.status);

        if (response.status === 200) {
          self.$router.push({ name: 'BpmnList', params: {} });
        } else {
          self.previousState();
        }
      })
      .catch(function (error) {
        console.log(error);
        self.previousState();
        const message = self.$t('problem processing request: ' + error);
        self.alertService().showAlert(message, 'info');
      });
  }

  public checkBpmn() {
    this.bpmnCheckResult = {};
    const self = this;

    let targetURL = bpmnUrl + `/check/csr/${this.bPNMProcessInfo.processId}/${this.csrId}`;
    if (this.bpmnUpload.type === 'CERTIFICATE_NOTIFY') {
      targetURL = bpmnUrl + `/check/certificateNotify/${this.bPNMProcessInfo.processId}/${this.certificateId}`;
    } else if (this.bpmnUpload.type === 'ACME_ACCOUNT_AUTHORIZATION') {
      targetURL = bpmnUrl + `/check/accountRequest/${this.bPNMProcessInfo.processId}`;
    }

    console.log('calling bpmn check endpoint at ' + targetURL);

    axios({
      method: 'post',
      url: targetURL,
    })
      .then(function (response) {
        window.console.info(targetURL + ' returns ' + response.data);
        self.bpmnCheckResult = response.data;
      })
      .catch(function (error) {
        console.log(error);
        const message = self.$t('problem processing BPMN: ' + error);
        self.alertService().showAlert(message, 'info');
      });
  }

  public getBpmnUrl(bpmnId): void {
    if (bpmnId) {
      console.log(bpmnUrl + ' called for ' + bpmnId);
      const self = this;

      axios
        .get(bpmnUrl + '/' + bpmnId, { responseType: 'blob' })
        .then(function (response) {
          const blob = new Blob([response.data], {});

          self.bpmnBlobUrl = URL.createObjectURL(blob);
          window.console.info('tmp download url : ' + self.bpmnBlobUrl);
        })
        .catch(function (error) {
          console.log(error);
          const message = self.$t('problem processing request: ' + error);

          const err = error as AxiosError;
          if (err.response) {
            console.log(err.response.status);
            console.log(err.response.data);
            if (err.response.status === 401) {
              self.alertService().showAlert('Action not allowed', 'warn');
            } else {
              self.alertService().showAlert(message, 'info');
            }
          } else {
            self.alertService().showAlert(message, 'info');
          }
          self.getAlertFromStore();
        });
    }
  }

  public getOptions() {
    return {
      propertiesPanel: {},
      additionalModules: [],
      moddleExtensions: [],
    };
  }

  public handleError(err) {
    console.error('failed to show diagram', err);
  }
  public handleShown() {
    console.log('diagram shown');
  }
  public handleLoading() {
    console.log('diagram loading');
  }

  public notifyFileChange(evt: any): void {
    if (!evt || !evt.target || !evt.target.files || evt.target.files.length === 0) {
      return;
    }

    this.warningMessage = null;

    const self = this;

    const readerContent = new FileReader();
    readerContent.onload = function (_result) {
      console.log('uploaded bpmn content read');
      self.bpmnFileUploaded = false;
      self.bpmnUpload.contentXML = '';
      self.bpmnBlobUrl = '';
      self.warningMessage = '';

      if (typeof readerContent.result === 'string') {
        self.bpmnUpload.contentXML = readerContent.result;
        console.log('uploaded bpmn read as XML: ' + readerContent.result);
      } else {
        console.error('uploaded bpmn reading URL: unexpected type ' + readerContent.result);
      }

      const readerUrl = new FileReader();
      readerUrl.onload = function (__result) {
        if (typeof readerUrl.result === 'string') {
          self.bpmnBlobUrl = readerUrl.result;

          const name = evt.target.files[0].name;
          if (name.endsWith('.bpmn20.xml')) {
            name.substring(0, name.length - 11);
          }
          const lastDot = name.lastIndexOf('.');
          self.bpmnUpload.name = name.substring(0, lastDot);

          self.bpmnFileUploaded = true;

          const version = self.bpmnUploadedVersion;
          const lastVersionDot = version.lastIndexOf('.');
          const currentPatchLevel: number = parseInt(version.substring(lastVersionDot + 1));
          self.bpmnUploadedVersion = version.substring(0, lastVersionDot + 1) + (currentPatchLevel + 1);

          console.log('uploaded bpmn read as URL: ' + self.bpmnBlobUrl);
        } else {
          console.error('uploaded bpmn reading URL: unexpected type ' + readerUrl.result);
        }
      };

      readerUrl.readAsDataURL(evt.target.files[0]);
    };
    readerContent.readAsText(evt.target.files[0]);
  }
}
