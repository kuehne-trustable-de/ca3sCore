import { Component, Vue, Inject } from 'vue-property-decorator';

import axios from 'axios';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import CAConnectorConfigService from '../../entities/ca-connector-config/ca-connector-config.service';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';

import BPNMProcessInfoService from '../../entities/bpnm-process-info/bpnm-process-info.service';
import { IBPNMProcessInfo } from '@/shared/model/bpnm-process-info.model';
import { IPipelineView } from '@/shared/model/transfer-object.model';

import AlertService from '@/shared/alert/alert.service';
import PipelineViewService from './pipelineview.service';

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
    approvalInfo1: {},
    ipAsSubjectAllowed: {},
    ipAsSanAllowed: {}
  }
};

@Component({
  validations
})
export default class PipelineUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('pipelineViewService') private pipelineViewService: () => PipelineViewService;
  public pipeline: IPipelineView = {};

  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;

  public cAConnectorConfigs: ICAConnectorConfig[] = [];

  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;

  public allCertGenerators: CAConnectorConfigService[] = [];

  public bPNMProcessInfos: IBPNMProcessInfo[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.pipelineId) {
        vm.retrievePipeline(to.params.pipelineId, to.params.mode);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.pipeline.id) {
      this.pipelineViewService()
        .update(this.pipeline)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.pipeline.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.pipelineViewService()
        .create(this.pipeline)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.pipeline.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrievePipeline(pipelineId, mode): void {
    this.pipelineViewService()
      .find(pipelineId)
      .then(res => {
        this.pipeline = res;
        if (mode === 'copy') {
          this.pipeline.name = 'Copy of ' + this.pipeline.name;
          this.pipeline.id = null;
        }
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
/*
    this.pipelineAttributeService()
      .retrieve()
      .then(res => {
        this.pipelineAttributes = res.data;
      });
*/
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
    })
    .then(function(response) {
      window.console.info('allCertGenerators returns ' + response.data );
      self.allCertGenerators = response.data;
    });
  }
}
