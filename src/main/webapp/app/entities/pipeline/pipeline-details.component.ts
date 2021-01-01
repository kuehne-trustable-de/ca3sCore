import { Component, Vue, Inject } from 'vue-property-decorator';

import { IPipeline } from '@/shared/model/pipeline.model';
import PipelineService from './pipeline.service';

@Component
export default class PipelineDetails extends Vue {
  @Inject('pipelineService') private pipelineService: () => PipelineService;
  public pipeline: IPipeline = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.pipelineId) {
        vm.retrievePipeline(to.params.pipelineId);
      }
    });
  }

  public retrievePipeline(pipelineId) {
    this.pipelineService()
      .find(pipelineId)
      .then(res => {
        this.pipeline = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
