import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';

import { ITenant } from '../../shared/model/tenant.model';

import TenantService from './tenant.service';
import { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class Tenant extends mixins(AlertMixin, Vue) {
  @Inject('tenantService') private tenantService: () => TenantService;

  private removeId: number = null;

  public tenants: ITenant[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllTenants();
  }

  public clear(): void {
    this.retrieveAllTenants();
  }

  public retrieveAllTenants(): void {
    const self = this;
    this.isFetching = true;
    this.tenantService()
      .retrieve()
      .then(
        res => {
          self.tenants = res.data;
          self.isFetching = false;
        },
        err => {
          self.isFetching = false;
          self.alertService().showAlert(err.response, 'warn');
        }
      );
  }

  public handleSyncList(): void {
    this.clear();
  }

  public prepareRemove(instance: ITenant): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeTenant(): void {
    const self = this;
    this.tenantService()
      .delete(this.removeId)
      .then(() => {
        const message = self.$t('ca3SApp.tenant.deleted', { param: self.removeId });
        self.alertService().showAlert(message, 'info');
        self.removeId = null;
        self.retrieveAllTenants();
        self.closeDialog();
      })
      .catch(error => {
        self.alertService().showAlert(error.response, 'error');
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
