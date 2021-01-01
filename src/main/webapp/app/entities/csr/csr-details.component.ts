import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { ICSR } from '@/shared/model/csr.model';
import CSRService from './csr.service';

@Component
export default class CSRDetails extends mixins(JhiDataUtils) {
  @Inject('cSRService') private cSRService: () => CSRService;
  public cSR: ICSR = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.cSRId) {
        vm.retrieveCSR(to.params.cSRId);
      }
    });
  }

  public retrieveCSR(cSRId) {
    this.cSRService()
      .find(cSRId)
      .then(res => {
        this.cSR = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
