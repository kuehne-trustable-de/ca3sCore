import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { ICertificateFilter, ICertificateFilterList, IPipelineView } from '@/shared/model/transfer-object.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import PipelineViewService from './pipelineview.service';
import axios from 'axios';

@Component
export default class Pipeline extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('pipelineViewService') private pipelineViewService: () => PipelineViewService;
  private removeId: number = null;
  public pipelines: IPipelineView[] = [];
  public filteredPipelines: IPipelineView[] = [];
  public distinctConnectors: string[] = [];

  public typeFilter = 'all';
  public activeFilter = 'all';
  public connectorFilter = 'all';

  public defaultFilters: ICertificateFilter[] = [
    { attributeName: 'type', attributeValue: 'all', selector: 'EQUAL' },
    { attributeName: 'active', attributeValue: 'all', selector: 'EQUAL' },
    { attributeName: 'connector', attributeValue: 'all', selector: 'EQUAL' },
  ];
  public filters: ICertificateFilterList = { filterList: this.defaultFilters };
  public lastFilters: string = JSON.stringify({ filterList: [this.defaultFilter] });

  public isFetching = false;

  public mounted(): void {
    this.typeFilter = 'all';
    this.activeFilter = 'all';

    this.getUsersFilterList();

    this.retrieveAllPipelines();

    window.console.info('pipeline mounted, #' + this.filteredPipelines.length + ' filtered pipelines');
  }

  public clear(): void {
    this.retrieveAllPipelines();
  }

  public retrieveAllPipelines(): void {
    this.isFetching = true;

    let self = this;

    this.pipelineViewService()
      .retrieve()
      .then(
        res => {
          self.pipelines = res.data;
          self.filterPipelines();
          self.isFetching = false;
        },
        err => {
          self.isFetching = false;
        }
      );
  }

  public filterPipelines(): void {
    window.console.info('filter pipeline for ' + this.typeFilter + ' / ' + this.activeFilter);

    let connectorNames: string[] = [];
    for (const pipeline of this.pipelines) {
      if (!connectorNames.includes(pipeline.caConnectorName)) {
        connectorNames.push(pipeline.caConnectorName);
      }
    }
    this.distinctConnectors = connectorNames.sort((a, b) => a.localeCompare(b));

    this.filteredPipelines = [];
    for (const pipeline of this.pipelines) {
      if (this.typeFilter !== 'all') {
        if (pipeline.type !== this.typeFilter) {
          window.console.info('pipeline dropped: ' + pipeline.type);
          continue;
        }
      }

      if (this.activeFilter !== 'all') {
        if ((pipeline.active && this.activeFilter === 'disabled') || (!pipeline.active && this.activeFilter === 'enabled')) {
          window.console.info('pipeline dropped: ' + pipeline.active);
          continue;
        }
      }

      if (this.connectorFilter !== 'all') {
        if (pipeline.caConnectorName !== this.connectorFilter) {
          window.console.info('pipeline dropped: ' + pipeline.caConnectorName);
          continue;
        }
      }
      this.filteredPipelines.push(pipeline);
    }
    this.putUsersFilterList(this);
  }

  public prepareRemove(instance: IPipelineView): void {
    this.removeId = instance.id;
  }

  public removePipeline(): void {
    this.pipelineViewService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.pipeline.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllPipelines();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }

  public getUsersFilterList(): void {
    window.console.info('calling getUsersFilterList fpr Pipeline');
    const self = this;

    axios({
      method: 'get',
      url: 'api/userProperties/filterList/PipelineList',
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
          } else if ('connector' === filter.attributeName) {
            self.connectorFilter = filter.attributeValue;
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
      } else if ('connector' === filter.attributeName) {
        filter.attributeValue = self.connectorFilter;
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
        url: 'api/userProperties/filterList/PipelineList',
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
