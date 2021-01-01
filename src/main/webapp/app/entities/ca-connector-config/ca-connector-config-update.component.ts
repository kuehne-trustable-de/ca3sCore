import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import ProtectedContentService from '../protected-content/protected-content.service';
import { IProtectedContent } from '@/shared/model/protected-content.model';

import AlertService from '@/shared/alert/alert.service';
import { ICAConnectorConfig, CAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import CAConnectorConfigService from './ca-connector-config.service';

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
    active: {},
    selector: {},
    interval: {},
    plainSecret: {}
  }
};

@Component({
  validations
})
export default class CAConnectorConfigUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;
  public cAConnectorConfig: ICAConnectorConfig = new CAConnectorConfig();

  @Inject('protectedContentService') private protectedContentService: () => ProtectedContentService;

  public protectedContents: IProtectedContent[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.cAConnectorConfigId) {
        vm.retrieveCAConnectorConfig(to.params.cAConnectorConfigId);
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

  public retrieveCAConnectorConfig(cAConnectorConfigId): void {
    this.cAConnectorConfigService()
      .find(cAConnectorConfigId)
      .then(res => {
        this.cAConnectorConfig = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.protectedContentService()
      .retrieve()
      .then(res => {
        this.protectedContents = res.data;
      });
  }
}
