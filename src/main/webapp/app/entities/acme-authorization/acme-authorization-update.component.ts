import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AcmeChallengeService from '../acme-challenge/acme-challenge.service';
import { IAcmeChallenge } from '@/shared/model/acme-challenge.model';

import AcmeOrderService from '../acme-order/acme-order.service';
import { IAcmeOrder } from '@/shared/model/acme-order.model';

import AlertService from '@/shared/alert/alert.service';
import { IAcmeAuthorization, AcmeAuthorization } from '@/shared/model/acme-authorization.model';
import AcmeAuthorizationService from './acme-authorization.service';

const validations: any = {
  acmeAuthorization: {
    acmeAuthorizationId: {
      required,
      numeric
    },
    type: {
      required
    },
    value: {
      required
    }
  }
};

@Component({
  validations
})
export default class AcmeAuthorizationUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('acmeAuthorizationService') private acmeAuthorizationService: () => AcmeAuthorizationService;
  public acmeAuthorization: IAcmeAuthorization = new AcmeAuthorization();

  @Inject('acmeChallengeService') private acmeChallengeService: () => AcmeChallengeService;

  public acmeChallenges: IAcmeChallenge[] = [];

  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;

  public acmeOrders: IAcmeOrder[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeAuthorizationId) {
        vm.retrieveAcmeAuthorization(to.params.acmeAuthorizationId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.acmeAuthorization.id) {
      this.acmeAuthorizationService()
        .update(this.acmeAuthorization)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeAuthorization.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.acmeAuthorizationService()
        .create(this.acmeAuthorization)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeAuthorization.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveAcmeAuthorization(acmeAuthorizationId): void {
    this.acmeAuthorizationService()
      .find(acmeAuthorizationId)
      .then(res => {
        this.acmeAuthorization = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.acmeChallengeService()
      .retrieve()
      .then(res => {
        this.acmeChallenges = res.data;
      });
    this.acmeOrderService()
      .retrieve()
      .then(res => {
        this.acmeOrders = res.data;
      });
  }
}
