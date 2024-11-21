import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';

import {
  ICertificateFilter,
  ICertificateFilterList,
  ISelector,
  ICertificateSelectionData,
  IUserDTO,
} from '@/shared/model/transfer-object.model';

import { colFieldToStr, makeQueryStringFromObj } from '@/shared/utils';

import { VuejsDatatableFactory, TColumnsDefinition, ITableContentParam } from 'vuejs-datatable';

import axios from 'axios';
import AlertMixin from '@/shared/alert/alert.mixin';

Vue.use(VuejsDatatableFactory);

VuejsDatatableFactory.useDefaultType(false).registerTableType<any, any, any, any, any>('users-table', tableType =>
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
      window.console.info('in setDisplayHandler ');

      const delimit = baseEndPoint.includes('?') ? '&' : '?';
      const url = `${baseEndPoint}${delimit}${makeQueryStringFromObj(endpointDesc)}`;

      const {
        // Data to display
        data,
        // Get the total number of matched items
        headers: { 'x-total-count': totalCount },
      } = await axios.get(url);

      window.console.info('list data returns ' + totalCount);

      return {
        rows: data,
        totalRowCount: parseInt(totalCount, 10),
      } as ITableContentParam<IUserDTO>;
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

interface ISelectionChoices {
  itemType: string;
  hasValue: boolean;
  choices?: ISelector[];
}

@Component
export default class CertList extends mixins(AlertMixin, Vue) {
  public now: Date = new Date();
  public soon: Date = new Date();
  public recently: Date = new Date();
  public dateWarn = new Date();
  public dateAlarm = new Date();

  public userSelectionAttributes: string[] = [];

  public userSelectionItems: ICertificateSelectionData[] = [
    { itemName: 'id', itemType: 'number', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'login', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'first' },
    { itemName: 'firstName', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'first' },
    { itemName: 'lastName', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'last' },
    { itemName: 'email', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: '' },
    { itemName: 'activated', itemType: 'boolean', itemDefaultSelector: 'ISTRUE', itemDefaultValue: 'true' },

    { itemName: 'langKey', itemType: 'string', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'createdDate', itemType: 'date', itemDefaultSelector: 'AFTER', itemDefaultValue: '{now}' },
    { itemName: 'lastModifiedBy', itemType: 'string', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'lastModifiedDate', itemType: 'date', itemDefaultSelector: 'AFTER', itemDefaultValue: '{now}' },
    { itemName: 'isManagedExternally', itemType: 'boolean', itemDefaultSelector: 'ISTRUE', itemDefaultValue: 'true' },

    { itemName: 'failedLogins', itemType: 'number', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'blockedUntilDate', itemType: 'date', itemDefaultSelector: 'AFTER', itemDefaultValue: '{now}' },
    { itemName: 'credentialsValidToDate', itemType: 'date', itemDefaultSelector: 'AFTER', itemDefaultValue: '{now}' },
    /*
    { itemName: 'tenant', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: null },
    {
      itemName: 'authorities',
      itemType: 'set',
      itemDefaultSelector: 'EQUAL',
      itemDefaultValue: 'ROLE_USER',
      values: ['ROLE_ADMIN', 'ROLE_RA', 'ROLE_RA_DOMAIN', 'ROLE_USER'],
    },
*/
  ];

  public selectionChoices: ISelectionChoices[] = [
    { itemType: 'string', hasValue: true, choices: ['EQUAL', 'NOT_EQUAL', 'LIKE', 'NOTLIKE', 'LESSTHAN', 'GREATERTHAN'] },
    { itemType: 'number', hasValue: true, choices: ['EQUAL', 'NOT_EQUAL', 'LESSTHAN', 'GREATERTHAN'] },
    { itemType: 'serial', hasValue: true, choices: ['DECIMAL', 'HEX'] },
    { itemType: 'date', hasValue: true, choices: ['ON', 'BEFORE', 'AFTER'] },
    //    { itemType: 'date', hasValue: true, choices: ['ON', 'BEFORE', 'AFTER', 'PERIOD_BEFORE', 'PERIOD_AFTER'] },
    { itemType: 'boolean', hasValue: false, choices: ['ISTRUE', 'ISFALSE'] },
    { itemType: 'set', hasValue: false, choices: ['EQUAL', 'NOT_EQUAL'] },
  ];

  public defaultFilter: ICertificateFilter = { attributeName: 'subject', attributeValue: 'trust', selector: 'LIKE' };
  public filters: ICertificateFilterList = { filterList: [this.defaultFilter] };
  public lastFilters: string = JSON.stringify({ filterList: [this.defaultFilter] });

  public pageSize = 20;

  public contentAccessUrl: string;
  public tmpContentAccessUrl: string;

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public addSelector() {
    const newFilter = { ...this.defaultFilter };
    this.filters.filterList.push(newFilter);
  }
  public removeSelector(index: number) {
    this.filters.filterList.splice(index, 1);
  }

  public getInputType(itemName: string): string {
    const selectionItem = this.userSelectionItems.find(selections => selections.itemName === itemName);
    if (selectionItem) {
      return selectionItem.itemType;
    }
    return '';
  }

  public getInputSelector(itemName: string): string {
    const selectionItem = this.userSelectionItems.find(selections => selections.itemName === itemName);

    if (selectionItem) {
      window.console.info('getInputSelector(' + itemName + ') -> ' + selectionItem.itemDefaultSelector);
      return selectionItem.itemDefaultSelector;
    }
    return '';
  }

  public getRevocationStyle(revoked: boolean): string {
    return revoked ? 'text-decoration:line-through;' : '';
  }

  public getLoginStyle(blockedUntilDate: Date, activated: boolean): string {
    if (blockedUntilDate && blockedUntilDate > this.now) {
      //      window.console.info('getValidToStyle(' + validTo + '), dateNow: ' + dateNow + ' , dateWarn: ' + dateWarn +
      //      ' -> ' + (validTo > dateNow) + ' - ' + (validTo < dateWarn));
      return 'color:red;font-weight: bold;';
    } else if (!activated) {
      return 'color:green; font-weight: bold;';
    }
    return '';
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
  }

  public getValueChoices(itemName: string): string[] {
    const selectionItem = this.userSelectionItems.find(selections => selections.itemName === itemName);
    if (selectionItem) {
      return selectionItem.values;
    }
    return [];
  }

  public getSelectorChoices(itemName: string): string[] {
    //    window.console.info('getChoices(' + itemName + ')');

    const selectionItem = this.userSelectionItems.find(selections => selections.itemName === itemName);

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
        { label: 'id', field: 'id' },
        { label: this.$t('login'), field: 'login', headerClass: 'class-in-header second-class' },
        { label: this.$t('firstName'), field: 'firstName' },
        { label: this.$t('lastName'), field: 'lastName' },
        { label: this.$t('email'), field: 'email' },
        { label: this.$t('activated'), field: 'activated' },
        { label: this.$t('langKey'), field: 'langKey' },
        { label: this.$t('createdDate'), field: 'createdDate' },
        //        { label: this.$t('lastModifiedBy'), field: 'lastModifiedBy' },

        //        { label: this.$t('isManagedExternally'), field: 'isManagedExternally' },
        { label: this.$t('failedLogins'), field: 'failedLogins' },
        { label: this.$t('blockedUntilDate'), field: 'blockedUntilDate' },
        //        { label: this.$t('credentialsValidToDate'), field: 'credentialsValidToDate' },
        { label: this.$t('tenant'), field: 'tenant' },
        { label: this.$t('authorities'), field: 'authorities' },
      ] as TColumnsDefinition<IUserDTO>,
      page: 1,
      filter: '',
      contentAccessUrl: '',

      get userApiUrl() {
        window.console.info('userApiUrl returns : ' + self.contentAccessUrl);
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

  public buildAccessUrl(baseUrl: string): string {
    const filterLen = this.filters.filterList.length;

    const params = {};
    for (let i = 0; i < filterLen; i++) {
      const filter = this.filters.filterList[i];
      const idx = i + 1;
      params['attributeName_' + idx] = filter.attributeName;
      params['attributeValue_' + idx] = filter.attributeValue;
      params['attributeSelector_' + idx] = filter.selector;
    }

    return `${baseUrl}?${makeQueryStringFromObj(params)}`;
  }

  public buildContentAccessUrl() {
    const url = this.buildAccessUrl('api/userList');

    if (this.tmpContentAccessUrl !== url) {
      this.tmpContentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change detected');
    } else if (this.contentAccessUrl !== url) {
      this.contentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change propagated');
    }
  }

  public mounted(): void {
    this.soon.setDate(this.now.getDate() + 7);
    this.recently.setDate(this.now.getDate() - 7);
    this.dateWarn.setDate(this.now.getDate() + 35);
    this.dateAlarm.setDate(this.now.getDate() + 10);

    this.getUsersFilterList();
    setInterval(() => this.putUsersFilterList(this), 3000);
    setInterval(() => this.buildContentAccessUrl(), 1000);
  }

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public getUsersFilterList(): void {
    window.console.info('calling getUsersFilterList ');
    const self = this;

    axios({
      method: 'get',
      url: 'api/userProperties/filterList/UserList',
      responseType: 'stream',
    }).then(function (response) {
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
        url: 'api/userProperties/filterList/UserList',
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
  /*
  public downloadCSV() {
    const url = this.buildAccessUrl('publicapi/certificateListCSV');
    //      '&filter=id%2Csubject%2Cissuer%2Ctype%2CkeyLength%2Cserial%2CvalidFrom%2CvalidTo%2ChashAlgorithm%2CpaddingAlgorithm%2Crevoked%2CrevokedSince%2CrevocationReason';

    this.download(url, 'certificateList.csv', 'text/csv');
  }

  public download(url: string, filename: string, mimetype: string) {
    axios
      .get(url, { responseType: 'blob', headers: { Accept: mimetype } })
      .then(response => {
        const blob = new Blob([response.data], { type: mimetype, endings: 'transparent' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.type = mimetype;

        window.console.info('tmp download lnk : ' + link.download);

        link.click();
        URL.revokeObjectURL(link.href);
      })
      .catch(console.error);
  }

  public printObject(object): string {
    window.console.info('printObject : ' + object);
    return '###';
  }

 */
}
