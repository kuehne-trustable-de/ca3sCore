import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';
import { IUserPreference, UserPreference } from '@/shared/model/user-preference.model';
import UserPreferenceService from './user-preference.service';

const validations: any = {
  userPreference: {
    userId: {
      required,
      numeric
    },
    name: {
      required
    },
    content: {
      required
    }
  }
};

@Component({
  validations
})
export default class UserPreferenceUpdate extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('userPreferenceService') private userPreferenceService: () => UserPreferenceService;
  public userPreference: IUserPreference = new UserPreference();
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
    if (this.userPreference.id) {
      this.userPreferenceService()
        .update(this.userPreference)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.userPreference.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.userPreferenceService()
        .create(this.userPreference)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.userPreference.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveUserPreference(userPreferenceId): void {
    this.userPreferenceService()
      .find(userPreferenceId)
      .then(res => {
        this.userPreference = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
