import Component from 'vue-class-component';
import { Inject, Vue } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';

import { ICertificateFilter, ICertificateFilterList, ISelector, ICertificateSelectionData } from '@/shared/model/transfer-object.model';

import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';

import { colFieldToStr, makeQueryStringFromObj } from '@/shared/utils';

import { VuejsDatatableFactory, TColumnsDefinition, ITableContentParam } from 'vuejs-datatable';

import BPNMProcessInfoService from '../../entities/bpnm-process-info/bpnm-process-info.service';

import axios from 'axios';
import AlertMixin from '@/shared/alert/alert.mixin';

Vue.use(VuejsDatatableFactory);

interface ISelectionChoices {
  itemType: string;
  hasValue: boolean;
  choices?: ISelector[];
}

VuejsDatatableFactory.registerTableType<any, any, any, any, any>('bpmn-table', tableType =>
  tableType
    .setFilterHandler((source, filter, columns) => ({
      // See https://documenter.getpostman.com/view/2025350/RWaEzAiG#json-field-masking
      filter: columns.map(col => colFieldToStr(col.field!).replace(/\./g, '/')).join(','),
    }))

    .setSortHandler((endpointDesc, sortColumn, sortDir) => ({
      ...endpointDesc,

      ...(sortColumn && sortDir
        ? {
            order: sortDir,
            sort: colFieldToStr(sortColumn.field!).replace(/\./g, '/'),
          }
        : {}),
    }))

    .setPaginateHandler((endpointDesc, perPage, pageIndex) => ({
      ...endpointDesc,

      ...(perPage !== null
        ? {
            limit: perPage || 20,
            offset: (pageIndex - 1) * perPage || 0,
          }
        : {}),
    }))

    // Alias our process steps, because the source, here, is our API url, and paged is the complete query string
    .setDisplayHandler(async ({ source: baseEndPoint, paged: endpointDesc }) => {
      const delimit = baseEndPoint.includes('?') ? '&' : '?';
      const url = `${baseEndPoint}${delimit}${makeQueryStringFromObj(endpointDesc)}`;

      const {
        // Data to display
        data,
        // Get the total number of matched items
        headers: { 'x-total-count': totalCount },
      } = await axios.get(url);

      return {
        rows: data,
        totalRowCount: parseInt(totalCount, 10),
      } as ITableContentParam<IBPMNProcessInfo>;
    })
    .mergeSettings({
      table: {
        class: 'table table-hover table-striped',
        sorting: {
          sortAsc: '<img src="../../../content/images/caret-up-solid.png" alt="asc">',
          sortDesc: '<img src="../../../content/images/caret-down-solid.png" alt="desc">',
          sortNone: '',
        },
      },
      pager: {
        classes: {
          pager: 'pagination text-center',
          selected: 'active',
        },
        icons: {
          next: '<img src="../../../content/images/chevron-right-solid.png" alt=">">',
          previous: '<img src="../../../content/images/chevron-left-solid.png" alt="<">',
        },
      },
    })
);

@Component
export default class BpmnList extends mixins(AlertMixin, Vue) {
  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public bpmnSelectionItems: ICertificateSelectionData[] = [
    { itemName: 'name', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'ISSUE' },
    { itemName: 'version', itemType: 'string', itemDefaultSelector: 'GREATERTHAN', itemDefaultValue: null },
    {
      itemName: 'type',
      itemType: 'set',
      itemDefaultSelector: 'EQUAL',
      itemDefaultValue: 'ISSUANCE',
      values: ['ISSUANCE', 'REVOCATION', 'ACME_ACCOUNT'],
    },
    { itemName: 'author', itemType: 'string', itemDefaultSelector: 'EQUAL', itemDefaultValue: '{user}' },
    { itemName: 'lastChange', itemType: 'date', itemDefaultSelector: 'BEFORE', itemDefaultValue: '{now}' },
  ];

  public selectionChoices: ISelectionChoices[] = [
    { itemType: 'string', hasValue: true, choices: ['EQUAL', 'NOT_EQUAL', 'LIKE', 'NOTLIKE', 'LESSTHAN', 'GREATERTHAN'] },
    { itemType: 'number', hasValue: true, choices: ['EQUAL', 'NOT_EQUAL', 'LESSTHAN', 'GREATERTHAN'] },
    { itemType: 'date', hasValue: true, choices: ['ON', 'BEFORE', 'AFTER'] },
    { itemType: 'boolean', hasValue: false, choices: ['ISTRUE', 'ISFALSE'] },
    { itemType: 'set', hasValue: false, choices: ['EQUAL', 'NOT_EQUAL'] },
  ];

  public contentAccessUrl: string;
  public tmpContentAccessUrl: string;

  public defaultFilter: ICertificateFilter = { attributeName: 'type', attributeValue: 'ISSUANCE', selector: 'EQUAL' };
  public filters: ICertificateFilterList = { filterList: [this.defaultFilter] };
  public lastFilters: string = JSON.stringify({ filterList: [this.defaultFilter] });

  public removeId: number = null;

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public addSelector() {
    const newFilter = { ...this.defaultFilter };
    this.filters.filterList.push(newFilter);
  }
  public removeSelector(index: number) {
    this.filters.filterList.splice(index, 1);
  }

  public getInputType(itemName: string): string {
    const selectionItem = this.bpmnSelectionItems.find(selections => selections.itemName === itemName);
    if (selectionItem) {
      return selectionItem.itemType;
    }
    return '';
  }

  public getRevocationStyle(revoked: boolean): string {
    return revoked ? 'text-decoration:line-through;' : '';
  }

  public toLocalDate(dateAsString: string): string {
    if (dateAsString && dateAsString.length > 8) {
      const dateObj = new Date(dateAsString);
      return dateObj.toLocaleDateString();
    }
    return '';
  }

  public getValueChoices(itemName: string): string[] {
    const selectionItem = this.bpmnSelectionItems.find(selections => selections.itemName === itemName);
    if (selectionItem) {
      return selectionItem.values;
    }
    return [];
  }

  public getLoading(): boolean {
    return true;
  }

  public getColor(): string {
    return '#3AB982';
  }

  public getSize(): Object {
    return { height: '35px', width: '4px', margin: '2px', radius: '2px' };
  }

  el() {
    return '#vue-certificates';
  }
  data() {
    const self = this;

    return {
      columns: [
        { label: 'id', field: 'id' },
        { label: this.$t('bpmn.name'), field: 'name' },
        { label: this.$t('type'), field: 'type' },
        { label: this.$t('version'), field: 'version' },
        { label: this.$t('author'), field: 'author' },
        { label: this.$t('lastChange'), field: 'lastChange' },
        { label: this.$t('action'), field: 'lastChange' },
      ] as TColumnsDefinition<IBPMNProcessInfo>,
      page: 1,
      filter: '',
      contentAccessUrl: '',

      get bpmnApiUrl() {
        window.console.info('bpmnApiUrl returns : ' + self.contentAccessUrl);
        return self.contentAccessUrl;
      },
    };
  }

  // refesh table by pressing 'enter'
  public updateTable() {
    //    window.console.debug('updateTable: enter pressed ...');
    this.buildContentAccessUrl();
    this.buildContentAccessUrl();
  }

  public prepareRemove(instance: IBPMNProcessInfo): void {
    this.removeId = instance.id;
  }

  public removeBPMNProcess(): void {
    this.bPNMProcessInfoService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.bpmn.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        //        this.retrieveAllPipelines();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }

  public buildContentAccessUrl() {
    const filterLen = this.filters.filterList.length;

    const params = {};
    for (let i = 0; i < filterLen; i++) {
      const filter = this.filters.filterList[i];
      const idx = i + 1;
      params['attributeName_' + idx] = filter.attributeName;
      params['attributeValue_' + idx] = filter.attributeValue;
      params['attributeSelector_' + idx] = filter.selector;
    }

    const baseApiUrl = 'api/bpmn-process-infos';
    const url = `${baseApiUrl}?${makeQueryStringFromObj(params)}`;

    if (this.tmpContentAccessUrl !== url) {
      this.tmpContentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change detected: ' + url);
    } else if (this.contentAccessUrl !== url) {
      this.contentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change propagated: ' + url);
    }
  }

  public mounted(): void {
    this.getUsersFilterList();
    setInterval(() => this.putUsersFilterList(this), 3000);
    setInterval(() => this.buildContentAccessUrl(), 1000);
  }

  public getUsersFilterList(): void {
    window.console.info('calling getUsersFilterList ');
    const self = this;

    axios({
      method: 'get',
      url: 'api/userProperties/filterList/BPMNList',
      responseType: 'stream',
    })
      .then(function (response) {
        //      window.console.debug('getUsersFilterList returns ' + response.data );
        if (response.status === 200) {
          self.filters.filterList = response.data.filterList;
          //        window.console.debug('getUsersFilterList sets filters to ' + JSON.stringify(self.filters));
          self.lastFilters = JSON.stringify(self.filters);
        }
      })
      .catch(console.error);
  }

  public putUsersFilterList(self): void {
    //    window.console.debug('calling putUsersFilterList ');
    const lastFiltersValue = JSON.stringify(self.filters);
    if (self.lastFilters === lastFiltersValue) {
      //      window.console.debug('putUsersFilterList: no change ...');
    } else {
      window.console.debug('putUsersFilterList: change detected ...');
      axios({
        method: 'put',
        url: 'api/userProperties/filterList/BPMNList',
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
