import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';
import format from 'date-fns/format';
import parse from 'date-fns/parse';
import parseISO from 'date-fns/parseISO';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import CSRService from '../csr/csr.service';
import { ICSR } from '@/shared/model/csr.model';

import CertificateAttributeService from '../certificate-attribute/certificate-attribute.service';
import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';

import CAConnectorConfigService from '../ca-connector-config/ca-connector-config.service';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';

import AlertService from '@/shared/alert/alert.service';
import { ICertificate, Certificate } from '@/shared/model/certificate.model';
import CertificateService from './certificate.service';

const validations: any = {
  certificate: {
    tbsDigest: {
      required,
    },
    subject: {
      required,
    },
    sans: {},
    issuer: {
      required,
    },
    root: {},
    type: {
      required,
    },
    description: {},
    fingerprint: {},
    serial: {
      required,
    },
    validFrom: {
      required,
    },
    validTo: {
      required,
    },
    keyAlgorithm: {},
    keyLength: {},
    curveName: {},
    hashingAlgorithm: {},
    paddingAlgorithm: {},
    signingAlgorithm: {},
    creationExecutionId: {},
    contentAddedAt: {},
    revokedSince: {},
    revocationReason: {},
    revoked: {},
    revocationExecutionId: {},
    administrationComment: {},
    endEntity: {},
    selfsigned: {},
    trusted: {},
    active: {},
    content: {
      required,
    },
  },
};

@Component({
  validations,
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

  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;

  public cAConnectorConfigs: ICAConnectorConfig[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.certificateId) {
        vm.retrieveCertificate(to.params.certificateId);
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

  public convertDateTimeFromServer(date: Date): string {
    if (date) {
      return format(date, DATE_TIME_LONG_FORMAT);
    }
    return null;
  }

  public updateInstantField(field, event) {
    if (event.target.value) {
      this.certificate[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.certificate[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.certificate[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.certificate[field] = null;
    }
  }

  public retrieveCertificate(certificateId): void {
    this.certificateService()
      .find(certificateId)
      .then(res => {
        res.validFrom = new Date(res.validFrom);
        res.validTo = new Date(res.validTo);
        res.contentAddedAt = new Date(res.contentAddedAt);
        res.revokedSince = new Date(res.revokedSince);
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
    this.certificateService()
      .retrieve()
      .then(res => {
        this.certificates = res.data;
      });
    this.cAConnectorConfigService()
      .retrieve()
      .then(res => {
        this.cAConnectorConfigs = res.data;
      });
  }
}
