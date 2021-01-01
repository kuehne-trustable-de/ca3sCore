import { Component, Vue, Inject } from 'vue-property-decorator';

import { IAcmeContact } from '@/shared/model/acme-contact.model';
import AcmeContactService from './acme-contact.service';

@Component
export default class AcmeContactDetails extends Vue {
  @Inject('acmeContactService') private acmeContactService: () => AcmeContactService;
  public acmeContact: IAcmeContact = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeContactId) {
        vm.retrieveAcmeContact(to.params.acmeContactId);
      }
    });
  }

  public retrieveAcmeContact(acmeContactId) {
    this.acmeContactService()
      .find(acmeContactId)
      .then(res => {
        this.acmeContact = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
