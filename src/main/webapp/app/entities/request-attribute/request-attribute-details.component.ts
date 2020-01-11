import { Component, Vue, Inject } from 'vue-property-decorator';

import { IRequestAttribute } from '@/shared/model/request-attribute.model';
import RequestAttributeService from './request-attribute.service';

@Component
export default class RequestAttributeDetails extends Vue {
  @Inject('requestAttributeService') private requestAttributeService: () => RequestAttributeService;
  public requestAttribute: IRequestAttribute = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.requestAttributeId) {
        vm.retrieveRequestAttribute(to.params.requestAttributeId);
      }
    });
  }

  public retrieveRequestAttribute(requestAttributeId) {
    this.requestAttributeService()
      .find(requestAttributeId)
      .then(res => {
        this.requestAttribute = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
