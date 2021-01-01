import { Component, Vue, Inject } from 'vue-property-decorator';

import { IAcmeAuthorization } from '@/shared/model/acme-authorization.model';
import AcmeAuthorizationService from './acme-authorization.service';

@Component
export default class AcmeAuthorizationDetails extends Vue {
  @Inject('acmeAuthorizationService') private acmeAuthorizationService: () => AcmeAuthorizationService;
  public acmeAuthorization: IAcmeAuthorization = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeAuthorizationId) {
        vm.retrieveAcmeAuthorization(to.params.acmeAuthorizationId);
      }
    });
  }

  public retrieveAcmeAuthorization(acmeAuthorizationId) {
    this.acmeAuthorizationService()
      .find(acmeAuthorizationId)
      .then(res => {
        this.acmeAuthorization = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
