import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import CAConnectorConfigService from './ca-connector-config.service';

@Component
export default class CAConnectorConfig extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;
  private removeId: number = null;
  public cAConnectorConfigs: ICAConnectorConfig[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllCAConnectorConfigs();
  }

  public clear(): void {
    this.retrieveAllCAConnectorConfigs();
  }

  public retrieveAllCAConnectorConfigs(): void {
    this.isFetching = true;

    this.cAConnectorConfigService()
      .retrieve()
      .then(
        res => {
          this.cAConnectorConfigs = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: ICAConnectorConfig): void {
    this.removeId = instance.id;
  }

  public removeCAConnectorConfig(): void {
    this.cAConnectorConfigService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.cAConnectorConfig.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllCAConnectorConfigs();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
