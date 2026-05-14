import format from 'date-fns/format';
import { Component, Inject } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';
import Vue2Filters from 'vue2-filters';
import axios from "axios";

@Component
export default class CrlUrlStatus extends mixins(Vue2Filters.mixin) {
  public crlUrlStatusList: any = [];
  public fromDate: any = null;
  public itemsPerPage = 20;
  public queryCount: any = null;
  public page = 1;
  public previousPage = 1;
  public propOrder = 'auditEventDate';
  public predicate = 'timestamp';
  public reverse = false;
  public toDate: any = null;
  public totalItems = 0;
  public isFetching = false;

  public mounted(): void {
    this.init();
  }

  public init(): void {
    this.today();
    this.previousMonth();
    this.loadAll();
  }

  public previousMonth(): void {
    const dateFormat = 'yyyy-MM-dd';
    let fromDate = new Date();

    if (fromDate.getMonth() === 0) {
      fromDate = new Date(fromDate.getFullYear() - 1, 11, fromDate.getDate());
    } else {
      fromDate = new Date(fromDate.getFullYear(), fromDate.getMonth() - 1, fromDate.getDate());
    }

    this.fromDate = format(fromDate, dateFormat);
  }

  public today(): void {
    const dateFormat = 'yyyy-MM-dd';
    // Today + 1 day - needed if the current day must be included
    const today = new Date();
    today.setDate(today.getDate() + 1);
    const date = new Date(today.getFullYear(), today.getMonth(), today.getDate());

    this.toDate = format(date, dateFormat);
  }

  public loadAll(): void {
     this.fillCrlUrlData();
/*
      this.isFetching = true;
//    if (this.fromDate && this.toDate) {
      this.crlurlStatusService()
        .query({
          page: this.page - 1,
          size: this.itemsPerPage,
          sort: this.sort(),
          fromDate: this.fromDate,
          toDate: this.toDate
        })
        .then(
          res => {
            this.crlUrlStatusList = res.data;
            this.totalItems = Number(res.headers['x-total-count']);
            this.queryCount = this.totalItems;
            this.isFetching = false;
          },
          err => {
            this.isFetching = false;
          }
        );
  //  }

 */
  }

  public sort(): any {
    const result = [this.propOrder + ',' + (this.reverse ? 'asc' : 'desc')];
    result.push('id');
    return result;
  }

  public loadPage(page): void {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.transition();
    }
  }

  public transition(): void {
    this.loadAll();
  }

  public changeOrder(propOrder: string, predicate: string): void {
    this.propOrder = propOrder;
    this.predicate = predicate;
    this.reverse = !this.reverse;
    this.transition();
  }

  public fillCrlUrlData(): void {
    window.console.info('calling fillCrlUrlData');
    const self = this;

    axios({
      method: 'get',
      url: 'api/crl-url-status-views',
      responseType: 'stream',
    }).then(function (response) {
      self.crlUrlStatusList = response.data;
      window.console.info('crlUrlStatusList :' + self.crlUrlStatusList);
    });
  }

}
