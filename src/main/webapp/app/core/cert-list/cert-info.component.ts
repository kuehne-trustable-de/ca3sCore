import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { ICertificate } from '@/shared/model/certificate.model';
import CertificateService from '../../entities/certificate/certificate.service';

import axios from 'axios';
import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';
import { ICertificateAdministrationData } from '@/shared/model/transfer-object.model';

@Component
export default class CertificateDetails extends mixins(JhiDataUtils) {
  @Inject('certificateService') private certificateService: () => CertificateService;
  public certificate: ICertificate = {};

  public certificateAdminData: ICertificateAdministrationData = {};

  public usage = '';

  public downloadUrl(): string {
    const url = '/publicapi/cert/' + this.certificate.id;
    window.console.info('downloadUrl() : ' + url);
    return url;
  }

  public downloadItem(extension: string, mimetype: string) {
    const url = '/publicapi/cert/' + this.certificate.id;
    axios.get(url, { responseType: 'blob', headers: { 'Accept': mimetype } })
      .then(response => {
        const blob = new Blob([response.data], { type: mimetype, endings: 'transparent'});
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = this.certificate.subject + extension;
        link.click();
        URL.revokeObjectURL(link.href);
      }).catch(console.error);
  }

  public sansOnly(attArr: ICertificateAttribute[]) {
    return attArr.filter(function(att) {
      return att.name === 'SAN';
    });
  }

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.certificateId) {
        vm.retrieveCertificate(to.params.certificateId);
      }
    });
  }

  public retrieveCertificate(certificateId) {
    this.certificateService()
      .find(certificateId)
      .then(res => {
        this.certificate = res;

        this.usage = this.getAttributeList( 'USAGE' );

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
    return !(this.certificate.revoked) &&
      ( this.certificate.validTo ) &&
//      ( this.certificate.validTo.getMilliseconds() < Date.now()) &&
      ( this.isRAOfficer() || this.isOwnCertificate() );
  }

  public isRAOfficer() {
      return this.roles === 'ROLE_RA';
  }

  public isOwnCertificate() {
      return this.certificate.csr && this.getUsername() === this.certificate.csr.requestedBy;
  }

  public revokeCertificate() {
    this.certificateAdminData.certificateId = this.certificate.id;
    this.sendAdministrationAction('api/administerCertificate');
  }

  public withdrawCertificate() {
    this.certificateAdminData.certificateId = this.certificate.id;
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

  getAttribute( attrName: string): string {

    for ( let i = 0; i < this.certificate.certificateAttributes.length; i++ ) {
      if ( this.certificate.certificateAttributes[i].name === attrName) {
        return this.certificate.certificateAttributes[i].value;
      }
    }
    return '';
  }

  getAttributeList( attrName: string): string {
    let ret = '';
    for ( let i = 0; i < this.certificate.certificateAttributes.length; i++ ) {
      if ( this.certificate.certificateAttributes[i].name === attrName) {
        if ( ret.length > 0) {
          ret += ', ';
        }
        ret += this.certificate.certificateAttributes[i].value;
      }
    }
    return ret;
  }
}
