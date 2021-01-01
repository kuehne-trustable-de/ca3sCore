import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAcmeAuthorization } from '@/shared/model/acme-authorization.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import AcmeAuthorizationService from './acme-authorization.service';

@Component
export default class AcmeAuthorization extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('acmeAuthorizationService') private acmeAuthorizationService: () => AcmeAuthorizationService;
  private removeId: number = null;

  public acmeAuthorizations: IAcmeAuthorization[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAcmeAuthorizations();
  }

  public clear(): void {
    this.retrieveAllAcmeAuthorizations();
  }

  public retrieveAllAcmeAuthorizations(): void {
    this.isFetching = true;

    this.acmeAuthorizationService()
      .retrieve()
      .then(
        res => {
          this.acmeAuthorizations = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IAcmeAuthorization): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeAcmeAuthorization(): void {
    this.acmeAuthorizationService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.acmeAuthorization.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllAcmeAuthorizations();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
