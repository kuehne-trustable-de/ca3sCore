import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';
import { IProtectedContent, ProtectedContent } from '@/shared/model/protected-content.model';
import ProtectedContentService from './protected-content.service';

const validations: any = {
  protectedContent: {
    contentBase64: {
      required
    },
    type: {
      required
    },
    relationType: {},
    relatedId: {}
  }
};

@Component({
  validations
})
export default class ProtectedContentUpdate extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('protectedContentService') private protectedContentService: () => ProtectedContentService;
  public protectedContent: IProtectedContent = new ProtectedContent();
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.protectedContentId) {
        vm.retrieveProtectedContent(to.params.protectedContentId);
      }
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.protectedContent.id) {
      this.protectedContentService()
        .update(this.protectedContent)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.protectedContent.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.protectedContentService()
        .create(this.protectedContent)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.protectedContent.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveProtectedContent(protectedContentId): void {
    this.protectedContentService()
      .find(protectedContentId)
      .then(res => {
        this.protectedContent = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
