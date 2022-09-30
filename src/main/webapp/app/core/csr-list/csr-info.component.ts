import { Component, Inject } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import axios, { AxiosError } from 'axios';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

import { ICSRAdministrationData, INamedValue, ICSRView } from '@/shared/model/transfer-object.model';

import ArItem from './ar-item.component';

import CSRViewService from '../../entities/csr/csr-view.service';
import { ICsrAttribute } from '@/shared/model/csr-attribute.model';
import AlertMixin from '@/shared/alert/alert.mixin';

@Component({
  components: {
    ArItem,
    Fragment,
    CopyClipboardButton,
    HelpTag,
    AuditTag
  }
})
export default class CsrInfo extends mixins(AlertMixin, JhiDataUtils) {
  @Inject('cSRViewService') private cSRViewService: () => CSRViewService;
  public icsrView: ICSRView = {};

  public csrAdminData: ICSRAdministrationData = {};

  public arAttributes: INamedValue[] = [];

  public requestorComment = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.csrId) {
        vm.retrieveCsr(to.params.csrId);
      }
    });
  }

  public retrieveCsr(csrId) {
    const self = this;
    this.cSRViewService()
      .find(csrId)
      .then(res => {
        self.icsrView = res;
        window.console.info('csr :' + self.icsrView.status);
        self.requestorComment = self.getRequestorComment();
        self.arAttributes = self.getArAttributes();
        self.csrAdminData.arAttributes = self.getArAttributes();
      });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public mounted(): void {
    window.console.info('in mounted()) ');
    this.requestorComment = this.getRequestorComment();
    this.arAttributes = this.getArAttributes();

    window.console.info('++++++++++++++++++ route.query : ' + this.$route.query.csrId);
    if (this.$route.query.csrId) {
      this.retrieveCsr(this.$route.query.csrId);
    }
  }

  public getRequestorComment(): string {
    return this.icsrView.requestorComment;

    /*
    if (this.icsrView.csrAttributes === undefined) {
      return '';
    }

    for (let i = 0; i < this.icsrView.csrAttributes.length; i++) {
      window.console.info('checking csrAttribute : ' + i);
      if (this.icsrView.csrAttributes[i].name === 'REQUESTOR_COMMENT') {
        return this.icsrView.csrAttributes[i].value;
      }
    }

 */
  }

  public getArAttributes(): INamedValue[] {
    return this.icsrView.arArr;
    /*
    const resultArr: INamedValue[] = new Array<INamedValue>();

    if (this.icsrView.arArr === undefined) {
      return resultArr;
    }

    for (let i = 0; i < this.icsrView.arArr.length; i++) {
      const attr = this.icsrView.arArr[i];
      if (attr.name.startsWith('_ARA_')) {
        const nv: INamedValue = {};
        nv.name = attr.name.substr(5);
        nv.value = attr.value;
        resultArr.push(nv);
      }
    }

    // retrieve all names
    const pas = this.cSR.pipeline.pipelineAttributes;
    for (let i = 0; i < this.cSR.csrAttributes.length; i++) {
      window.console.info('checking csrAttribute : ' + i);
      const attr = this.cSR.csrAttributes[i];
      for (let j = 0; j < pas.length; j++) {
        if (pas[j].name.startsWith('RESTR_ARA_') && pas[j].value === attr.name) {
          const nv: INamedValue = {};
          nv.name = attr.name;
          nv.value = '';
          resultArr.push(nv);
          break;
        }
      }
    }

    // insert all values
    for (let i = 0; i < this.cSR.csrAttributes.length; i++) {
      window.console.info('checking csrAttribute : ' + i);
      const attr = this.cSR.csrAttributes[i];
      for (let j = 0; j < resultArr.length; j++) {
        const nv = resultArr[j];
        if (nv.name === attr.name) {
          if (nv.value.length > 0) {
            nv.value += ',';
          }
          nv.value += attr.value;
        }
      }
    }
    return resultArr;
*/
  }

  public async withdrawCSR() {
    this.csrAdminData.csrId = this.icsrView.id;
    this.csrAdminData.administrationType = 'REJECT';

    this.sendAdministrationAction('api/withdrawOwnRequest');
  }

  public async rejectCSR() {
    this.csrAdminData.csrId = this.icsrView.id;
    this.csrAdminData.administrationType = 'REJECT';

    this.sendAdministrationAction('api/administerRequest');
  }

  public confirmCSR() {
    this.csrAdminData.csrId = this.icsrView.id;
    this.csrAdminData.administrationType = 'ACCEPT';

    this.sendAdministrationAction('api/administerRequest');
  }

  public updateCSR() {
    this.csrAdminData.csrId = this.icsrView.id;
    this.csrAdminData.administrationType = 'UPDATE';

    this.sendAdministrationAction('api/administerRequest');
  }

  public selfAdministerRequest() {
    this.csrAdminData.csrId = this.icsrView.id;
    this.csrAdminData.administrationType = 'UPDATE';

    this.sendAdministrationAction('api/selfAdministerRequest');
  }

  public sansOnly(attArr: ICsrAttribute[]) {
    return attArr.filter(function(att) {
      return att.name === 'SAN';
    });
  }

  sendAdministrationAction(adminUrl: string) {
    document.body.style.cursor = 'wait';
    const self = this;

    axios({
      method: 'post',
      url: adminUrl,
      data: this.csrAdminData,
      responseType: 'stream'
    })
      .then(function(response) {
        console.log(response.status);

        if (response.status === 201) {
          self.$router.push({ name: 'CertInfo', params: { certificateId: response.data.toString() } });
        } else {
          self.previousState();
        }
      })
      .catch(function(error) {
        console.log(error);
        self.previousState();
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
      })
      .then(function() {
        // always executed
        document.body.style.cursor = 'default';
      });
  }

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public isRAOfficer() {
    console.log('isRAOfficer: ' + this.icsrView.isAdministrable);
    return this.icsrView.isAdministrable;
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
}
