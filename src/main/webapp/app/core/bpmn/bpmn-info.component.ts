import { Component, Inject, Vue } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

import BPNMProcessInfoService from '../../entities/bpnm-process-info/bpnm-process-info.service';
import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';

import VueBpmn from 'vue-bpmn';
import axios from 'axios';
import { IBpmnCheckResult, IBPMNUpload } from '@/shared/model/transfer-object.model';

@Component({
  components: {
    Fragment,
    CopyClipboardButton,
    HelpTag,
    AuditTag,
    VueBpmn
  }
})
export default class BpmnInfo extends mixins(JhiDataUtils, Vue) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;

  public bPNMProcessInfo: IBPMNProcessInfo = {};
  public bpmnUpload: IBPMNUpload = { type: 'CA_INVOCATION' };

  public bpmnCheckResult: IBpmnCheckResult = {};

  public bpmnUrl: string;
  public bpmnFileUploaded = false;
  public warningMessage: string = null;

  public csrId = 1;

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
        this.bpmnUpload.name = this.bPNMProcessInfo.name;
        this.bpmnUpload.type = this.bPNMProcessInfo.type;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public mounted(): void {
    window.console.info('in mounted()) ');
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

  public checkBpmn() {
    this.bpmnCheckResult = {};
    const self = this;

    const targetURL = `/api/bpmn/check/csr/${this.bPNMProcessInfo.processId}/${this.csrId}`;
    console.log('calling bpmn check endpoint at ' + targetURL);

    axios({
      method: 'post',
      url: targetURL
    }).then(function(response) {
      window.console.info('/bpmn/check/csr returns ' + response.data);
      self.bpmnCheckResult = response.data;
    });
  }

  public getBpmnUrl(): string {
    console.log('/api/bpmn/ called for ' + this.bPNMProcessInfo.processId);
    return '/api/bpmn/' + this.bPNMProcessInfo.processId;
  }

  public getOptions() {
    return {
      propertiesPanel: {},
      additionalModules: [],
      moddleExtensions: []
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
    readerContent.onload = function(_result) {
      console.log('uploaded bpmn content read');
      self.bpmnFileUploaded = false;
      self.bpmnUpload.contentXML = '';
      self.bpmnUrl = '';
      self.warningMessage = '';

      if (typeof readerContent.result === 'string') {
        self.bpmnUpload.contentXML = readerContent.result;
        console.log('uploaded bpmn read as XML: ' + readerContent.result);
      } else {
        console.error('uploaded bpmn reading URL: unexpected type ' + readerContent.result);
      }

      const readerUrl = new FileReader();
      readerUrl.onload = function(__result) {
        if (typeof readerUrl.result === 'string') {
          self.bpmnUrl = readerUrl.result;

          const name = evt.target.files[0].name;
          if (name.endsWith('.bpmn20.xml')) {
            name.substring(0, name.length - 11);
          }
          const lastDot = name.lastIndexOf('.');
          self.bpmnUpload.name = name.substring(0, lastDot);

          self.bpmnFileUploaded = true;
          console.log('uploaded bpmn read as URL: ' + self.bpmnUrl);
        } else {
          console.error('uploaded bpmn reading URL: unexpected type ' + readerUrl.result);
        }
      };

      readerUrl.readAsDataURL(evt.target.files[0]);
    };
    readerContent.readAsText(evt.target.files[0]);
  }
}
