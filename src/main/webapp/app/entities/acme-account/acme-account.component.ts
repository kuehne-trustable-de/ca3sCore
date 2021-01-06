import { mixins } from 'vue-class-component';

import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IACMEAccount } from '@/shared/model/acme-account.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import JhiDataUtils from '@/shared/data/data-utils.service';

import ACMEAccountService from './acme-account.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class ACMEAccount extends mixins(JhiDataUtils, AlertMixin) {
  @Inject('aCMEAccountService') private aCMEAccountService: () => ACMEAccountService;
  private removeId: number = null;
  public aCMEAccounts: IACMEAccount[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllACMEAccounts();
  }

  public clear(): void {
    this.retrieveAllACMEAccounts();
  }

  public retrieveAllACMEAccounts(): void {
    this.isFetching = true;

    this.aCMEAccountService()
      .retrieve()
      .then(
        res => {
          this.aCMEAccounts = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IACMEAccount): void {
    this.removeId = instance.id;
  }

  public removeACMEAccount(): void {
    this.aCMEAccountService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.aCMEAccount.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllACMEAccounts();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
