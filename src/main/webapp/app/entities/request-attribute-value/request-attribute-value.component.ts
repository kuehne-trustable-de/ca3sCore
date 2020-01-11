import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IRequestAttributeValue } from '@/shared/model/request-attribute-value.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import RequestAttributeValueService from './request-attribute-value.service';

@Component
export default class RequestAttributeValue extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('requestAttributeValueService') private requestAttributeValueService: () => RequestAttributeValueService;
  private removeId: number = null;
  public requestAttributeValues: IRequestAttributeValue[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllRequestAttributeValues();
  }

  public clear(): void {
    this.retrieveAllRequestAttributeValues();
  }

  public retrieveAllRequestAttributeValues(): void {
    this.isFetching = true;

    this.requestAttributeValueService()
      .retrieve()
      .then(
        res => {
          this.requestAttributeValues = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IRequestAttributeValue): void {
    this.removeId = instance.id;
  }

  public removeRequestAttributeValue(): void {
    this.requestAttributeValueService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.requestAttributeValue.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllRequestAttributeValues();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
