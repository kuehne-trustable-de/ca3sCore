import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import CSRService from '../csr/csr.service';
import { ICSR } from '@/shared/model/csr.model';

import CertificateAttributeService from '../certificate-attribute/certificate-attribute.service';
import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';

import AlertService from '@/shared/alert/alert.service';
import { ICertificate, Certificate } from '@/shared/model/certificate.model';
import CertificateService from './certificate.service';

const validations: any = {
  certificate: {
    tbsDigest: {
      required
    },
    subject: {
      required
    },
    issuer: {
      required
    },
    type: {
      required
    },
    description: {},
    subjectKeyIdentifier: {},
    authorityKeyIdentifier: {},
    fingerprint: {},
    serial: {
      required
    },
    validFrom: {
      required
    },
    validTo: {
      required
    },
    creationExecutionId: {},
    contentAddedAt: {},
    revokedSince: {},
    revocationReason: {},
    revoked: {},
    revocationExecutionId: {},
    content: {
      required
    }
  }
};

@Component({
  validations
})
export default class CertificateUpdate extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('certificateService') private certificateService: () => CertificateService;
  public certificate: ICertificate = new Certificate();

  @Inject('cSRService') private cSRService: () => CSRService;

  public cSRS: ICSR[] = [];

  @Inject('certificateAttributeService') private certificateAttributeService: () => CertificateAttributeService;

  public certificateAttributes: ICertificateAttribute[] = [];

  public certificates: ICertificate[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.certificateId) {
        vm.retrieveCertificate(to.params.certificateId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.certificate.id) {
      this.certificateService()
        .update(this.certificate)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.certificate.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.certificateService()
        .create(this.certificate)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.certificate.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveCertificate(certificateId): void {
    this.certificateService()
      .find(certificateId)
      .then(res => {
        this.certificate = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.cSRService()
      .retrieve()
      .then(res => {
        this.cSRS = res.data;
      });
    this.certificateAttributeService()
      .retrieve()
      .then(res => {
        this.certificateAttributes = res.data;
      });
    this.certificateService()
      .retrieve()
      .then(res => {
        this.certificates = res.data;
      });
  }
}
