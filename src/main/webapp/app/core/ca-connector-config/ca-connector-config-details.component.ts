import { Component, Vue, Inject } from 'vue-property-decorator';

import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import CAConnectorConfigService from './ca-connector-config.service';

@Component
export default class CAConnectorConfigDetails extends Vue {
  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;
  public cAConnectorConfig: ICAConnectorConfig = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.cAConnectorConfigId) {
        vm.retrieveCAConnectorConfig(to.params.cAConnectorConfigId);
      }
    });
  }

  public retrieveCAConnectorConfig(cAConnectorConfigId) {
    this.cAConnectorConfigService()
      .find(cAConnectorConfigId)
      .then(res => {
        this.cAConnectorConfig = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
