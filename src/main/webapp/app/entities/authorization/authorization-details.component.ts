import { Component, Vue, Inject } from 'vue-property-decorator';

import { IAuthorization } from '@/shared/model/authorization.model';
import AuthorizationService from './authorization.service';

@Component
export default class AuthorizationDetails extends Vue {
  @Inject('authorizationService') private authorizationService: () => AuthorizationService;
  public authorization: IAuthorization = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.authorizationId) {
        vm.retrieveAuthorization(to.params.authorizationId);
      }
    });
  }

  public retrieveAuthorization(authorizationId) {
    this.authorizationService()
      .find(authorizationId)
      .then(res => {
        this.authorization = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
