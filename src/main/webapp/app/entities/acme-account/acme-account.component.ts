import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAcmeAccount } from '@/shared/model/acme-account.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import JhiDataUtils from '@/shared/data/data-utils.service';

import AcmeAccountService from './acme-account.service';

@Component
export default class AcmeAccount extends mixins(JhiDataUtils, Vue2Filters.mixin, AlertMixin) {
  @Inject('aCMEAccountService') private aCMEAccountService: () => AcmeAccountService;
  private removeId: number = null;
  public aCMEAccounts: IAcmeAccount[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAcmeAccounts();
  }

  public clear(): void {
    this.retrieveAllAcmeAccounts();
  }

  public retrieveAllAcmeAccounts(): void {
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

  public prepareRemove(instance: IAcmeAccount): void {
    this.removeId = instance.id;
  }

  public removeAcmeAccount(): void {
    this.aCMEAccountService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.aCMEAccount.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllAcmeAccounts();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
