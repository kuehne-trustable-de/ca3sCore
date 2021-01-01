import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAuthorization } from '@/shared/model/authorization.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import AuthorizationService from './authorization.service';

@Component
export default class Authorization extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('authorizationService') private authorizationService: () => AuthorizationService;
  private removeId: number = null;

  public authorizations: IAuthorization[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAuthorizations();
  }

  public clear(): void {
    this.retrieveAllAuthorizations();
  }

  public retrieveAllAuthorizations(): void {
    this.isFetching = true;

    this.authorizationService()
      .retrieve()
      .then(
        res => {
          this.authorizations = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IAuthorization): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeAuthorization(): void {
    this.authorizationService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.authorization.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllAuthorizations();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
