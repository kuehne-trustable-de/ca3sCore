import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { ICertificateView } from '@/shared/model/transfer-object.model';
import CertificateViewService from '../../entities/certificate/certificate-view.service';

import axios from 'axios';
import { ICertificateAdministrationData } from '@/shared/model/transfer-object.model';

@Component
export default class CertificateDetails extends mixins(JhiDataUtils) {
  @Inject('certificateViewService') private certificateViewService: () => CertificateViewService;

  public certificateView: ICertificateView = {};
  public certificateAdminData: ICertificateAdministrationData = {};

  public downloadUrl(): string {
    const url = '/publicapi/cert/' + this.certificateView.id;
    window.console.info('downloadUrl() : ' + url);
    return url;
  }

  public downloadItem(extension: string, mimetype: string) {
    const url = '/publicapi/cert/' + this.certificateView.id;
    axios.get(url, { responseType: 'blob', headers: { 'Accept': mimetype } })
      .then(response => {
        const blob = new Blob([response.data], { type: mimetype, endings: 'transparent'});
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = this.certificateView.downloadFilename + extension;
        link.click();
        URL.revokeObjectURL(link.href);
      }).catch(console.error);
  }

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.certificateId) {
        vm.retrieveCertificate(to.params.certificateId);
      }
    });
  }

  public retrieveCertificate(certificateId) {
    this.certificateViewService()
      .find(certificateId)
      .then(res => {
        this.certificateView = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public get roles(): string {
    return this.$store.getters.account ? this.$store.getters.account.authorities[0] : '';
  }

  public getUsername(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public isRevocable() {
    return !(this.certificateView.revoked) &&
      ( this.certificateView.validTo ) &&
//      ( this.certificate.validTo.getMilliseconds() < Date.now()) &&
      ( this.isRAOfficer() || this.isOwnCertificate() );
  }

  public isRAOfficer() {
      return this.roles === 'ROLE_RA';
  }

  public isOwnCertificate() {
      return this.getUsername() === this.certificateView.requestedBy;
  }

  public revokeCertificate() {
    this.certificateAdminData.certificateId = this.certificateView.id;
    this.sendAdministrationAction('api/administerCertificate');
  }

  public withdrawCertificate() {
    this.certificateAdminData.certificateId = this.certificateView.id;
    this.sendAdministrationAction('api/withdrawOwnCertificate');
  }

  sendAdministrationAction(adminUrl: string) {
    document.body.style.cursor = 'wait';
    const self = this;

    axios({
      method: 'post',
      url: adminUrl,
      data : this.certificateAdminData,
      responseType: 'stream'
    })
    .then(function(response) {
      console.log(response.status);

      if ( response.status === 201) {
        self.$router.push({name: 'CertInfo', params: {certificateId: response.data.toString()}});
      }
    }).catch(function(error) {
      console.log(error);
    }).then(function() {
      // always executed
      document.body.style.cursor = 'default';
      self.previousState();
    });
  }

}
