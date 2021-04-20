import { Component, Vue, Inject } from 'vue-property-decorator';

import axios from 'axios';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import PipelineAttributeService from '../pipeline-attribute/pipeline-attribute.service';
import { IPipelineAttribute } from '@/shared/model/pipeline-attribute.model';

import CAConnectorConfigService from '../ca-connector-config/ca-connector-config.service';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';

import BPNMProcessInfoService from '../bpnm-process-info/bpnm-process-info.service';
import { IBPNMProcessInfo } from '@/shared/model/bpmn-process-info.model';

import AlertService from '@/shared/alert/alert.service';
import { IPipeline, Pipeline } from '@/shared/model/pipeline.model';
import PipelineService from './pipeline.service';

const validations: any = {
  pipeline: {
    name: {
      required
    },
    type: {
      required
    },
    urlPart: {},
    description: {},
    approvalRequired: {},
    approvalInfo1: {}
  }
};

@Component({
  validations
})
export default class PipelineUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('pipelineService') private pipelineService: () => PipelineService;
  public pipeline: IPipeline = new Pipeline();

  @Inject('pipelineAttributeService') private pipelineAttributeService: () => PipelineAttributeService;

  public pipelineAttributes: IPipelineAttribute[] = [];

  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;

  public cAConnectorConfigs: ICAConnectorConfig[] = [];

  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;

  public allCertGenerators: CAConnectorConfigService[] = [];

  public bPNMProcessInfos: IBPNMProcessInfo[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.pipelineId) {
        vm.retrievePipeline(to.params.pipelineId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.pipeline.id) {
      this.pipelineService()
        .update(this.pipeline)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.pipeline.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.pipelineService()
        .create(this.pipeline)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.pipeline.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrievePipeline(pipelineId): void {
    this.pipelineService()
      .find(pipelineId)
      .then(res => {
        this.pipeline = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.pipelineAttributeService()
      .retrieve()
      .then(res => {
        this.pipelineAttributes = res.data;
      });
    this.cAConnectorConfigService()
      .retrieve()
      .then(res => {
        this.cAConnectorConfigs = res.data;
      });
    this.bPNMProcessInfoService()
      .retrieve()
      .then(res => {
        this.bPNMProcessInfos = res.data;
      });
  }

  public mounted(): void {
    this.fillData();
  }

  public fillData(): void {
    window.console.info('calling fillData ');
    const self = this;

    axios({
      method: 'get',
      url: 'api/ca-connector-configs/cert-generators',
      responseType: 'stream'
    }).then(function(response) {
      window.console.info('allCertGenerators returns ' + response.data);
      self.allCertGenerators = response.data;
    });
  }
}
