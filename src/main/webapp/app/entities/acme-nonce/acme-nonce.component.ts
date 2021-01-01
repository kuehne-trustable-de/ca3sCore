import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAcmeNonce } from '@/shared/model/acme-nonce.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import AcmeNonceService from './acme-nonce.service';

@Component
export default class AcmeNonce extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('acmeNonceService') private acmeNonceService: () => AcmeNonceService;
  private removeId: number = null;

  public acmeNonces: IAcmeNonce[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAcmeNonces();
  }

  public clear(): void {
    this.retrieveAllAcmeNonces();
  }

  public retrieveAllAcmeNonces(): void {
    this.isFetching = true;

    this.acmeNonceService()
      .retrieve()
      .then(
        res => {
          this.acmeNonces = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IAcmeNonce): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeAcmeNonce(): void {
    this.acmeNonceService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.acmeNonce.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllAcmeNonces();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
