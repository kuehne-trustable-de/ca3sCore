import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IImportedURL } from '@/shared/model/imported-url.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import ImportedURLService from './imported-url.service';

@Component
export default class ImportedURL extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('importedURLService') private importedURLService: () => ImportedURLService;
  private removeId: number = null;

  public importedURLS: IImportedURL[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllImportedURLs();
  }

  public clear(): void {
    this.retrieveAllImportedURLs();
  }

  public retrieveAllImportedURLs(): void {
    this.isFetching = true;

    this.importedURLService()
      .retrieve()
      .then(
        res => {
          this.importedURLS = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IImportedURL): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeImportedURL(): void {
    this.importedURLService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.importedURL.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllImportedURLs();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
