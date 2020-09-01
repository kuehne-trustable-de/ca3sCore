import { Component, Inject } from 'vue-property-decorator';

import axios from 'axios';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';

import { ICSRAdministrationData } from '@/shared/model/transfer-object.model';

import { ICSR } from '@/shared/model/csr.model';
import CSRService from '../../entities/csr/csr.service';
import { ICsrAttribute } from '@/shared/model/csr-attribute.model';

@Component
export default class CsrInfo extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('cSRService') private cSRService: () => CSRService;
  public cSR: ICSR = {};

  public csrAdminData: ICSRAdministrationData = {};

  public requestorComment = '';

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public get roles(): string {
    return this.$store.getters.account ? this.$store.getters.account.authorities[0] : '';
  }

  public getUsername(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.csrId) {
        vm.retrieveCsr(to.params.csrId);
      }
    });
  }

  public retrieveCsr(csrId) {
    this.cSRService()
      .find(csrId)
      .then(res => {
        this.cSR = res;
        window.console.info('csr :' + this.cSR.status );
      });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public mounted(): void {
    this.requestorComment = this.getRequestorComment();
  }

  public getRequestorComment(): string {

    if ( this.cSR.csrAttributes === undefined) {
      return '';
    }

    for ( let i = 0; i < this.cSR.csrAttributes.length; i++ ) {
      window.console.info('checking csrAttribute : ' + i );
      if ( this.cSR.csrAttributes[i].name === 'REQUESTOR_COMMENT' ) {
        return this.cSR.csrAttributes[i].value;
      }
    }
    return '';
  }

  public async withdrawCSR() {
    this.csrAdminData.csrId = this.cSR.id;
    this.csrAdminData.administrationType = 'REJECT';

    this.sendAdministrationAction('api/withdrawOwnRequest');
  }

  public async rejectCSR() {
    this.csrAdminData.csrId = this.cSR.id;
    this.csrAdminData.administrationType = 'REJECT';

    this.sendAdministrationAction('api/administerRequest');
  }

  public confirmCSR() {
    this.csrAdminData.csrId = this.cSR.id;
    this.csrAdminData.administrationType = 'ACCEPT';

    this.sendAdministrationAction('api/administerRequest');
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
      data : this.csrAdminData,
      responseType: 'stream'
    })
    .then(function(response) {
      console.log(response.status);

      if ( response.status === 201) {
        self.$router.push({name: 'CertInfo', params: {certificateId: response.data.toString()}});
      } else {
        self.previousState();
      }
    }).catch(function(error) {
      console.log(error);
      self.previousState();
      const message = self.$t('problem processing request: ' + error);
      self.alertService().showAlert(message, 'info');

    }).then(function() {
      // always executed
      document.body.style.cursor = 'default';
    });
  }
}
