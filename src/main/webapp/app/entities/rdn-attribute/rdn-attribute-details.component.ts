import { Component, Vue, Inject } from 'vue-property-decorator';

import { IRDNAttribute } from '@/shared/model/rdn-attribute.model';
import RDNAttributeService from './rdn-attribute.service';

@Component
export default class RDNAttributeDetails extends Vue {
  @Inject('rDNAttributeService') private rDNAttributeService: () => RDNAttributeService;
  public rDNAttribute: IRDNAttribute = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.rDNAttributeId) {
        vm.retrieveRDNAttribute(to.params.rDNAttributeId);
      }
    });
  }

  public retrieveRDNAttribute(rDNAttributeId) {
    this.rDNAttributeService()
      .find(rDNAttributeId)
      .then(res => {
        this.rDNAttribute = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
