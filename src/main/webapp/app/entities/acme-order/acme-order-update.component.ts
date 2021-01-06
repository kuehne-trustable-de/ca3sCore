import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';
import format from 'date-fns/format';
import parse from 'date-fns/parse';
import parseISO from 'date-fns/parseISO';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import AcmeAuthorizationService from '../acme-authorization/acme-authorization.service';
import { IAcmeAuthorization } from '@/shared/model/acme-authorization.model';

import AcmeIdentifierService from '../acme-identifier/acme-identifier.service';
import { IAcmeIdentifier } from '@/shared/model/acme-identifier.model';

import CSRService from '../csr/csr.service';
import { ICSR } from '@/shared/model/csr.model';

import CertificateService from '../certificate/certificate.service';
import { ICertificate } from '@/shared/model/certificate.model';

import ACMEAccountService from '../acme-account/acme-account.service';
import { IACMEAccount } from '@/shared/model/acme-account.model';

import AlertService from '@/shared/alert/alert.service';
import { IAcmeOrder, AcmeOrder } from '@/shared/model/acme-order.model';
import AcmeOrderService from './acme-order.service';

const validations: any = {
  acmeOrder: {
    orderId: {
      required,
      numeric,
    },
    status: {
      required,
    },
    expires: {},
    notBefore: {},
    notAfter: {},
    error: {},
    finalizeUrl: {},
    certificateUrl: {},
  },
};

@Component({
  validations,
})
export default class AcmeOrderUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;
  public acmeOrder: IAcmeOrder = new AcmeOrder();

  @Inject('acmeAuthorizationService') private acmeAuthorizationService: () => AcmeAuthorizationService;

  public acmeAuthorizations: IAcmeAuthorization[] = [];

  @Inject('acmeIdentifierService') private acmeIdentifierService: () => AcmeIdentifierService;

  public acmeIdentifiers: IAcmeIdentifier[] = [];

  @Inject('cSRService') private cSRService: () => CSRService;

  public cSRS: ICSR[] = [];

  @Inject('certificateService') private certificateService: () => CertificateService;

  public certificates: ICertificate[] = [];

  @Inject('aCMEAccountService') private aCMEAccountService: () => ACMEAccountService;

  public aCMEAccounts: IACMEAccount[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeOrderId) {
        vm.retrieveAcmeOrder(to.params.acmeOrderId);
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
    if (this.acmeOrder.id) {
      this.acmeOrderService()
        .update(this.acmeOrder)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeOrder.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.acmeOrderService()
        .create(this.acmeOrder)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeOrder.created', { param: param.id });
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
      this.acmeOrder[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.acmeOrder[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.acmeOrder[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.acmeOrder[field] = null;
    }
  }

  public retrieveAcmeOrder(acmeOrderId): void {
    this.acmeOrderService()
      .find(acmeOrderId)
      .then(res => {
        res.expires = new Date(res.expires);
        res.notBefore = new Date(res.notBefore);
        res.notAfter = new Date(res.notAfter);
        this.acmeOrder = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.acmeAuthorizationService()
      .retrieve()
      .then(res => {
        this.acmeAuthorizations = res.data;
      });
    this.acmeIdentifierService()
      .retrieve()
      .then(res => {
        this.acmeIdentifiers = res.data;
      });
    this.cSRService()
      .retrieve()
      .then(res => {
        this.cSRS = res.data;
      });
    this.certificateService()
      .retrieve()
      .then(res => {
        this.certificates = res.data;
      });
    this.aCMEAccountService()
      .retrieve()
      .then(res => {
        this.aCMEAccounts = res.data;
      });
  }
}
