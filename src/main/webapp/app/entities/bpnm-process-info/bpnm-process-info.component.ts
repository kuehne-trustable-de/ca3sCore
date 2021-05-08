import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import JhiDataUtils from '@/shared/data/data-utils.service';

import BPNMProcessInfoService from './bpnm-process-info.service';

@Component
export default class BPNMProcessInfo extends mixins(JhiDataUtils, Vue2Filters.mixin, AlertMixin) {
  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;
  private removeId: number = null;
  public bPNMProcessInfos: IBPMNProcessInfo[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllBPNMProcessInfos();
  }

  public clear(): void {
    this.retrieveAllBPNMProcessInfos();
  }

  public retrieveAllBPNMProcessInfos(): void {
    this.isFetching = true;

    this.bPNMProcessInfoService()
      .retrieve()
      .then(
        res => {
          this.bPNMProcessInfos = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IBPMNProcessInfo): void {
    this.removeId = instance.id;
  }

  public removeBPNMProcessInfo(): void {
    this.bPNMProcessInfoService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.bPNMProcessInfo.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllBPNMProcessInfos();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
