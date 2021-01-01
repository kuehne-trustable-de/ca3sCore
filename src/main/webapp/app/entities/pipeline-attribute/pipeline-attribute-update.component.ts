import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import PipelineService from '../pipeline/pipeline.service';
import { IPipeline } from '@/shared/model/pipeline.model';

import AlertService from '@/shared/alert/alert.service';
import { IPipelineAttribute, PipelineAttribute } from '@/shared/model/pipeline-attribute.model';
import PipelineAttributeService from './pipeline-attribute.service';

const validations: any = {
  pipelineAttribute: {
    name: {
      required
    },
    value: {
      required
    }
  }
};

@Component({
  validations
})
export default class PipelineAttributeUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('pipelineAttributeService') private pipelineAttributeService: () => PipelineAttributeService;
  public pipelineAttribute: IPipelineAttribute = new PipelineAttribute();

  @Inject('pipelineService') private pipelineService: () => PipelineService;

  public pipelines: IPipeline[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.pipelineAttributeId) {
        vm.retrievePipelineAttribute(to.params.pipelineAttributeId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.pipelineAttribute.id) {
      this.pipelineAttributeService()
        .update(this.pipelineAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.pipelineAttribute.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.pipelineAttributeService()
        .create(this.pipelineAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.pipelineAttribute.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrievePipelineAttribute(pipelineAttributeId): void {
    this.pipelineAttributeService()
      .find(pipelineAttributeId)
      .then(res => {
        this.pipelineAttribute = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.pipelineService()
      .retrieve()
      .then(res => {
        this.pipelines = res.data;
      });
  }
}
