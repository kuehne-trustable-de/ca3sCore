import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAcmeIdentifier } from '@/shared/model/acme-identifier.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import AcmeIdentifierService from './acme-identifier.service';

@Component
export default class AcmeIdentifier extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('acmeIdentifierService') private acmeIdentifierService: () => AcmeIdentifierService;
  private removeId: number = null;

  public acmeIdentifiers: IAcmeIdentifier[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAcmeIdentifiers();
  }

  public clear(): void {
    this.retrieveAllAcmeIdentifiers();
  }

  public retrieveAllAcmeIdentifiers(): void {
    this.isFetching = true;

    this.acmeIdentifierService()
      .retrieve()
      .then(
        res => {
          this.acmeIdentifiers = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IAcmeIdentifier): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeAcmeIdentifier(): void {
    this.acmeIdentifierService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.acmeIdentifier.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllAcmeIdentifiers();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
