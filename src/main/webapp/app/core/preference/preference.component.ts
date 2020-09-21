import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';
import { IUserPreference, UserPreference } from '@/shared/model/user-preference.model';
import UserPreferenceService from '../../entities/user-preference/user-preference.service';


@Component
export default class Preference extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('userPreferenceService') private userPreferenceService: () => UserPreferenceService;
  public preference: IUserPreference = new UserPreference();

  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.userPreferenceId) {
        vm.retrieveUserPreference(to.params.userPreferenceId);
      }
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.preference.id) {
      this.userPreferenceService()
        .update(this.preference)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.userPreference.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.userPreferenceService()
        .create(this.preference)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.userPreference.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveUserPreference(preferenceId): void {
    this.userPreferenceService()
      .find(preferenceId)
      .then(res => {
        this.preference = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
