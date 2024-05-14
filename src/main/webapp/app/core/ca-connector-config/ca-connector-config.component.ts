import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import { ICAConnectorStatus, ICAStatus, ICertificateFilter, ICertificateFilterList } from '@/shared/model/transfer-object.model';

import AlertMixin from '@/shared/alert/alert.mixin';

import CAConnectorConfigService from './ca-connector-config.service';
import axios from 'axios';

const statusApiUrl = 'api/ca-connector-configs/status';

@Component
export default class CAConnectorConfig extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;
  private removeId: number = null;

  public cAConnectorConfigs: ICAConnectorConfig[] = [];
  public filteredCAConnectorConfigs: ICAConnectorConfig[] = [];

  public cAConnectorStatus: ICAConnectorStatus[] = [];

  public defaultFilters: ICertificateFilter[] = [
    { attributeName: 'type', attributeValue: 'all', selector: 'EQUAL' },
    { attributeName: 'active', attributeValue: 'all', selector: 'EQUAL' },
  ];
  public filters: ICertificateFilterList = { filterList: this.defaultFilters };
  public lastFilters: string = JSON.stringify({ filterList: [this.defaultFilter] });

  public typeFilter = 'all';
  public activeFilter = 'all';

  public isFetching = false;

  public timer;

  public mounted(): void {
    this.getUsersFilterList();
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
    const self = this;

    this.cAConnectorConfigService()
      .retrieve()
      .then(
        res => {
          self.cAConnectorConfigs = res.data;
          self.filterCAConnectorConfigs();
          self.isFetching = false;
        },
        err => {
          self.isFetching = false;
        }
      );

    //    this.retrieveAllCAConnectorStatus();
  }

  public updateCAConnectorConfigs(): void {
    //    this.retrieveAllCAConnectorStatus();
  }

  public retrieveAllCAConnectorStatus(): void {
    const self = this;
    axios
      .get(statusApiUrl)
      .then(function (res) {
        self.cAConnectorStatus = res.data;
      })
      .catch(err => {
        window.console.info(err);
      });
  }

  public getStatus(connectorId: number): ICAStatus {
    for (const ccs of this.cAConnectorStatus) {
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

  public filterCAConnectorConfigs(): void {
    window.console.info('filter cAConnectorConfig for ' + this.typeFilter + ' / ' + this.activeFilter);

    this.filteredCAConnectorConfigs = [];
    for (const cAConnectorConfig of this.cAConnectorConfigs) {
      if (this.typeFilter !== 'all') {
        if (cAConnectorConfig.caConnectorType !== this.typeFilter) {
          window.console.info('cAConnectorConfig dropped: ' + cAConnectorConfig.caConnectorType);
          continue;
        }
      }

      if (this.activeFilter !== 'all') {
        if (
          (cAConnectorConfig.active && this.activeFilter === 'disabled') ||
          (!cAConnectorConfig.active && this.activeFilter === 'enabled')
        ) {
          window.console.info('cAConnectorConfig dropped: ' + cAConnectorConfig.active);
          continue;
        }
      }

      this.filteredCAConnectorConfigs.push(cAConnectorConfig);
    }

    this.putUsersFilterList(this);
  }

  public getUsersFilterList(): void {
    window.console.info('calling getUsersFilterList for CaConnectorConfig');
    const self = this;

    axios({
      method: 'get',
      url: 'api/userProperties/filterList/CaConnectorConfigList',
      responseType: 'stream',
    }).then(function (response) {
      //      window.console.debug('getUsersFilterList returns ' + response.data );
      if (response.status === 200) {
        self.filters.filterList = response.data.filterList;
        //        window.console.debug('getUsersFilterList sets filters to ' + JSON.stringify(self.filters));
        self.lastFilters = JSON.stringify(self.filters);

        for (const filter of self.filters.filterList) {
          if ('type' === filter.attributeName) {
            self.typeFilter = filter.attributeValue;
          } else if ('active' === filter.attributeName) {
            self.activeFilter = filter.attributeValue;
          }
        }
      }
    });
  }

  public putUsersFilterList(self): void {
    for (const filter of self.filters.filterList) {
      if ('type' === filter.attributeName) {
        filter.attributeValue = self.typeFilter;
      } else if ('active' === filter.attributeName) {
        filter.attributeValue = self.activeFilter;
      }
    }

    //    window.console.debug('calling putUsersFilterList ');
    const lastFiltersValue = JSON.stringify(self.filters);
    if (self.lastFilters === lastFiltersValue) {
      //      window.console.debug('putUsersFilterList: no change ...');
    } else {
      window.console.debug('putUsersFilterList: change detected ...');
      axios({
        method: 'put',
        url: 'api/userProperties/filterList/CaConnectorConfigList',
        data: self.filters,
        responseType: 'stream',
      }).then(function (response) {
        //        window.console.debug('putUsersFilterList returns ' + response.status);
        if (response.status === 204) {
          self.lastFilters = lastFiltersValue;
        }
      });
    }
  }
}
