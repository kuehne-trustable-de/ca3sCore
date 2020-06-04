import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IPipelineView } from '@/shared/model/transfer-object.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import PipelineViewService from './pipelineview.service';

@Component
export default class Pipeline extends mixins(Vue2Filters.mixin, AlertMixin) {

  @Inject('pipelineViewService') private pipelineViewService: () => PipelineViewService;
  private removeId: number = null;
  public pipelines: IPipelineView[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllPipelines();
  }

  public clear(): void {
    this.retrieveAllPipelines();
  }

  public retrieveAllPipelines(): void {
    this.isFetching = true;

    this.pipelineViewService()
      .retrieve()
      .then(
        res => {
          this.pipelines = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IPipelineView): void {
    this.removeId = instance.id;
  }

  public removePipeline(): void {
    this.pipelineViewService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.pipeline.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllPipelines();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
