import { Component, Vue, Inject } from 'vue-property-decorator';

import { IPipelineAttribute } from '@/shared/model/pipeline-attribute.model';
import PipelineAttributeService from './pipeline-attribute.service';

@Component
export default class PipelineAttributeDetails extends Vue {
  @Inject('pipelineAttributeService') private pipelineAttributeService: () => PipelineAttributeService;
  public pipelineAttribute: IPipelineAttribute = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.pipelineAttributeId) {
        vm.retrievePipelineAttribute(to.params.pipelineAttributeId);
      }
    });
  }

  public retrievePipelineAttribute(pipelineAttributeId) {
    this.pipelineAttributeService()
      .find(pipelineAttributeId)
      .then(res => {
        this.pipelineAttribute = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
