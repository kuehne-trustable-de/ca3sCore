import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AcmeOrderService from '../acme-order/acme-order.service';
import { IAcmeOrder } from '@/shared/model/acme-order.model';

import AlertService from '@/shared/alert/alert.service';
import { IAcmeIdentifier, AcmeIdentifier } from '@/shared/model/acme-identifier.model';
import AcmeIdentifierService from './acme-identifier.service';

const validations: any = {
  acmeIdentifier: {
    acmeIdentifierId: {
      required,
      numeric,
    },
    type: {
      required,
    },
    value: {
      required,
    },
  },
};

@Component({
  validations,
})
export default class AcmeIdentifierUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('acmeIdentifierService') private acmeIdentifierService: () => AcmeIdentifierService;
  public acmeIdentifier: IAcmeIdentifier = new AcmeIdentifier();

  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;

  public acmeOrders: IAcmeOrder[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeIdentifierId) {
        vm.retrieveAcmeIdentifier(to.params.acmeIdentifierId);
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
    if (this.acmeIdentifier.id) {
      this.acmeIdentifierService()
        .update(this.acmeIdentifier)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeIdentifier.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.acmeIdentifierService()
        .create(this.acmeIdentifier)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeIdentifier.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveAcmeIdentifier(acmeIdentifierId): void {
    this.acmeIdentifierService()
      .find(acmeIdentifierId)
      .then(res => {
        this.acmeIdentifier = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.acmeOrderService()
      .retrieve()
      .then(res => {
        this.acmeOrders = res.data;
      });
  }
}
