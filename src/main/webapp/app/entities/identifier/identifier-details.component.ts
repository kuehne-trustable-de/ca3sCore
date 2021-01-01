import { Component, Vue, Inject } from 'vue-property-decorator';

import { IIdentifier } from '@/shared/model/identifier.model';
import IdentifierService from './identifier.service';

@Component
export default class IdentifierDetails extends Vue {
  @Inject('identifierService') private identifierService: () => IdentifierService;
  public identifier: IIdentifier = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.identifierId) {
        vm.retrieveIdentifier(to.params.identifierId);
      }
    });
  }

  public retrieveIdentifier(identifierId) {
    this.identifierService()
      .find(identifierId)
      .then(res => {
        this.identifier = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
