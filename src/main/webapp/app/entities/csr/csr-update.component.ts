import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';
import format from 'date-fns/format';
import parse from 'date-fns/parse';
import parseISO from 'date-fns/parseISO';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import RDNService from '../rdn/rdn.service';
import { IRDN } from '@/shared/model/rdn.model';

import RequestAttributeService from '../request-attribute/request-attribute.service';
import { IRequestAttribute } from '@/shared/model/request-attribute.model';

import CsrAttributeService from '../csr-attribute/csr-attribute.service';
import { ICsrAttribute } from '@/shared/model/csr-attribute.model';

import PipelineService from '../pipeline/pipeline.service';
import { IPipeline } from '@/shared/model/pipeline.model';

import CertificateService from '../certificate/certificate.service';
import { ICertificate } from '@/shared/model/certificate.model';

import AlertService from '@/shared/alert/alert.service';
import { ICSR, CSR } from '@/shared/model/csr.model';
import CSRService from './csr.service';

const validations: any = {
  cSR: {
    csrBase64: {
      required
    },
    requestedOn: {
      required
    },
    status: {
      required
    },
    processInstanceId: {},
    signingAlgorithm: {},
    isCSRValid: {},
    x509KeySpec: {},
    publicKeyAlgorithm: {},
    publicKeyHash: {},
    subjectPublicKeyInfoBase64: {
      required
    }
  }
};

@Component({
  validations
})
export default class CSRUpdate extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('cSRService') private cSRService: () => CSRService;
  public cSR: ICSR = new CSR();

  @Inject('rDNService') private rDNService: () => RDNService;

  public rDNS: IRDN[] = [];

  @Inject('requestAttributeService') private requestAttributeService: () => RequestAttributeService;

  public requestAttributes: IRequestAttribute[] = [];

  @Inject('csrAttributeService') private csrAttributeService: () => CsrAttributeService;

  public csrAttributes: ICsrAttribute[] = [];

  @Inject('pipelineService') private pipelineService: () => PipelineService;

  public pipelines: IPipeline[] = [];

  @Inject('certificateService') private certificateService: () => CertificateService;

  public certificates: ICertificate[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.cSRId) {
        vm.retrieveCSR(to.params.cSRId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.cSR.id) {
      this.cSRService()
        .update(this.cSR)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.cSR.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.cSRService()
        .create(this.cSR)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.cSR.created', { param: param.id });
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
      this.cSR[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.cSR[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.cSR[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.cSR[field] = null;
    }
  }

  public retrieveCSR(cSRId): void {
    this.cSRService()
      .find(cSRId)
      .then(res => {
        res.requestedOn = new Date(res.requestedOn);
        this.cSR = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.rDNService()
      .retrieve()
      .then(res => {
        this.rDNS = res.data;
      });
    this.requestAttributeService()
      .retrieve()
      .then(res => {
        this.requestAttributes = res.data;
      });
    this.csrAttributeService()
      .retrieve()
      .then(res => {
        this.csrAttributes = res.data;
      });
    this.pipelineService()
      .retrieve()
      .then(res => {
        this.pipelines = res.data;
      });
    this.certificateService()
      .retrieve()
      .then(res => {
        this.certificates = res.data;
      });
  }
}
