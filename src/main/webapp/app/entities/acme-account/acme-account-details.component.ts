import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { IAcmeAccount } from '@/shared/model/acme-account.model';
import AcmeAccountService from './acme-account.service';

@Component
export default class AcmeAccountDetails extends mixins(JhiDataUtils) {
  @Inject('aCMEAccountService') private aCMEAccountService: () => AcmeAccountService;
  public aCMEAccount: IAcmeAccount = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.aCMEAccountId) {
        vm.retrieveAcmeAccount(to.params.aCMEAccountId);
      }
    });
  }

  public retrieveAcmeAccount(aCMEAccountId) {
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
