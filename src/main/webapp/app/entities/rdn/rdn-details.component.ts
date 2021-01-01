import { Component, Vue, Inject } from 'vue-property-decorator';

import { IRDN } from '@/shared/model/rdn.model';
import RDNService from './rdn.service';

@Component
export default class RDNDetails extends Vue {
  @Inject('rDNService') private rDNService: () => RDNService;
  public rDN: IRDN = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.rDNId) {
        vm.retrieveRDN(to.params.rDNId);
      }
    });
  }

  public retrieveRDN(rDNId) {
    this.rDNService()
      .find(rDNId)
      .then(res => {
        this.rDN = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
