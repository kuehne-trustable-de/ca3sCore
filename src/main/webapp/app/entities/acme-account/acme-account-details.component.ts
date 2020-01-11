import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { IACMEAccount } from '@/shared/model/acme-account.model';
import ACMEAccountService from './acme-account.service';

@Component
export default class ACMEAccountDetails extends mixins(JhiDataUtils) {
  @Inject('aCMEAccountService') private aCMEAccountService: () => ACMEAccountService;
  public aCMEAccount: IACMEAccount = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.aCMEAccountId) {
        vm.retrieveACMEAccount(to.params.aCMEAccountId);
      }
    });
  }

  public retrieveACMEAccount(aCMEAccountId) {
    this.aCMEAccountService()
      .find(aCMEAccountId)
      .then(res => {
        this.aCMEAccount = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
