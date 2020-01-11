import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AuthorizationService from '../authorization/authorization.service';
import { IAuthorization } from '@/shared/model/authorization.model';

import AlertService from '@/shared/alert/alert.service';
import { IAcmeChallenge, AcmeChallenge } from '@/shared/model/acme-challenge.model';
import AcmeChallengeService from './acme-challenge.service';

const validations: any = {
  acmeChallenge: {
    challengeId: {
      required,
      numeric
    },
    type: {
      required
    },
    value: {
      required
    },
    token: {
      required
    },
    validated: {},
    status: {
      required
    }
  }
};

@Component({
  validations
})
export default class AcmeChallengeUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('acmeChallengeService') private acmeChallengeService: () => AcmeChallengeService;
  public acmeChallenge: IAcmeChallenge = new AcmeChallenge();

  @Inject('authorizationService') private authorizationService: () => AuthorizationService;

  public authorizations: IAuthorization[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeChallengeId) {
        vm.retrieveAcmeChallenge(to.params.acmeChallengeId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.acmeChallenge.id) {
      this.acmeChallengeService()
        .update(this.acmeChallenge)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeChallenge.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.acmeChallengeService()
        .create(this.acmeChallenge)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeChallenge.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveAcmeChallenge(acmeChallengeId): void {
    this.acmeChallengeService()
      .find(acmeChallengeId)
      .then(res => {
        this.acmeChallenge = res;
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
  }
}
