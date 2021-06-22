import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';

import {
  ICertificateFilter,
  ICertificateFilterList,
  ISelector,
  ICertificateSelectionData,
  IAuditTraceView
} from '@/shared/model/transfer-object.model';

import { colFieldToStr, makeQueryStringFromObj } from '@/shared/utils';

import { VuejsDatatableFactory, TColumnsDefinition, ITableContentParam } from 'vuejs-datatable';

import axios from 'axios';
import AlertMixin from '@/shared/alert/alert.mixin';

Vue.use(VuejsDatatableFactory);

interface ISelectionChoices {
  itemType: string;
  hasValue: boolean;
  choices?: ISelector[];
}

VuejsDatatableFactory.useDefaultType(false).registerTableType<any, any, any, any, any>('audit-table', tableType =>
  tableType
    .setFilterHandler((source, filter, columns) => ({
      // See https://documenter.getpostman.com/view/2025350/RWaEzAiG#json-field-masking
      filter: columns.map(col => colFieldToStr(col.field!).replace(/\./g, '/')).join(',')
    }))

    .setSortHandler((endpointDesc, sortColumn, sortDir) => ({
      ...endpointDesc,

      ...(sortColumn && sortDir
        ? {
            order: sortDir,
            sort: colFieldToStr(sortColumn.field!).replace(/\./g, '/')
          }
        : {})
    }))

    .setPaginateHandler((endpointDesc, perPage, pageIndex) => ({
      ...endpointDesc,

      ...(perPage !== null
        ? {
            limit: perPage || 20,
            offset: (pageIndex - 1) * perPage || 0
          }
        : {})
    }))

    // Alias our process steps, because the source, here, is our API url, and paged is the complete query string
    .setDisplayHandler(async ({ source: baseEndPoint, paged: endpointDesc }) => {
      const delimit = baseEndPoint.includes('?') ? '&' : '?';
      const url = `${baseEndPoint}${delimit}${makeQueryStringFromObj(endpointDesc)}`;

      window.console.info('setDisplayHandler url(' + url + ')');

      const {
        // Data to display
        data,
        // Get the total number of matched items
        headers: { 'x-total-count': totalCount }
      } = await axios.get(url);

      return {
        rows: data,
        totalRowCount: parseInt(totalCount, 10)
      } as ITableContentParam<IAuditTraceView>;
    })
    .mergeSettings({
      table: {
        class: 'table table-hover table-striped',
        sorting: {
          sortAsc: '<img src="../../../content/images/caret-up-solid.png" alt="asc">',
          sortDesc: '<img src="../../../content/images/caret-down-solid.png" alt="desc">',
          sortNone: ''
        }
      },
      pager: {
        classes: {
          pager: 'pagination text-center',
          selected: 'active'
        },
        icons: {
          next: '<img src="../../../content/images/chevron-right-solid.png" alt=">">',
          previous: '<img src="../../../content/images/chevron-left-solid.png" alt="<">'
        }
      }
    })
);

@Component
export default class CertList extends mixins(AlertMixin, Vue) {
  public now: Date = new Date();
  public soon: Date = new Date();
  public recently: Date = new Date();

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public certSelectionItems: ICertificateSelectionData[] = [
    { itemName: 'actorName', itemType: 'string', itemDefaultSelector: 'EQUAL', itemDefaultValue: '{user}' },
    {
      itemName: 'actorRole',
      itemType: 'set',
      itemDefaultSelector: 'EQUAL',
      itemDefaultValue: 'USER',
      values: ['USER', 'RA', 'ADMIN', 'SYSTEM']
    },
    { itemName: 'contentTemplate', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'trustable' },
    { itemName: 'createdOn', itemType: 'date', itemDefaultSelector: 'BEFORE', itemDefaultValue: '{now}' }
  ];

  public selectionChoices: ISelectionChoices[] = [
    { itemType: 'string', hasValue: true, choices: ['EQUAL', 'NOT_EQUAL', 'LIKE', 'NOTLIKE', 'LESSTHAN', 'GREATERTHAN'] },
    { itemType: 'number', hasValue: true, choices: ['EQUAL', 'NOT_EQUAL', 'LESSTHAN', 'GREATERTHAN'] },
    { itemType: 'date', hasValue: true, choices: ['ON', 'BEFORE', 'AFTER'] },
    { itemType: 'boolean', hasValue: false, choices: ['ISTRUE', 'ISFALSE'] },
    { itemType: 'set', hasValue: false, choices: ['EQUAL', 'NOT_EQUAL'] }
  ];

  public defaultFilter: ICertificateFilter = { attributeName: 'subject', attributeValue: 'trust', selector: 'LIKE' };
  public filters: ICertificateFilterList = { filterList: [this.defaultFilter] };
  public lastFilters: string = JSON.stringify({ filterList: [this.defaultFilter] });

  public contentAccessUrl: string;
  public tmpContentAccessUrl: string;

  public addSelector() {
    const newFilter = { ...this.defaultFilter };
    this.filters.filterList.push(newFilter);
  }
  public removeSelector(index: number) {
    this.filters.filterList.splice(index, 1);
  }

  public getInputType(itemName: string): string {
    const selectionItem = this.certSelectionItems.find(selections => selections.itemName === itemName);
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

      if (dateObj > this.recently && dateObj < this.soon) {
        return dateObj.toLocaleDateString() + ', ' + dateObj.toLocaleTimeString();
      } else {
        return dateObj.toLocaleDateString();
      }
    }
    return '';
  }

  public getValueChoices(itemName: string): string[] {
    const selectionItem = this.certSelectionItems.find(selections => selections.itemName === itemName);
    if (selectionItem) {
      return selectionItem.values;
    }
    return [];
  }

  public getSelectorChoices(itemName: string): string[] {
    const selectionItem = this.certSelectionItems.find(selections => selections.itemName === itemName);

    if (selectionItem) {
      const found = this.selectionChoices.find(choices => choices.itemType === selectionItem.itemType);
      window.console.info('getChoices returns ' + found);
      if (found) {
        return found.choices;
      }
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
        { label: this.$t('createdOn'), field: 'createdOn', align: 'right' },
        { label: this.$t('actorName'), field: 'actorName' },
        { label: this.$t('actorRole'), field: 'actorRole' },
        { label: this.$t('content'), field: 'contentTemplate' },
        { label: this.$t('links'), field: 'id' },
        { label: 'plainCcontent', field: 'plainContent', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: 'csrId', field: 'csrId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: 'certificateId', field: 'certificateId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: 'pipelineId', field: 'pipelineId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: 'caConnectorId', field: 'caConnectorId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: 'processInfoId', field: 'processInfoId', headerClass: 'hiddenColumn', class: 'hiddenColumn' }
      ] as TColumnsDefinition<IAuditTraceView>,
      page: 1,
      filter: '',
      contentAccessUrl: '',

      get auditListUrl() {
        window.console.info('auditListUrl returns : ' + self.contentAccessUrl);
        return self.contentAccessUrl;
      }
    };
  }

  public localizedContent(template: string, auditContent: string) {
    const contentParts = auditContent.split(',');

    const len = contentParts.length;
    console.log('localizedContent: ' + auditContent + ',  #' + len);

    if (len === 0) {
      return this.$t(template);
    } else if (len === 1) {
      return this.$t(template, { val: this.unescapeComma(contentParts[0]) });
    } else if (len === 2) {
      return this.$t(template, { oldVal: this.unescapeComma(contentParts[0]), newVal: this.unescapeComma(contentParts[1]) });
    } else {
      return this.$t(template, {
        attribute: this.unescapeComma(contentParts[0]),
        oldVal: this.unescapeComma(contentParts[1]),
        newVal: this.unescapeComma(contentParts[2])
      });
    }
  }

  public links(csrId: string, certificateId: string, pipelineId: string, caConnectorId: string, processInfoId: string) {}

  unescapeComma(content: string): string {
    return content.replace('%2C', ',').replace('%25', '%');
  }

  // refesh table by pressing 'enter'
  public updateTable() {
    //    window.console.debug('updateTable: enter pressed ...');
    this.buildContentAccessUrl();
    this.buildContentAccessUrl();
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

    const baseApiUrl = 'api/auditTraceList';
    const url = `${baseApiUrl}?${makeQueryStringFromObj(params)}`;

    if (this.tmpContentAccessUrl !== url) {
      this.tmpContentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change detected');
    } else if (this.contentAccessUrl !== url) {
      this.contentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change propagated');
    }
  }

  public mounted(): void {
    this.getUsersFilterList();

    this.soon.setDate(this.now.getDate() + 7);
    this.recently.setDate(this.now.getDate() - 7);

    setInterval(() => this.putUsersFilterList(this), 3000);
    setInterval(() => this.buildContentAccessUrl(), 1000);
  }

  public getUsersFilterList(): void {
    window.console.info('calling getUsersFilterList ');
    const self = this;

    axios({
      method: 'get',
      url: 'api/userProperties/filterList/AuditList',
      responseType: 'stream'
    }).then(function(response) {
      //      window.console.debug('getUsersFilterList returns ' + response.data );
      if (response.status === 200) {
        self.filters.filterList = response.data.filterList;
        //        window.console.debug('getUsersFilterList sets filters to ' + JSON.stringify(self.filters));
        self.lastFilters = JSON.stringify(self.filters);
      }
    });
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
        url: 'api/userProperties/filterList/AuditList',
        data: self.filters,
        responseType: 'stream'
      }).then(function(response) {
        //        window.console.debug('putUsersFilterList returns ' + response.status);
        if (response.status === 204) {
          self.lastFilters = lastFiltersValue;
        }
      });
    }
  }
}
