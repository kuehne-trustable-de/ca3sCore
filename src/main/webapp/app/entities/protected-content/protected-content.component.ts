import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IProtectedContent } from '@/shared/model/protected-content.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import JhiDataUtils from '@/shared/data/data-utils.service';

import ProtectedContentService from './protected-content.service';

@Component
export default class ProtectedContent extends mixins(JhiDataUtils, Vue2Filters.mixin, AlertMixin) {
  @Inject('protectedContentService') private protectedContentService: () => ProtectedContentService;
  private removeId: number = null;
  public protectedContents: IProtectedContent[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllProtectedContents();
  }

  public clear(): void {
    this.retrieveAllProtectedContents();
  }

  public retrieveAllProtectedContents(): void {
    this.isFetching = true;

    this.protectedContentService()
      .retrieve()
      .then(
        res => {
          this.protectedContents = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IProtectedContent): void {
    this.removeId = instance.id;
  }

  public removeProtectedContent(): void {
    this.protectedContentService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.protectedContent.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllProtectedContents();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
