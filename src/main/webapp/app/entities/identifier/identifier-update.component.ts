import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AcmeOrderService from '../acme-order/acme-order.service';
import { IAcmeOrder } from '@/shared/model/acme-order.model';

import AlertService from '@/shared/alert/alert.service';
import { IIdentifier, Identifier } from '@/shared/model/identifier.model';
import IdentifierService from './identifier.service';

const validations: any = {
  identifier: {
    identifierId: {
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
export default class IdentifierUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('identifierService') private identifierService: () => IdentifierService;
  public identifier: IIdentifier = new Identifier();

  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;

  public acmeOrders: IAcmeOrder[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.identifierId) {
        vm.retrieveIdentifier(to.params.identifierId);
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
    if (this.identifier.id) {
      this.identifierService()
        .update(this.identifier)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.identifier.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.identifierService()
        .create(this.identifier)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.identifier.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveIdentifier(identifierId): void {
    this.identifierService()
      .find(identifierId)
      .then(res => {
        this.identifier = res;
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
