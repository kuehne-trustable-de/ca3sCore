import { mixins } from 'vue-class-component';

import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IRDN } from '@/shared/model/rdn.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import RDNService from './rdn.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class RDN extends mixins(AlertMixin) {
  @Inject('rDNService') private rDNService: () => RDNService;
  private removeId: number = null;
  public rDNS: IRDN[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllRDNs();
  }

  public clear(): void {
    this.retrieveAllRDNs();
  }

  public retrieveAllRDNs(): void {
    this.isFetching = true;

    this.rDNService()
      .retrieve()
      .then(
        res => {
          this.rDNS = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IRDN): void {
    this.removeId = instance.id;
  }

  public removeRDN(): void {
    this.rDNService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.rDN.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllRDNs();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
