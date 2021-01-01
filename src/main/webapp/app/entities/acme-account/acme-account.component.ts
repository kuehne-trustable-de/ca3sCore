import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IACMEAccount } from '@/shared/model/acme-account.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import JhiDataUtils from '@/shared/data/data-utils.service';

import ACMEAccountService from './acme-account.service';

@Component
export default class ACMEAccount extends mixins(JhiDataUtils, Vue2Filters.mixin, AlertMixin) {
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
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
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
