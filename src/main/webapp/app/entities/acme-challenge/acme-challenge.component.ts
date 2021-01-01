import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAcmeChallenge } from '@/shared/model/acme-challenge.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import AcmeChallengeService from './acme-challenge.service';

@Component
export default class AcmeChallenge extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('acmeChallengeService') private acmeChallengeService: () => AcmeChallengeService;
  private removeId: number = null;

  public acmeChallenges: IAcmeChallenge[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAcmeChallenges();
  }

  public clear(): void {
    this.retrieveAllAcmeChallenges();
  }

  public retrieveAllAcmeChallenges(): void {
    this.isFetching = true;

    this.acmeChallengeService()
      .retrieve()
      .then(
        res => {
          this.acmeChallenges = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IAcmeChallenge): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeAcmeChallenge(): void {
    this.acmeChallengeService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.acmeChallenge.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllAcmeChallenges();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
