import { mixins } from 'vue-class-component';

import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';

import JhiDataUtils from '@/shared/data/data-utils.service';

import BPMNProcessInfoService from './bpmn-process-info.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class BPMNProcessInfo extends mixins(JhiDataUtils) {
  @Inject('bPMNProcessInfoService') private bPMNProcessInfoService: () => BPMNProcessInfoService;
  private removeId: number = null;

  public bPMNProcessInfos: IBPMNProcessInfo[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllBPMNProcessInfos();
  }

  public clear(): void {
    this.retrieveAllBPMNProcessInfos();
  }

  public retrieveAllBPMNProcessInfos(): void {
    this.isFetching = true;

    this.bPMNProcessInfoService()
      .retrieve()
      .then(
        res => {
          this.bPMNProcessInfos = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public handleSyncList(): void {
    this.clear();
  }

  public prepareRemove(instance: IBPMNProcessInfo): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeBPMNProcessInfo(): void {
    const self = this;

    this.bPMNProcessInfoService()
      .delete(this.removeId)
      .then(() => {
        const message = self.$t('tmpGenApp.bPMNProcessInfo.deleted', { param: self.removeId });
        self.alertService().showAlert(message, 'info');
        self.removeId = null;
        self.retrieveAllBPMNProcessInfos();
        self.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
