import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { INonce } from '@/shared/model/nonce.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import NonceService from './nonce.service';

@Component
export default class Nonce extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('nonceService') private nonceService: () => NonceService;
  private removeId: number = null;
  public nonces: INonce[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllNonces();
  }

  public clear(): void {
    this.retrieveAllNonces();
  }

  public retrieveAllNonces(): void {
    this.isFetching = true;

    this.nonceService()
      .retrieve()
      .then(
        res => {
          this.nonces = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: INonce): void {
    this.removeId = instance.id;
  }

  public removeNonce(): void {
    this.nonceService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.nonce.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllNonces();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
