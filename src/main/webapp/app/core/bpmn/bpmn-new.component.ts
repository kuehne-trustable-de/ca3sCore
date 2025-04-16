import { Component, Inject, Vue } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import { mixins } from 'vue-class-component';

import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';

import { IBPMNUpload } from '@/shared/model/transfer-object.model';

import BPNMProcessInfoService from '../../entities/bpnm-process-info/bpnm-process-info.service';

import VueBpmn from 'vue-bpmn';
import axios from 'axios';

const bpmnUrl = 'api/bpmn';

const semVerRegEx = '^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$';

@Component({
  components: {
    Fragment,
    CopyClipboardButton,
    HelpTag,
    VueBpmn,
  },
})
export default class BpmnInfo extends mixins(JhiDataUtils, Vue) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;

  public bpmnUpload: IBPMNUpload = {
    type: 'CERTIFICATE_NOTIFY',
    version: '0.0.1',
  };

  public bpmnUrl: string;
  public bpmnFileUploaded = false;
  public warningMessage: string = null;

  public options: {
    propertiesPanel: {};
    additionalModules: [];
    moddleExtensions: [];
  };

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

  public canCreateSecondFactor(secondFactorType: string): boolean {
    return this.$store.state.uiConfigStore.config.scndFactorTypes.includes(secondFactorType);
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
      self.bpmnUrl = '';
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

  public getOptions() {
    return {
      propertiesPanel: {},
      additionalModules: [],
      moddleExtensions: [],
    };
  }

  public handleError(err) {
    console.error('failed to show diagram', err);
    this.warningMessage = err;
  }
  public handleShown() {
    console.log('diagram shown');
  }
  public handleLoading() {
    console.log('diagram loading');
  }

  public saveBpmn(): void {
    document.body.style.cursor = 'wait';
    const self = this;

    axios({
      method: 'post',
      url: bpmnUrl,
      data: this.bpmnUpload,
    })
      .then(function (response) {
        console.log(response.status);

        if (response.status === 201) {
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
      })
      .then(function () {
        // always executed
        document.body.style.cursor = 'default';
      });
  }
}
