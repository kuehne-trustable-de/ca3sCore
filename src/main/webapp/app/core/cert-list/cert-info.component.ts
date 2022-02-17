import { Component, Inject } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertMixin from '@/shared/alert/alert.mixin';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

import { ICertificateView } from '@/shared/model/transfer-object.model';
import CertificateViewService from '../../entities/certificate/certificate-view.service';

import axios, { AxiosError } from 'axios';
import { ICertificateAdministrationData } from '@/shared/model/transfer-object.model';

@Component({
  components: {
    Fragment,
    CopyClipboardButton,
    HelpTag,
    AuditTag
  }
})
export default class CertificateDetails extends mixins(AlertMixin, JhiDataUtils) {
  @Inject('certificateViewService') private certificateViewService: () => CertificateViewService;

  public certificateView: ICertificateView = {};
  public certificateAdminData: ICertificateAdministrationData = {};
  public p12Alias = 'alias';
  public downloadFormat = 'pkix';

  public getDownloadFilename(): string {
    let extension = '.crt';
    if (this.downloadFormat === 'pem') {
      extension = '.pem';
    } else if (this.downloadFormat === 'pemPart') {
      extension = '.part.pem';
    } else if (this.downloadFormat === 'pemFull') {
      extension = '.full.pem';
    }
    return this.certificateView.downloadFilename + extension;
  }

  public downloadItem() {
    const filename = this.getDownloadFilename();

    let url = '/publicapi/certPKIX/' + this.certificateView.id + '/' + filename;
    let mimetype = 'application/pkix-cert';

    if (this.downloadFormat === 'pem') {
      url = '/publicapi/certPEM/' + this.certificateView.id + '/' + filename;
      mimetype = 'application/pem-certificate';
    } else if (this.downloadFormat === 'pemPart') {
      url = '/publicapi/certPEMPart/' + this.certificateView.id + '/' + filename;
      mimetype = 'application/x-pem-certificate-chain';
    } else if (this.downloadFormat === 'pemFull') {
      url = '/publicapi/certPEMFull/' + this.certificateView.id + '/' + filename;
      mimetype = 'application/pem-certificate-chain';
    }

    this.download(url, filename, mimetype);
  }

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

  public downloadItem__(extension: string, mimetype: string) {
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
    const url =
      '/publicapi/keystore/' + this.certificateView.id + '/' + encodeURIComponent(filename) + '/' + encodeURIComponent(this.p12Alias);
    this.download(url, filename, mimetype);
  }

  public copyToClipboard(elementId) {
    /* Get the text field */
    const copyText = document.getElementById(elementId) as HTMLInputElement;

    /* Select the text field */
    copyText.select();
    copyText.setSelectionRange(0, 99999); /* For mobile devices */

    /* Copy the text inside the text field */
    document.execCommand('copy');
  }

  public download(url: string, filename: string, mimetype: string) {
    const self = this;
    axios
      .get(url, { responseType: 'blob', headers: { Accept: mimetype } })
      .then(response => {
        const blob = new Blob([response.data], { type: mimetype, endings: 'transparent' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.type = mimetype;

        window.console.info('tmp download lnk : ' + link.download);

        link.click();
        URL.revokeObjectURL(link.href);
      })
      .catch(function(error) {
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

  beforeRouteEnter(to, from, next) {
    next(vm => {
      window.console.info('################ to.params : ' + to.params.certificateId);
      if (to.params.certificateId) {
        vm.retrieveCertificate(to.params.certificateId);
      }
    });
  }

  public mounted(): void {
    window.console.info('++++++++++++++++++ route.query : ' + this.$route.query.certificateId);
    if (this.$route.query.certificateId) {
      this.retrieveCertificate(this.$route.query.certificateId);
    }
  }

  public retrieveCertificate(certificateId) {
    this.certificateViewService()
      .find(certificateId)
      .then(res => {
        this.certificateView = res;
        this.certificateAdminData.arAttributes = this.certificateView.arArr;
        this.certificateAdminData.comment = this.certificateView.comment;
        this.certificateAdminData.trusted = this.certificateView.trusted;
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

  public isTrustable() {
    return this.isRAOrAdmin() && !this.certificateView.revoked && this.certificateView.selfsigned;
  }

  public isEditable() {
    return this.isRAOfficer() || this.isOwnCertificate();
  }

  public isRevocable() {
    return (
      !this.certificateView.revoked &&
      this.certificateView.validTo &&
      //      ( this.certificate.validTo.getMilliseconds() < Date.now()) &&
      (this.isRAOfficer() || this.isOwnCertificate())
    );
  }

  public isRemovableFromCRL() {
    return (
      this.certificateView.revocationReason === 'certificateHold' &&
      this.certificateView.validTo &&
      //      ( this.certificate.validTo.getMilliseconds() < Date.now()) &&
      (this.isRAOfficer() || this.isOwnCertificate())
    );
  }

  public isRAOfficer() {
    return this.hasRole('ROLE_RA');
  }

  public isAdmin() {
    return this.hasRole('ROLE_ADMIN');
  }

  public isRAOrAdmin() {
    return this.isRAOfficer() || this.isAdmin();
  }

  public hasRole(targetRole: string) {
    if (this.$store.getters.account.authorities === null) {
      return false;
    }

    for (const role of this.$store.getters.account.authorities) {
      if (targetRole === role) {
        return true;
      }
    }
    return false;
  }

  public isOwnCertificate() {
    return this.getUsername() === this.certificateView.requestedBy;
  }

  public updateCertificate() {
    this.certificateAdminData.certificateId = this.certificateView.id;
    this.certificateAdminData.administrationType = 'UPDATE';
    this.certificateAdminData.trusted = this.certificateView.trusted;
    this.sendAdministrationAction('api/administerCertificate');
  }

  public removeCertificateFromCRL() {
    this.certificateAdminData.certificateId = this.certificateView.id;
    this.certificateAdminData.revocationReason = 'removeFromCRL';
    this.certificateAdminData.administrationType = 'REVOKE';
    this.certificateAdminData.trusted = this.certificateView.trusted;
    this.sendAdministrationAction('api/administerCertificate');
  }

  public revokeCertificate() {
    this.certificateAdminData.certificateId = this.certificateView.id;
    this.certificateAdminData.administrationType = 'REVOKE';
    this.certificateAdminData.trusted = this.certificateView.trusted;
    this.sendAdministrationAction('api/administerCertificate');
  }

  public selfAdministerCertificate() {
    this.certificateAdminData.certificateId = this.certificateView.id;
    this.certificateAdminData.administrationType = 'UPDATE';
    this.certificateAdminData.trusted = this.certificateView.trusted;
    this.sendAdministrationAction('api/selfAdministerCertificate');
  }

  public withdrawCertificate() {
    this.certificateAdminData.certificateId = this.certificateView.id;
    this.certificateAdminData.administrationType = 'REVOKE';
    this.certificateAdminData.trusted = this.certificateView.trusted;
    this.sendAdministrationAction('api/withdrawOwnCertificate');
  }

  sendAdministrationAction(adminUrl: string) {
    document.body.style.cursor = 'wait';
    const self = this;

    if (this.certificateAdminData.trusted === null) {
      this.certificateAdminData.trusted = false;
    }

    axios({
      method: 'post',
      url: adminUrl,
      data: this.certificateAdminData,
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
        self.alertService().showAlert(message, 'info');
      })
      .then(function() {
        // always executed
        document.body.style.cursor = 'default';
      });
  }
}
