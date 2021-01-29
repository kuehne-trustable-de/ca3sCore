import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import { ICAConnectorStatus, ICAStatus } from '@/shared/model/transfer-object.model';

import AlertMixin from '@/shared/alert/alert.mixin';

import CAConnectorConfigService from './ca-connector-config.service';
import axios from 'axios';

const statusApiUrl = 'api/ca-connector-configs/status';

@Component
export default class CAConnectorConfig extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;
  private removeId: number = null;

  public cAConnectorConfigs: ICAConnectorConfig[] = [];

  public cAConnectorStatus: ICAConnectorStatus[] = [];

  public isFetching = false;

  public timer;

  public mounted(): void {
    this.retrieveAllCAConnectorConfigs();
    this.timer = setInterval(this.updateCAConnectorConfigs, 10000);
  }

  public beforeDestroy() {
    clearInterval(this.timer);
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

    this.retrieveAllCAConnectorStatus();
  }

  public updateCAConnectorConfigs(): void {
    this.retrieveAllCAConnectorStatus();
  }

  public retrieveAllCAConnectorStatus(): void {
    const self = this;
    axios
      .get(statusApiUrl)
      .then(function(res) {
        self.cAConnectorStatus = res.data;
      })
      .catch(err => {
        window.console.info(err);
      });
  }

  public getStatus(connectorId: number): ICAStatus {
    for (let ccs of this.cAConnectorStatus) {
      if (connectorId === ccs.connectorId) {
        return ccs.status;
      }
    }
    return 'Unknown';
  }

  public prepareRemove(instance: ICAConnectorConfig): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
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
