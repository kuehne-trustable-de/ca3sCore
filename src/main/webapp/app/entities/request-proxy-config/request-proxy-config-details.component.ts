import { Component, Vue, Inject } from 'vue-property-decorator';

import { IRequestProxyConfig } from '@/shared/model/request-proxy-config.model';
import RequestProxyConfigService from './request-proxy-config.service';

@Component
export default class RequestProxyConfigDetails extends Vue {
  @Inject('requestProxyConfigService') private requestProxyConfigService: () => RequestProxyConfigService;
  public requestProxyConfig: IRequestProxyConfig = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.requestProxyConfigId) {
        vm.retrieveRequestProxyConfig(to.params.requestProxyConfigId);
      }
    });
  }

  public retrieveRequestProxyConfig(requestProxyConfigId) {
    this.requestProxyConfigService()
      .find(requestProxyConfigId)
      .then(res => {
        this.requestProxyConfig = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
