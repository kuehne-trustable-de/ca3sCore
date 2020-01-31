import { Component, Vue, Inject } from 'vue-property-decorator';

import { IAcmeIdentifier } from '@/shared/model/acme-identifier.model';
import AcmeIdentifierService from './acme-identifier.service';

@Component
export default class AcmeIdentifierDetails extends Vue {
  @Inject('acmeIdentifierService') private acmeIdentifierService: () => AcmeIdentifierService;
  public acmeIdentifier: IAcmeIdentifier = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeIdentifierId) {
        vm.retrieveAcmeIdentifier(to.params.acmeIdentifierId);
      }
    });
  }

  public retrieveAcmeIdentifier(acmeIdentifierId) {
    this.acmeIdentifierService()
      .find(acmeIdentifierId)
      .then(res => {
        this.acmeIdentifier = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
