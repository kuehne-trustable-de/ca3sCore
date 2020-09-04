import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';

import { ICertificateView } from '@/shared/model/transfer-object.model';
import CertificateViewService from '../../entities/certificate/certificate-view.service';

import axios from 'axios';
import { ICertificateAdministrationData } from '@/shared/model/transfer-object.model';

@Component
export default class CertificateDetails extends mixins(JhiDataUtils) {
  @Inject('certificateViewService') private certificateViewService: () => CertificateViewService;
  @Inject('alertService') private alertService: () => AlertService;

  public certificateView: ICertificateView = {};
  public certificateAdminData: ICertificateAdministrationData = {};
  public p12Alias = 'alias';

  public downloadUrl(): string {
    const url = '/publicapi/cert/' + this.certificateView.id;
    window.console.info('downloadUrl() : ' + url);
    return url;
  }

  public downloadUrlDER(): string {
    const url = '/publicapi/certPKIX/' + this.certificateView.id + '/' + this.certificateView.downloadFilename + '.crt';
    window.console.info('downloadUrlDER() : ' + url);
    return url;
  }

  public downloadUrlPEM(): string {
    const url = '/publicapi/certPEM/' + this.certificateView.id + '/' + this.certificateView.downloadFilename + '.cer';
    window.console.info('downloadUrlPEM() : ' + url);
    return url;
  }

  public downloadItem(extension: string, mimetype: string) {
    const filename = this.certificateView.downloadFilename + extension;
    const url = '/publicapi/cert/' + this.certificateView.id;
    this.download(url, filename, mimetype);
  }

  public downloadPKIX(extension: string, mimetype: string) {
    const filename = this.certificateView.downloadFilename + extension;
    const url = '/publicapi/certPKIX/' + this.certificateView.id + '/' + encodeURIComponent(filename);
    this.download(url, filename, mimetype);
  }

  public downloadKeystore(extension: string, mimetype: string) {
    const filename = this.certificateView.downloadFilename + extension;
    const url = '/publicapi/keystore/' + this.certificateView.id + '/' + encodeURIComponent(filename) + '/' + encodeURIComponent(this.p12Alias);
    this.download(url, filename, mimetype);
  }

  public download(url: string, filename: string, mimetype: string) {
    axios.get(url, { responseType: 'blob', headers: { 'Accept': mimetype } })
      .then(response => {
        const blob = new Blob([response.data], { type: mimetype, endings: 'transparent'});
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.type = mimetype;

        window.console.info('tmp download lnk : ' + link.download);

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

  public isRemovableFromCRL() {
    return (this.certificateView.revocationReason	=== 'certificateHold') &&
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

  public removeCertificateFromCRL() {
    this.certificateAdminData.certificateId = this.certificateView.id;
    this.certificateAdminData.revocationReason = 'removeFromCRL';
    this.sendAdministrationAction('api/administerCertificate');
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
