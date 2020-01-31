import { Component, Vue, Inject } from 'vue-property-decorator';

import { IAcmeNonce } from '@/shared/model/acme-nonce.model';
import AcmeNonceService from './acme-nonce.service';

@Component
export default class AcmeNonceDetails extends Vue {
  @Inject('acmeNonceService') private acmeNonceService: () => AcmeNonceService;
  public acmeNonce: IAcmeNonce = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeNonceId) {
        vm.retrieveAcmeNonce(to.params.acmeNonceId);
      }
    });
  }

  public retrieveAcmeNonce(acmeNonceId) {
    this.acmeNonceService()
      .find(acmeNonceId)
      .then(res => {
        this.acmeNonce = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
