import { Component, Vue, Inject } from 'vue-property-decorator';

import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';
import CertificateAttributeService from './certificate-attribute.service';

@Component
export default class CertificateAttributeDetails extends Vue {
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
