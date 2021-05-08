import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';
import BPMNProcessInfoService from './bpmn-process-info.service';

@Component
export default class BPMNProcessInfoDetails extends mixins(JhiDataUtils) {
  @Inject('bPMNProcessInfoService') private bPMNProcessInfoService: () => BPMNProcessInfoService;
  public bPMNProcessInfo: IBPMNProcessInfo = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.bPMNProcessInfoId) {
        vm.retrieveBPMNProcessInfo(to.params.bPMNProcessInfoId);
      }
    });
  }

  public retrieveBPMNProcessInfo(bPMNProcessInfoId) {
    this.bPMNProcessInfoService()
      .find(bPMNProcessInfoId)
      .then(res => {
        this.bPMNProcessInfo = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
