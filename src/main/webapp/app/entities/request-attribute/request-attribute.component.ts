import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IRequestAttribute } from '@/shared/model/request-attribute.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import RequestAttributeService from './request-attribute.service';

@Component
export default class RequestAttribute extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('requestAttributeService') private requestAttributeService: () => RequestAttributeService;
  private removeId: number = null;

  public requestAttributes: IRequestAttribute[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllRequestAttributes();
  }

  public clear(): void {
    this.retrieveAllRequestAttributes();
  }

  public retrieveAllRequestAttributes(): void {
    this.isFetching = true;

    this.requestAttributeService()
      .retrieve()
      .then(
        res => {
          this.requestAttributes = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IRequestAttribute): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeRequestAttribute(): void {
    this.requestAttributeService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.requestAttribute.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllRequestAttributes();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
