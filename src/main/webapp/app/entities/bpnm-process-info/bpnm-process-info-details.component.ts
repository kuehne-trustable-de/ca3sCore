import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { IBPNMProcessInfo } from '@/shared/model/bpmn-process-info.model';
import BPNMProcessInfoService from './bpnm-process-info.service';

@Component
export default class BPNMProcessInfoDetails extends mixins(JhiDataUtils) {
  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;
  public bPNMProcessInfo: IBPNMProcessInfo = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.bPNMProcessInfoId) {
        vm.retrieveBPNMProcessInfo(to.params.bPNMProcessInfoId);
      }
    });
  }

  public retrieveBPNMProcessInfo(bPNMProcessInfoId) {
    this.bPNMProcessInfoService()
      .find(bPNMProcessInfoId)
      .then(res => {
        this.bPNMProcessInfo = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
