import Component from 'vue-class-component';
import { Vue } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';

import {
  ICertificateFilter,
  ICertificateFilterList,
  ISelector,
  ICertificateSelectionData,
  ICertificateView
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

VuejsDatatableFactory.useDefaultType(false).registerTableType<any, any, any, any, any>('certificate', tableType =>
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

      const {
        // Data to display
        data,
        // Get the total number of matched items
        headers: { 'x-total-count': totalCount }
      } = await axios.get(url);

      return {
        rows: data,
        totalRowCount: parseInt(totalCount, 10)
      } as ITableContentParam<ICertificateView>;
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
  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public certSelectionItems: ICertificateSelectionData[] = [
    { itemName: 'subject', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'trustable' },
    { itemName: 'sans', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'trustable' },
    { itemName: 'issuer', itemType: 'string', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'serial', itemType: 'number', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'id', itemType: 'number', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'validTo', itemType: 'date', itemDefaultSelector: 'AFTER', itemDefaultValue: '{now}' },
    { itemName: 'active', itemType: 'boolean', itemDefaultSelector: 'ISTRUE', itemDefaultValue: 'true' },
    { itemName: 'revoked', itemType: 'boolean', itemDefaultSelector: 'ISTRUE', itemDefaultValue: 'true' },
    {
      itemName: 'revocationReason',
      itemType: 'set',
      itemDefaultSelector: 'EQUAL',
      itemDefaultValue: 'true',
      values: [
        'keyCompromise',
        'cACompromise',
        'affiliationChanged',
        'superseded',
        'cessationOfOperation',
        'privilegeWithdrawn',
        'aACompromise',
        'certificateHold',
        'unspecified'
      ]
    },
    { itemName: 'keyAlgorithm', itemType: 'set', itemDefaultSelector: 'EQUAL', itemDefaultValue: 'true', values: ['rsa', 'dsa', 'ec'] },
    {
      itemName: 'signingAlgorithm',
      itemType: 'set',
      itemDefaultSelector: 'EQUAL',
      itemDefaultValue: 'true',
      values: ['rsa', 'dsa', 'ecdsa']
    },
    { itemName: 'paddingAlgorithm', itemType: 'set', itemDefaultSelector: 'EQUAL', itemDefaultValue: 'true', values: ['pkcs1', 'mgf1'] },
    {
      itemName: 'usage',
      itemType: 'set',
      itemDefaultSelector: 'EQUAL',
      itemDefaultValue: 'true',
      values: [
        'nonRepudiation',
        'cRLSign',
        'keyCertSign',
        'digitalSignature',
        'keyEncipherment',
        'dataEncipherment',
        'unspecified',
        'keyAgreement'
      ]
    },
    { itemName: 'requestedBy', itemType: 'string', itemDefaultSelector: 'EQUAL', itemDefaultValue: '{user}' }
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

  public getValidToStyle(validFromString: string, validToString: string, revoked: boolean): string {
    if (revoked) {
      return '';
    }

    const validTo = new Date(validToString);
    const validFrom = new Date(validFromString);

    const dateNow = new Date();
    const dateWarn = new Date();
    dateWarn.setDate(dateNow.getDate() + 35);
    const dateAlarm = new Date();
    dateAlarm.setDate(dateNow.getDate() + 10);

    if (validTo > dateNow && validTo < dateAlarm) {
      //      window.console.info('getValidToStyle(' + validTo + '), dateNow: ' + dateNow + ' , dateWarn: ' + dateWarn + ' -> ' + (validTo > dateNow) + ' - ' + (validTo < dateWarn));
      return 'color:red;font-weight: bold;';
    } else if (validTo > dateNow && validTo < dateWarn) {
      return 'color:yellow; font-weight: bold;';
    } else if (validTo > dateNow && validFrom <= dateNow) {
      return 'color:green; font-weight: bold;';
    }
    return '';
  }

  public toLocalDate(dateAsString: string): string {
    if (dateAsString && dateAsString.length > 8) {
      const dateObj = new Date(dateAsString);
      return dateObj.toLocaleDateString();
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
    //    window.console.info('getChoices(' + itemName + ')');

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
        { label: 'id', field: 'id' },
        { label: this.$t('subject'), field: 'subject', headerClass: 'class-in-header second-class' },
        { label: this.$t('issuer'), field: 'issuer' },
        { label: this.$t('type'), field: 'type', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: this.$t('length'), field: 'keyLength', align: 'right' },
        {
          label: this.$t('serial'),
          field: 'serial',
          align: 'right',
          representedAs: row =>
            `${
              row.serial.length > 12
                ? row.serial.substring(0, 6).concat('...', row.serial.substring(row.serial.length - 4, row.serial.length - 1))
                : row.serial
            }`
        },
        { label: this.$t('validFrom'), field: 'validFrom' },
        { label: this.$t('validTo'), field: 'validTo' },
        { label: this.$t('hashAlgorithm'), field: 'hashAlgorithm' },
        { label: this.$t('paddingAlgorithm'), field: 'paddingAlgorithm' },
        { label: this.$t('revoked'), field: 'revoked', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: this.$t('revokedOn'), field: 'revokedSince' },
        { label: this.$t('reason'), field: 'revocationReason' },
        { label: this.$t('sans'), field: 'sans' }
      ] as TColumnsDefinition<ICertificateView>,
      page: 1,
      filter: '',
      contentAccessUrl: '',

      get certApiUrl() {
        window.console.info('certApiUrl returns : ' + self.contentAccessUrl);
        return self.contentAccessUrl;
      }
    };
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

    const baseApiUrl = 'publicapi/certificateList';
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
    setInterval(() => this.putUsersFilterList(this), 3000);
    setInterval(() => this.buildContentAccessUrl(), 1000);
  }

  public getUsersFilterList(): void {
    window.console.info('calling getUsersFilterList ');
    const self = this;

    axios({
      method: 'get',
      url: 'api/userProperties/filterList/CertList',
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
        url: 'api/userProperties/filterList/CertList',
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
