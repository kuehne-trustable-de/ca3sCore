import { mixins } from 'vue-class-component';

import { Component, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { IPipelineAttribute } from '@/shared/model/pipeline-attribute.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import PipelineAttributeService from './pipeline-attribute.service';

@Component
export default class PipelineAttribute extends mixins(Vue2Filters.mixin, AlertMixin) {
  @Inject('pipelineAttributeService') private pipelineAttributeService: () => PipelineAttributeService;
  private removeId: number = null;
  public pipelineAttributes: IPipelineAttribute[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllPipelineAttributes();
  }

  public clear(): void {
    this.retrieveAllPipelineAttributes();
  }

  public retrieveAllPipelineAttributes(): void {
    this.isFetching = true;

    this.pipelineAttributeService()
      .retrieve()
      .then(
        res => {
          this.pipelineAttributes = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: IPipelineAttribute): void {
    this.removeId = instance.id;
  }

  public removePipelineAttribute(): void {
    this.pipelineAttributeService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.pipelineAttribute.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();

        this.removeId = null;
        this.retrieveAllPipelineAttributes();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
