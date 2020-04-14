import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { ICsrAttribute } from '@/shared/model/csr-attribute.model';
import CsrAttributeService from './csr-attribute.service';

@Component
export default class CsrAttributeDetails extends mixins(JhiDataUtils) {
  @Inject('csrAttributeService') private csrAttributeService: () => CsrAttributeService;
  public csrAttribute: ICsrAttribute = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.csrAttributeId) {
        vm.retrieveCsrAttribute(to.params.csrAttributeId);
      }
    });
  }

  public retrieveCsrAttribute(csrAttributeId) {
    this.csrAttributeService()
      .find(csrAttributeId)
      .then(res => {
        this.csrAttribute = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
