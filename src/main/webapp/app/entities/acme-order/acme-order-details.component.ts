import { Component, Vue, Inject } from 'vue-property-decorator';

import { IAcmeOrder } from '@/shared/model/acme-order.model';
import AcmeOrderService from './acme-order.service';

@Component
export default class AcmeOrderDetails extends Vue {
  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;
  public acmeOrder: IAcmeOrder = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeOrderId) {
        vm.retrieveAcmeOrder(to.params.acmeOrderId);
      }
    });
  }

  public retrieveAcmeOrder(acmeOrderId) {
    this.acmeOrderService()
      .find(acmeOrderId)
      .then(res => {
        this.acmeOrder = res;
      });
  }

  public previousState() {
    this.$router.push('/acme-order-list');
  }
}
