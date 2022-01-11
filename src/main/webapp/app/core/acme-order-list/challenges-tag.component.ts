import { Component, Vue, Prop } from 'vue-property-decorator';

import { ITableContentParam, TColumnsDefinition, VuejsDatatableFactory } from 'vuejs-datatable';
import { colFieldToStr, makeQueryStringFromObj } from '@/shared/utils';
import axios from 'axios';
import { IACMEChallengeView } from '@/shared/model/transfer-object.model';

import { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';

Vue.use(VuejsDatatableFactory);

VuejsDatatableFactory.registerTableType<any, any, any, any, any>('challenges-table', tableType =>
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

      window.console.info('challenge data : ' + totalCount + ' -> ' + data);

      return {
        rows: data,
        totalRowCount: data.numberOfElements
      } as ITableContentParam<IACMEChallengeView>;
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
export default class ChallengesTag extends mixins(AlertMixin, Vue) {
  @Prop()
  public orderId: number;

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
    return '#challenges-table';
  }

  data() {
    const self = this;

    return {
      columns: [
        { label: 'challengeId', field: 'challengeId', headerClass: 'hiddenColumn', class: 'hiddenColumn' },
        { label: this.$t('ca3SApp.acmeChallenge.status'), field: 'status' },
        //        { label: this.$t('challenges.authorizationType'), field: 'authorizationType' },
        //        { label: this.$t('challenges.authorizationValue'), field: 'authorizationValue' },
        { label: this.$t('ca3SApp.acmeChallenge.type'), field: 'type' },
        { label: this.$t('ca3SApp.acmeChallenge.target'), field: 'value' },
        { label: this.$t('ca3SApp.acmeChallenge.updatedOn'), field: 'validated' }
      ] as TColumnsDefinition<IACMEChallengeView>,

      get challengesApiUrl() {
        console.log('challengesApiUrl returning ' + self.buildContentAccessUrl());
        return self.buildContentAccessUrl();
      },
      page: 1,
      filter: '',
      contentAccessUrl: ''
    };
  }

  // refresh table by pressing 'enter'
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
    return content.replace(new RegExp('%2C', 'g'), ',').replace(new RegExp('%25', 'g'), '%');
  }

  public buildContentAccessUrl(): string {
    const baseApiUrl = 'api/acmeOrderView/' + this.orderId + '/challenges';

    let url = baseApiUrl;

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
  /*
  processParamForURL(id): string {
    if (id === null) {
      return '-1&';
    } else if (id !== undefined) {
      return id + '&';
    } else {
      return '-1&';
    }
  }
*/
  public mounted(): void {
    window.console.info('in mounted()');
  }
}
