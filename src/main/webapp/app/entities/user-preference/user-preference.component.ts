import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IUserPreference } from '@/shared/model/user-preference.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import JhiDataUtils from '@/shared/data/data-utils.service';

import UserPreferenceService from './user-preference.service';

@Component
export default class UserPreference extends mixins(JhiDataUtils, Vue2Filters.mixin, AlertMixin) {
  @Inject('userPreferenceService') private userPreferenceService: () => UserPreferenceService;
  private removeId: number = null;

  public userPreferences: IUserPreference[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllUserPreferences();
  }

  public clear(): void {
    this.retrieveAllUserPreferences();
  }

  public retrieveAllUserPreferences(): void {
    this.isFetching = true;

    this.userPreferenceService()
      .retrieve()
      .then(
        res => {
          this.userPreferences = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IUserPreference): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeUserPreference(): void {
    this.userPreferenceService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.userPreference.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllUserPreferences();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
