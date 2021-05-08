import { Component, Inject, Vue } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

import { ICSRAdministrationData, INamedValue } from '@/shared/model/transfer-object.model';

import BPNMProcessInfoService from '../../entities/bpnm-process-info/bpnm-process-info.service';
import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';

import VueBpmn from 'vue-bpmn';

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

  public csrAdminData: ICSRAdministrationData = {};

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
    return this.hasRole('ROLE_RA');
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

  public handleError(err) {
    console.error('failed to show diagram', err);
  }
  public handleShown() {
    console.log('diagram shown');
  }
  public handleLoading() {
    console.log('diagram loading');
  }

  public options: {
    propertiesPanel: {};
    additionalModules: [];
    moddleExtensions: [];
  };
}
