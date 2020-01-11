import { Component, Vue, Inject } from 'vue-property-decorator';

import { IRequestAttributeValue } from '@/shared/model/request-attribute-value.model';
import RequestAttributeValueService from './request-attribute-value.service';

@Component
export default class RequestAttributeValueDetails extends Vue {
  @Inject('requestAttributeValueService') private requestAttributeValueService: () => RequestAttributeValueService;
  public requestAttributeValue: IRequestAttributeValue = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.requestAttributeValueId) {
        vm.retrieveRequestAttributeValue(to.params.requestAttributeValueId);
      }
    });
  }

  public retrieveRequestAttributeValue(requestAttributeValueId) {
    this.requestAttributeValueService()
      .find(requestAttributeValueId)
      .then(res => {
        this.requestAttributeValue = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
