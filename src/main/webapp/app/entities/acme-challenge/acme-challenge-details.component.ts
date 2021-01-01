import { Component, Vue, Inject } from 'vue-property-decorator';

import { IAcmeChallenge } from '@/shared/model/acme-challenge.model';
import AcmeChallengeService from './acme-challenge.service';

@Component
export default class AcmeChallengeDetails extends Vue {
  @Inject('acmeChallengeService') private acmeChallengeService: () => AcmeChallengeService;
  public acmeChallenge: IAcmeChallenge = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeChallengeId) {
        vm.retrieveAcmeChallenge(to.params.acmeChallengeId);
      }
    });
  }

  public retrieveAcmeChallenge(acmeChallengeId) {
    this.acmeChallengeService()
      .find(acmeChallengeId)
      .then(res => {
        this.acmeChallenge = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
