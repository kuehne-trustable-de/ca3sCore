import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AcmeChallengeService from '../acme-challenge/acme-challenge.service';
import { IAcmeChallenge } from '@/shared/model/acme-challenge.model';

import AcmeOrderService from '../acme-order/acme-order.service';
import { IAcmeOrder } from '@/shared/model/acme-order.model';

import AlertService from '@/shared/alert/alert.service';
import { IAuthorization, Authorization } from '@/shared/model/authorization.model';
import AuthorizationService from './authorization.service';

const validations: any = {
  authorization: {
    authorizationId: {
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
export default class AuthorizationUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('authorizationService') private authorizationService: () => AuthorizationService;
  public authorization: IAuthorization = new Authorization();

  @Inject('acmeChallengeService') private acmeChallengeService: () => AcmeChallengeService;

  public acmeChallenges: IAcmeChallenge[] = [];

  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;

  public acmeOrders: IAcmeOrder[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.authorizationId) {
        vm.retrieveAuthorization(to.params.authorizationId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.authorization.id) {
      this.authorizationService()
        .update(this.authorization)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.authorization.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.authorizationService()
        .create(this.authorization)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.authorization.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveAuthorization(authorizationId): void {
    this.authorizationService()
      .find(authorizationId)
      .then(res => {
        this.authorization = res;
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
