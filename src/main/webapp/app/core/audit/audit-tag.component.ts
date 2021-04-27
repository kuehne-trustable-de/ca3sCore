import { Component, Vue, Prop } from 'vue-property-decorator';

import { ITableContentParam, TColumnsDefinition, VuejsDatatableFactory } from 'vuejs-datatable';
import { colFieldToStr, makeQueryStringFromObj } from '@/shared/utils';
import axios from 'axios';
import { IAuditTraceView, ICertificateFilter, ICertificateFilterList } from '@/shared/model/transfer-object.model';

import { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';

Vue.use(VuejsDatatableFactory);

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

  @Prop()
  public pipelineId: string;

  @Prop()
  public caConnectorId: string;

  @Prop()
  public processInfoId: string;

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
        { label: this.$t('audits.actor'), field: 'actorName' },
        { label: this.$t('audits.role'), field: 'actorRole' },
        { label: this.$t('audits.plainContent'), field: 'plainContent' },
        { label: this.$t('audits.createdOn'), field: 'createdOn' },
        { label: 'certificateId', field: 'certificateId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: 'csrId', field: 'csrId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: this.$t('audits.links'), field: 'links', headerClass: 'hiddenColumn', class: 'hiddenColumn' }
      ] as TColumnsDefinition<IAuditTraceView>,

      get auditApiUrl() {
        console.log('in auditApiUrl ... ');
        return self.buildContentAccessUrl();
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

  unescapeComma(content: string): string {
    return content.replace('%2C', ',').replace('%25', '%');
  }

  public buildContentAccessUrl(): string {
    const baseApiUrl = 'api/audit-trace-views';

    let url = baseApiUrl + '?';
    url += 'csrId=' + this.processParamForURL(this.csrId);
    url += 'certificateId=' + this.processParamForURL(this.certificateId);
    url += 'pipelineId=' + this.processParamForURL(this.pipelineId);
    url += 'caConnectorId=' + this.processParamForURL(this.caConnectorId);
    url += 'processInfoId=' + this.processParamForURL(this.processInfoId);

    window.console.info('buildContentAccessUrl: url : ' + url);

    if (this.tmpContentAccessUrl !== url) {
      this.tmpContentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change detected: ' + url);
    } else if (this.contentAccessUrl !== url) {
      this.contentAccessUrl = url;
      window.console.info('buildContentAccessUrl: change propagated: ' + url);
    }
    return url;
  }

  processParamForURL(id): string {
    if (id === null) {
      return '-1&';
    } else if (id !== undefined) {
      return id + '&';
    } else {
      return '-1&';
    }
  }

  public mounted(): void {}
}
