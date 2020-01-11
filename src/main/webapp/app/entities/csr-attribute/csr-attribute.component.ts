import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { ICsrAttribute } from '@/shared/model/csr-attribute.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import CsrAttributeService from './csr-attribute.service';

@Component
export default class CsrAttribute extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('csrAttributeService') private csrAttributeService: () => CsrAttributeService;
  private removeId: number = null;
  public csrAttributes: ICsrAttribute[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllCsrAttributes();
  }

  public clear(): void {
    this.retrieveAllCsrAttributes();
  }

  public retrieveAllCsrAttributes(): void {
    this.isFetching = true;

    this.csrAttributeService()
      .retrieve()
      .then(
        res => {
          this.csrAttributes = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: ICsrAttribute): void {
    this.removeId = instance.id;
  }

  public removeCsrAttribute(): void {
    this.csrAttributeService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.csrAttribute.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllCsrAttributes();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
