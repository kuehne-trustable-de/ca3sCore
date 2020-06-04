import { Component, Vue, Inject } from 'vue-property-decorator';

import { IPipelineView } from '@/shared/model/transfer-object.model';
import PipelineViewService from './pipelineview.service';

@Component
export default class PipelineDetails extends Vue {
  @Inject('pipelineViewService') private pipelineViewService: () => PipelineViewService;
  public pipeline: IPipelineView = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.pipelineId) {
        vm.retrievePipeline(to.params.pipelineId);
      }
    });
  }

  public retrievePipeline(pipelineId) {
    this.pipelineViewService()
      .find(pipelineId)
      .then(res => {
        this.pipeline = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
