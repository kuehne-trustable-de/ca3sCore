import Component from 'vue-class-component';
import { Inject, Vue } from 'vue-property-decorator';
import LoginService from '@/account/login.service';

import { ICertificateFilter, ISelector, ICertificateSelectionData, ICertificateView } from '@/shared/model/transfer-object.model';

import { colFieldToStr, formatUtcDate, makeQueryStringFromObj } from '@/shared/utils';

import { VuejsDatatableFactory, TColumnsDefinition, ITableContentParam, IDataFnParams } from 'vuejs-datatable';

import axios from 'axios';
import { initFilters } from '@/shared/date/filters';

// import VueAxios from 'vue-axios'
// Vue.use(VueAxios, axios)

Vue.use(VuejsDatatableFactory);

interface ISelectionChoices {
    itemType: string;
    hasValue: boolean;
    choices?: ISelector[];
}

VuejsDatatableFactory.registerTableType<any, any, any, any, any>(
  'certificate-table',
  tableType => tableType
    .setFilterHandler( ( source, filter, columns ) => ( {
        // See https://documenter.getpostman.com/view/2025350/RWaEzAiG#json-field-masking
        filter: columns.map( col => colFieldToStr( col.field! ).replace( /\./g, '/' ) ).join( ',' ),
    } ) )

    .setSortHandler( ( endpointDesc, sortColumn, sortDir ) => ( {
        ...endpointDesc,

        ...( sortColumn && sortDir ? {
            order: sortDir,
            sort:  colFieldToStr( sortColumn.field! ).replace( /\./g, '/' ),
        } : {} ),
     } ) )

    .setPaginateHandler( ( endpointDesc, perPage, pageIndex ) => ( {
        ...endpointDesc,

        ...( perPage !== null ? {
            limit:  perPage || 20,
            offset: ( ( pageIndex - 1 ) * perPage ) || 0,
        } : {} ),
    } ) )

    // Alias our process steps, because the source, here, is our API url, and paged is the complete query string
    .setDisplayHandler(
      async ( { source: baseEndPoint, paged: endpointDesc } ) => {
        const delimit = baseEndPoint.includes('?') ? '&' : '?';
        const url = `${ baseEndPoint}${delimit}${ makeQueryStringFromObj( endpointDesc ) }`;

        const {
          // Data to display
          data,
          // Get the total number of matched items
          headers: { 'x-total-count': totalCount },
        } = await axios.get(url);

        return {
          rows: data,
          totalRowCount: parseInt( totalCount, 10 ),
        } as ITableContentParam<ICertificateView>;
      }
    )
  );

@Component
export default class CertList extends Vue {

  @Inject('loginService')
  private loginService: () => LoginService;

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public certSelectionItems: ICertificateSelectionData[] = [
    { itemName: 'subject', itemType: 'string', itemDefaultSelector: 'LIKE', itemDefaultValue: 'trustable'},
    { itemName: 'issuer', itemType: 'string', itemDefaultSelector: null, itemDefaultValue: null},
    { itemName: 'serial', itemType: 'number', itemDefaultSelector: null, itemDefaultValue: null},
    { itemName: 'validTo', itemType: 'date', itemDefaultSelector: 'AFTER', itemDefaultValue: '{now}'},
    { itemName: 'revoked', itemType: 'boolean', itemDefaultSelector: 'ISTRUE', itemDefaultValue: 'true'},
    { itemName: 'revocationReason', itemType: 'set', itemDefaultSelector: 'EQUAL', itemDefaultValue: 'true' , values: ['keyCompromise',
      'cACompromise',
      'affiliationChanged',
      'superseded',
      'cessationOfOperation',
      'privilegeWithdrawn',
      'aACompromise',
      'unspecified']}

  ];

  public selectionChoices: ISelectionChoices [] = [
    { itemType: 'string', hasValue: true, choices: ['EQUAL', 'NOT_EQUAL', 'LIKE', 'NOTLIKE', 'LESSTHAN', 'GREATERTHAN']},
    { itemType: 'number', hasValue: true, choices: ['EQUAL', 'NOT_EQUAL', 'LESSTHAN', 'GREATERTHAN']},
    { itemType: 'date', hasValue: true, choices: ['ON', 'BEFORE', 'AFTER']},
    { itemType: 'boolean', hasValue: false, choices: ['ISTRUE', 'ISFALSE']},
    { itemType: 'set', hasValue: false, choices: ['EQUAL', 'NOT_EQUAL']}
  ];

  public defaultFilter: ICertificateFilter = {attributeName: 'subject', attributeValue: 'trust', selector: 'LIKE'};
  public filters: ICertificateFilter[] = [this.defaultFilter];

  public addSelector() {
    const newFilter = {...this.defaultFilter};
    this.filters.push(newFilter);
  }
  public removeSelector(index: number) {
    this.filters.splice(index, 1);
  }

  public getInputType(itemName: string): string {
    const selectionItem = this.certSelectionItems.find(selections => selections.itemName === itemName);
    if ( selectionItem ) {
      return selectionItem.itemType;
    }
    return '';
  }

  public getRevocationStyle(revoked: boolean): string {
    return revoked ? 'text-decoration:line-through;' : '';
  }

  public getValidToStyle(validFromString: string, validToString: string, revoked: boolean): string {

    if ( revoked ) {
      return '';
    }

    const validTo = new Date(validToString);
    const validFrom = new Date(validFromString);

    const dateNow = new Date();
    const dateWarn = new Date();
    dateWarn.setDate( dateNow.getDate() + 35);
    const dateAlarm = new Date();
    dateAlarm.setDate( dateNow.getDate() + 10);

    if ( validTo > dateNow && validTo < dateAlarm ) {
//      window.console.info('getValidToStyle(' + validTo + '), dateNow: ' + dateNow + ' , dateWarn: ' + dateWarn + ' -> ' + (validTo > dateNow) + ' - ' + (validTo < dateWarn));
      return 'color:red;font-weight: bold;';
    } else if ( validTo > dateNow && validTo < dateWarn ) {
      return 'color:yellow; font-weight: bold;';
    } else if ( validTo > dateNow && validFrom <= dateNow ) {
      return 'color:green; font-weight: bold;';
    }
    return '';
  }

  public toLocalDate(dateAsString: string): string {

    if ( dateAsString && dateAsString.length > 8 ) {
      const dateObj = new Date(dateAsString);
      return dateObj.toLocaleDateString();
    }
    return '';
  }

  public getValueChoices(itemName: string): string[] {
    const selectionItem = this.certSelectionItems.find(selections => selections.itemName === itemName);
    if ( selectionItem ) {
      return selectionItem.values;
    }
    return [];
  }

  public getSelectorChoices(itemName: string): string[] {
//    window.console.info('getChoices(' + itemName + ')');

    const selectionItem = this.certSelectionItems.find(selections => selections.itemName === itemName);

    if ( selectionItem ) {
      const found = this.selectionChoices.find(choices => choices.itemType === selectionItem.itemType);
      window.console.info('getChoices returns ' + found );
      if ( found ) {
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
    return {height: '35px',
      width: '4px',
      margin: '2px',
      radius: '2px'
    };
  }

  el() { return '#vue-certificates'; }
  data() {

    const self = this;

    return {

      columns: [
        { label: 'id', field: 'id' },
        { label: 'subject', field: 'subject', headerClass: 'class-in-header second-class' },
        { label: 'issuer', field: 'issuer' },
        { label: 'type', field: 'type', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: 'length', field: 'keyLength', align: 'right' },
        { label: 'serial', field: 'serial', align: 'right',
representedAs: row => `${(row.serial.length > 12) ? row.serial.substring(0, 6).concat('...', row.serial.substring(row.serial.length - 4, row.serial.length - 1 )) : row.serial}` },
        { label: 'validFrom', field: 'validFrom'  },
        { label: 'validTo', field: 'validTo' },
        { label: 'hashAlgo', field: 'hashAlgorithm' },
        { label: 'paddingAlgo', field: 'paddingAlgorithm' },
        { label: 'revoked', field: 'revoked', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: 'revokedSince', field: 'revokedSince' },
        { label: 'reason', field: 'revocationReason' }
 //       { label: 'signingAlgo', field: 'signingAlgorithm' },
      ] as TColumnsDefinition<ICertificateView>,
      page: 1,
      filter: '',

//      certApiUrl: 'publicapi/certificateList',

      get certApiUrl() {
        const filterLen = self.filters.length;

        const params = {};
        for ( let i = 0; i < filterLen; i++) {
          const filter = self.filters[i];
          const idx = i + 1;
          params['attributeName_' + idx] = filter.attributeName;
          params['attributeValue_' + idx] = filter.attributeValue;
          params['attributeSelector_' + idx] = filter.selector;
        }

        const baseApiUrl = 'publicapi/certificateList';
        const url = `${baseApiUrl}?${makeQueryStringFromObj(params)}`;

        return url;
      }

    };
  }

}
