import { Component, Vue, Prop } from 'vue-property-decorator';

import { ITableContentParam, TColumnsDefinition, VuejsDatatableFactory } from 'vuejs-datatable';
import { colFieldToStr, makeQueryStringFromObj } from '@/shared/utils';
import axios from 'axios';
import { IAuditTraceView, ICertificateFilter, ICertificateFilterList } from '@/shared/model/transfer-object.model';

import { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';

VuejsDatatableFactory.registerTableType<any, any, any, any, any>('audits-table', tableType =>
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

      window.console.info('audit data : ' + totalCount + ' -> ' + data.content);

      return {
        rows: data.content,
        totalRowCount: data.numberOfElements
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
export default class AuditTag extends mixins(AlertMixin, Vue) {
  @Prop()
  public showLinks: boolean;

  @Prop()
  public csrId: string;

  @Prop()
  public certificateId: string;

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public contentAccessUrl: string;
  public tmpContentAccessUrl: string;

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public toLocalDate(dateAsString: string): string {
    window.console.info('toLocalDate: ' + dateAsString);

    if (dateAsString && dateAsString.length > 8) {
      const dateObj = new Date(dateAsString);
      return dateObj.toLocaleDateString();
    }
    return '';
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
    return '#audits-table';
  }

  data() {
    const self = this;

    return {
      columns: [
        { label: 'id', field: 'id', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: this.$t('actor'), field: 'actorName' },
        { label: this.$t('role'), field: 'actorRole' },
        { label: this.$t('plainContent'), field: 'plainContent' },
        { label: this.$t('createdOn'), field: 'createdOn' },
        { label: this.$t('certificateId'), field: 'certificateId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: this.$t('csrId'), field: 'csrId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: this.$t('links'), field: 'links', headerClass: 'hiddenColumn', class: 'hiddenColumn' }
      ] as TColumnsDefinition<IAuditTraceView>,

      get auditApiUrl() {
        let apiUrl = self.buildContentAccessUrl();
        window.console.info('csrApiUrl returns : ' + apiUrl);
        return apiUrl;
      },

      page: 1,
      filter: '',
      contentAccessUrl: ''
    };
  }

  // refesh table by pressing 'enter'
  public updateTable() {
    //    window.console.debug('updateTable: enter pressed ...');
    this.buildContentAccessUrl();
    this.buildContentAccessUrl();
  }

  public buildContentAccessUrl(): string {
    const baseApiUrl = 'api/audit-trace-views';

    window.console.info('buildContentAccessUrl: csrId : ' + this.csrId + ', certificateId : ' + this.certificateId);

    let url = baseApiUrl + '?';
    if (this.csrId !== undefined) {
      url += 'csrId=' + this.csrId + '&';
    }
    if (this.certificateId !== undefined) {
      url += 'certificateId=' + this.certificateId + '&';
    }

    if (this.tmpContentAccessUrl !== url) {
      this.tmpContentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change detected: ' + url);
    } else if (this.contentAccessUrl !== url) {
      this.contentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change propagated: ' + url);
    }
    return url;
  }

  public _buildContentAccessUrl(): string {
    const filters: ICertificateFilterList = { filterList: [] };

    if (this.csrId !== undefined) {
      filters.filterList.push({ attributeName: 'csrId', attributeValue: this.csrId, selector: 'EQUAL' });
    }
    if (this.certificateId !== undefined) {
      filters.filterList.push({ attributeName: 'certificateId', attributeValue: this.certificateId, selector: 'EQUAL' });
    }

    const filterLen = filters.filterList.length;

    const params = {};
    for (let i = 0; i < filterLen; i++) {
      const filter = filters.filterList[i];
      const idx = i + 1;
      params['attributeName_' + idx] = filter.attributeName;
      params['attributeValue_' + idx] = filter.attributeValue;
      params['attributeSelector_' + idx] = filter.selector;
    }

    const baseApiUrl = 'api/auditTraceList';

    window.console.info('buildContentAccessUrl: csrId : ' + this.csrId + ', certificateId : ' + this.certificateId);

    const url = `${baseApiUrl}?${makeQueryStringFromObj(params)}`;

    if (this.tmpContentAccessUrl !== url) {
      this.tmpContentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change detected: ' + url);
    } else if (this.contentAccessUrl !== url) {
      this.contentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change propagated: ' + url);
    }
    return url;
  }

  public mounted(): void {}
}
