import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AuthorizationService from '../authorization/authorization.service';
import { IAuthorization } from '@/shared/model/authorization.model';

import IdentifierService from '../identifier/identifier.service';
import { IIdentifier } from '@/shared/model/identifier.model';

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
      numeric
    },
    status: {
      required
    },
    expires: {},
    notBefore: {},
    notAfter: {},
    error: {},
    finalizeUrl: {},
    certificateUrl: {}
  }
};

@Component({
  validations
})
export default class AcmeOrderUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;
  public acmeOrder: IAcmeOrder = new AcmeOrder();

  @Inject('authorizationService') private authorizationService: () => AuthorizationService;

  public authorizations: IAuthorization[] = [];

  @Inject('identifierService') private identifierService: () => IdentifierService;

  public identifiers: IIdentifier[] = [];

  @Inject('cSRService') private cSRService: () => CSRService;

  public cSRS: ICSR[] = [];

  @Inject('certificateService') private certificateService: () => CertificateService;

  public certificates: ICertificate[] = [];

  @Inject('aCMEAccountService') private aCMEAccountService: () => ACMEAccountService;

  public aCMEAccounts: IACMEAccount[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeOrderId) {
        vm.retrieveAcmeOrder(to.params.acmeOrderId);
      }
      vm.initRelationships();
    });
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

  public retrieveAcmeOrder(acmeOrderId): void {
    this.acmeOrderService()
      .find(acmeOrderId)
      .then(res => {
        this.acmeOrder = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.authorizationService()
      .retrieve()
      .then(res => {
        this.authorizations = res.data;
      });
    this.identifierService()
      .retrieve()
      .then(res => {
        this.identifiers = res.data;
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
