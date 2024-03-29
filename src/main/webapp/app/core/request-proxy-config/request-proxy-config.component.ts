import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IRequestProxyConfigView } from '@/shared/model/transfer-object.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import RequestProxyConfigService from './request-proxy-config.service';

@Component
export default class RequestProxyConfig extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('requestProxyConfigService') private requestProxyConfigService: () => RequestProxyConfigService;
  private removeId: number = null;
  public requestProxyConfigs: IRequestProxyConfigView[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllRequestProxyConfigs();
  }

  public clear(): void {
    this.retrieveAllRequestProxyConfigs();
  }

  public retrieveAllRequestProxyConfigs(): void {
    this.isFetching = true;

    this.requestProxyConfigService()
      .retrieve()
      .then(
        res => {
          this.requestProxyConfigs = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IRequestProxyConfigView): void {
    this.removeId = instance.id;
  }

  public removeRequestProxyConfig(): void {
    this.requestProxyConfigService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.requestProxyConfig.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllRequestProxyConfigs();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
