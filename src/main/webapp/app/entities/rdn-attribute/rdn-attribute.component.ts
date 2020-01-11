import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IRDNAttribute } from '@/shared/model/rdn-attribute.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import RDNAttributeService from './rdn-attribute.service';

@Component
export default class RDNAttribute extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('rDNAttributeService') private rDNAttributeService: () => RDNAttributeService;
  private removeId: number = null;
  public rDNAttributes: IRDNAttribute[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllRDNAttributes();
  }

  public clear(): void {
    this.retrieveAllRDNAttributes();
  }

  public retrieveAllRDNAttributes(): void {
    this.isFetching = true;

    this.rDNAttributeService()
      .retrieve()
      .then(
        res => {
          this.rDNAttributes = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IRDNAttribute): void {
    this.removeId = instance.id;
  }

  public removeRDNAttribute(): void {
    this.rDNAttributeService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.rDNAttribute.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllRDNAttributes();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
