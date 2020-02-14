import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';
import format from 'date-fns/format';
import parse from 'date-fns/parse';
import parseISO from 'date-fns/parseISO';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import AcmeAuthorizationService from '../acme-authorization/acme-authorization.service';
import { IAcmeAuthorization } from '@/shared/model/acme-authorization.model';

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

  @Inject('acmeAuthorizationService') private acmeAuthorizationService: () => AcmeAuthorizationService;

  public acmeAuthorizations: IAcmeAuthorization[] = [];
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

  public convertDateTimeFromServer(date: Date): string {
    if (date) {
      return format(date, DATE_TIME_LONG_FORMAT);
    }
    return null;
  }

  public updateInstantField(field, event) {
    if (event.target.value) {
      this.acmeChallenge[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.acmeChallenge[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.acmeChallenge[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.acmeChallenge[field] = null;
    }
  }

  public retrieveAcmeChallenge(acmeChallengeId): void {
    this.acmeChallengeService()
      .find(acmeChallengeId)
      .then(res => {
        res.validated = new Date(res.validated);
        this.acmeChallenge = res;
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
  }
}
