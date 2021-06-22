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

      window.console.info('list data returns ' + totalCount);

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
  public now: Date = new Date();
  public soon: Date = new Date();
  public recently: Date = new Date();
  public dateWarn = new Date();
  public dateAlarm = new Date();

  public certificateSelectionAttributes: string[] = [];

  public certSelectionItems: ICertificateSelectionData[] = [
    { itemName: 'subject', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'trustable' },
    { itemName: 'cn', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'trustable' },
    { itemName: 'sans', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'trustable' },
    { itemName: 'comment', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'trustable' },

    { itemName: 'issuer', itemType: 'string', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'serial', itemType: 'number', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'fingerprint', itemType: 'number', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'id', itemType: 'number', itemDefaultSelector: null, itemDefaultValue: null },
    { itemName: 'validTo', itemType: 'date', itemDefaultSelector: 'AFTER', itemDefaultValue: '{now}' },
    { itemName: 'active', itemType: 'boolean', itemDefaultSelector: 'ISTRUE', itemDefaultValue: 'true' },
    { itemName: 'trusted', itemType: 'boolean', itemDefaultSelector: 'ISTRUE', itemDefaultValue: 'true' },
    {
      itemName: 'pkiLevel',
      itemType: 'set',
      itemDefaultSelector: 'EQUAL',
      itemDefaultValue: 'root',
      values: ['root', 'intermediate', 'endEntity']
    },
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
    const selectionItem = this.certSelectionItems.find(selections => selections.itemName === itemName);
    if (selectionItem) {
      return selectionItem.itemType;
    }
    return '';
  }

  public getSubjectStyle(ca: boolean, selfsigned: boolean, revoked: boolean, validToString: string): string {
    let revStyle = revoked ? 'text-decoration:line-through;' : '';

    const validTo = new Date(validToString);

    if (validTo < this.now) {
      revStyle += 'font-style: italic;';
    }

    if (ca && selfsigned) {
      return revStyle + 'color:navy;font-weight: bold;';
    } else if (selfsigned) {
      return revStyle + 'color:cyan; font-weight: bold;';
    } else if (ca) {
      return revStyle + 'color:green; font-weight: bold;';
    }
    return revStyle;
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

    if (validTo > this.now && validTo < this.dateAlarm) {
      //      window.console.info('getValidToStyle(' + validTo + '), dateNow: ' + dateNow + ' , dateWarn: ' + dateWarn + ' -> ' + (validTo > dateNow) + ' - ' + (validTo < dateWarn));
      return 'color:red;font-weight: bold;';
    } else if (validTo > this.now && validTo < this.dateWarn) {
      return 'color:yellow; font-weight: bold;';
    } else if (validTo > this.now && validFrom <= this.now) {
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
        //        { label: this.$t('ca'), field: 'ca', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        //        { label: this.$t('selfsigned'), field: 'selfSigned', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
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
    const url = this.buildAccessUrl('publicapi/certificateList');

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

    this.getCertificateSelectionAttributes();
    this.getUsersFilterList();
    setInterval(() => this.putUsersFilterList(this), 3000);
    setInterval(() => this.buildContentAccessUrl(), 1000);
  }

  public getCertificateSelectionAttributes(): void {
    window.console.info('calling getCertificateSelectionAttributes ');
    const self = this;

    axios({
      method: 'get',
      url: 'api/certificateSelectionAttributes',
      responseType: 'stream'
    }).then(function(response) {
      //      window.console.debug('getUsersFilterList returns ' + response.data );
      if (response.status === 200) {
        self.certificateSelectionAttributes = response.data;

        for (let i = 0; i < self.certificateSelectionAttributes.length; i++) {
          self.certSelectionItems.push({
            itemName: self.certificateSelectionAttributes[i],
            itemType: 'string',
            itemDefaultSelector: 'EQUAL',
            itemDefaultValue: 'X'
          });
        }
      }
    });
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
}
