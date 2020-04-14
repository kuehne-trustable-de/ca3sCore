import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { ICertificate } from '@/shared/model/certificate.model';
import CertificateService from '../../entities/certificate/certificate.service';

import axios from 'axios';

@Component
export default class CertificateDetails extends mixins(JhiDataUtils) {
  @Inject('certificateService') private certificateService: () => CertificateService;
  public certificate: ICertificate = {};

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
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
