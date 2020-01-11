import { Component, Vue, Inject } from 'vue-property-decorator';

import { INonce } from '@/shared/model/nonce.model';
import NonceService from './nonce.service';

@Component
export default class NonceDetails extends Vue {
  @Inject('nonceService') private nonceService: () => NonceService;
  public nonce: INonce = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.nonceId) {
        vm.retrieveNonce(to.params.nonceId);
      }
    });
  }

  public retrieveNonce(nonceId) {
    this.nonceService()
      .find(nonceId)
      .then(res => {
        this.nonce = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
