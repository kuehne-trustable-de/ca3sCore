import { mixins } from 'vue-class-component';

import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { ICSR } from '@/shared/model/csr.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import JhiDataUtils from '@/shared/data/data-utils.service';

import CSRService from './csr.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class CSR extends mixins(JhiDataUtils, AlertMixin) {
  @Inject('cSRService') private cSRService: () => CSRService;
  private removeId: number = null;

  public cSRS: ICSR[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllCSRs();
  }

  public clear(): void {
    this.retrieveAllCSRs();
  }

  public retrieveAllCSRs(): void {
    this.isFetching = true;

    this.cSRService()
      .retrieve()
      .then(
        res => {
          this.cSRS = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: ICSR): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeCSR(): void {
    this.cSRService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.cSR.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllCSRs();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
