import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';
import CertificateAttributeService from './certificate-attribute.service';

@Component
export default class CertificateAttributeDetails extends mixins(JhiDataUtils) {
  @Inject('certificateAttributeService') private certificateAttributeService: () => CertificateAttributeService;
  public certificateAttribute: ICertificateAttribute = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.certificateAttributeId) {
        vm.retrieveCertificateAttribute(to.params.certificateAttributeId);
      }
    });
  }

  public retrieveCertificateAttribute(certificateAttributeId) {
    this.certificateAttributeService()
      .find(certificateAttributeId)
      .then(res => {
        this.certificateAttribute = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
