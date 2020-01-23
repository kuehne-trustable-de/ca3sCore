import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { IProtectedContent } from '@/shared/model/protected-content.model';
import ProtectedContentService from './protected-content.service';

@Component
export default class ProtectedContentDetails extends mixins(JhiDataUtils) {
  @Inject('protectedContentService') private protectedContentService: () => ProtectedContentService;
  public protectedContent: IProtectedContent = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.protectedContentId) {
        vm.retrieveProtectedContent(to.params.protectedContentId);
      }
    });
  }

  public retrieveProtectedContent(protectedContentId) {
    this.protectedContentService()
      .find(protectedContentId)
      .then(res => {
        this.protectedContent = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
