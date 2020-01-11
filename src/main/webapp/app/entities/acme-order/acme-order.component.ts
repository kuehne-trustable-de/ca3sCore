import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAcmeOrder } from '@/shared/model/acme-order.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import AcmeOrderService from './acme-order.service';

@Component
export default class AcmeOrder extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;
  private removeId: number = null;
  public acmeOrders: IAcmeOrder[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAcmeOrders();
  }

  public clear(): void {
    this.retrieveAllAcmeOrders();
  }

  public retrieveAllAcmeOrders(): void {
    this.isFetching = true;

    this.acmeOrderService()
      .retrieve()
      .then(
        res => {
          this.acmeOrders = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IAcmeOrder): void {
    this.removeId = instance.id;
  }

  public removeAcmeOrder(): void {
    this.acmeOrderService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.acmeOrder.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllAcmeOrders();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
