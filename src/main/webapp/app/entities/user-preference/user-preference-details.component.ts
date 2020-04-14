import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { IUserPreference } from '@/shared/model/user-preference.model';
import UserPreferenceService from './user-preference.service';

@Component
export default class UserPreferenceDetails extends mixins(JhiDataUtils) {
  @Inject('userPreferenceService') private userPreferenceService: () => UserPreferenceService;
  public userPreference: IUserPreference = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.userPreferenceId) {
        vm.retrieveUserPreference(to.params.userPreferenceId);
      }
    });
  }

  public retrieveUserPreference(userPreferenceId) {
    this.userPreferenceService()
      .find(userPreferenceId)
      .then(res => {
        this.userPreference = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
