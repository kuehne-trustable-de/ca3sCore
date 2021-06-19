import { Component, Inject } from 'vue-property-decorator';

import axios from 'axios';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';
import { ICAConnectorConfig, CAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import CAConnectorConfigService from './ca-connector-config.service';

import { ICAStatus } from '@/shared/model/transfer-object.model';
import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

const validations: any = {
  cAConnectorConfig: {
    name: {
      required
    },
    caConnectorType: {
      required
    },
    caUrl: {},
    pollingOffset: {},
    defaultCA: {},
    trustSelfsignedCertificates: {},
    active: {},
    selector: {},
    interval: {},
    plainSecret: {}
  }
};

@Component({
  validations,
  components: {
    HelpTag,
    AuditTag
  }
})
export default class CAConnectorConfigUpdate extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;
  public cAConnectorConfig: ICAConnectorConfig = new CAConnectorConfig();
  public caStatus: ICAStatus = 'Unknown';

  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    const self = this;
    next(vm => {
      if (to.params.cAConnectorConfigId) {
        vm.retrieveCAConnectorConfig(to.params.cAConnectorConfigId, to.params.mode);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.cAConnectorConfig.id) {
      this.cAConnectorConfigService()
        .update(this.cAConnectorConfig)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.cAConnectorConfig.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.cAConnectorConfigService()
        .create(this.cAConnectorConfig)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.cAConnectorConfig.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveCAConnectorConfig(cAConnectorConfigId, mode): void {
    this.cAConnectorConfigService()
      .find(cAConnectorConfigId)
      .then(res => {
        this.cAConnectorConfig = res;
        if (mode === 'copy') {
          this.cAConnectorConfig.name = 'Copy of ' + this.cAConnectorConfig.name;
          this.cAConnectorConfig.id = null;
          this.cAConnectorConfig.plainSecret = null;
        }
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}

  public testCaConnectorConfig(): void {
    window.console.info('calling checkCaConnectorConfig ');
    const self = this;

    axios({
      method: 'post',
      url: 'api/ca-connector-configs/getStatus',
      data: self.cAConnectorConfig,
      responseType: 'stream'
    }).then(function(response) {
      window.console.info('testCaConnectorConfig returns ' + response.data);
      self.caStatus = response.data;
    });
  }
}
