import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import CertificateService from '../certificate/certificate.service';
import { ICertificate } from '@/shared/model/certificate.model';

import AlertService from '@/shared/alert/alert.service';
import { ICertificateAttribute, CertificateAttribute } from '@/shared/model/certificate-attribute.model';
import CertificateAttributeService from './certificate-attribute.service';

const validations: any = {
  certificateAttribute: {
    name: {
      required,
    },
    value: {},
  },
};

@Component({
  validations,
})
export default class CertificateAttributeUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('certificateAttributeService') private certificateAttributeService: () => CertificateAttributeService;
  public certificateAttribute: ICertificateAttribute = new CertificateAttribute();

  @Inject('certificateService') private certificateService: () => CertificateService;

  public certificates: ICertificate[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.certificateAttributeId) {
        vm.retrieveCertificateAttribute(to.params.certificateAttributeId);
      }
      vm.initRelationships();
    });
  }

  created(): void {
    this.currentLanguage = this.$store.getters.currentLanguage;
    this.$store.watch(
      () => this.$store.getters.currentLanguage,
      () => {
        this.currentLanguage = this.$store.getters.currentLanguage;
      }
    );
  }

  public save(): void {
    this.isSaving = true;
    if (this.certificateAttribute.id) {
      this.certificateAttributeService()
        .update(this.certificateAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.certificateAttribute.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.certificateAttributeService()
        .create(this.certificateAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.certificateAttribute.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveCertificateAttribute(certificateAttributeId): void {
    this.certificateAttributeService()
      .find(certificateAttributeId)
      .then(res => {
        this.certificateAttribute = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.certificateService()
      .retrieve()
      .then(res => {
        this.certificates = res.data;
      });
  }
}
