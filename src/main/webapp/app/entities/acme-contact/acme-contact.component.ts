import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IAcmeContact } from '@/shared/model/acme-contact.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import AcmeContactService from './acme-contact.service';

@Component
export default class AcmeContact extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('acmeContactService') private acmeContactService: () => AcmeContactService;
  private removeId: number = null;

  public acmeContacts: IAcmeContact[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllAcmeContacts();
  }

  public clear(): void {
    this.retrieveAllAcmeContacts();
  }

  public retrieveAllAcmeContacts(): void {
    this.isFetching = true;

    this.acmeContactService()
      .retrieve()
      .then(
        res => {
          this.acmeContacts = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IAcmeContact): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeAcmeContact(): void {
    this.acmeContactService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.acmeContact.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllAcmeContacts();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
