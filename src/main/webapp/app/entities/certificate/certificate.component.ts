import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { ICertificate } from '@/shared/model/certificate.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import JhiDataUtils from '@/shared/data/data-utils.service';

import CertificateService from './certificate.service';

@Component
export default class Certificate extends mixins(JhiDataUtils, Vue2Filters.mixin, AlertMixin) {
  @Inject('certificateService') private certificateService: () => CertificateService;
  private removeId: number = null;
  public itemsPerPage = 20;
  public queryCount: number = null;
  public page = 1;
  public previousPage = 1;
  public propOrder = 'id';
  public reverse = true;
  public totalItems = 0;
  public certificates: ICertificate[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllCertificates();
  }

  public clear(): void {
    this.page = 1;
    this.retrieveAllCertificates();
  }

  public retrieveAllCertificates(): void {
    this.isFetching = true;

    const paginationQuery = {
      page: this.page - 1,
      size: this.itemsPerPage,
      sort: this.sort()
    };
    this.certificateService()
      .retrieve(paginationQuery)
      .then(
        res => {
          this.certificates = res.data;
          this.totalItems = Number(res.headers['x-total-count']);
          this.queryCount = this.totalItems;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: ICertificate): void {
    this.removeId = instance.id;
  }

  public removeCertificate(): void {
    this.certificateService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.certificate.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllCertificates();
        this.closeDialog();
      });
  }

  public sort(): Array<any> {
    const result = [this.propOrder + ',' + (this.reverse ? 'asc' : 'desc')];
    if (this.propOrder !== 'id') {
      result.push('id');
    }
    return result;
  }

  public loadPage(page: number): void {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.transition();
    }
  }

  public transition(): void {
    this.retrieveAllCertificates();
  }

  public changeOrder(propOrder): void {
    this.propOrder = propOrder;
    this.reverse = !this.reverse;
    this.transition();
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
