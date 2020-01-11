import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IIdentifier } from '@/shared/model/identifier.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import IdentifierService from './identifier.service';

@Component
export default class Identifier extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('identifierService') private identifierService: () => IdentifierService;
  private removeId: number = null;
  public identifiers: IIdentifier[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllIdentifiers();
  }

  public clear(): void {
    this.retrieveAllIdentifiers();
  }

  public retrieveAllIdentifiers(): void {
    this.isFetching = true;

    this.identifierService()
      .retrieve()
      .then(
        res => {
          this.identifiers = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IIdentifier): void {
    this.removeId = instance.id;
  }

  public removeIdentifier(): void {
    this.identifierService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.identifier.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllIdentifiers();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
