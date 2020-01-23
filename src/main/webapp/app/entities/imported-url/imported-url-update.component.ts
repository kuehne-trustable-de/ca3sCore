import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';
import { IImportedURL, ImportedURL } from '@/shared/model/imported-url.model';
import ImportedURLService from './imported-url.service';

const validations: any = {
  importedURL: {
    name: {
      required
    },
    importDate: {
      required
    }
  }
};

@Component({
  validations
})
export default class ImportedURLUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('importedURLService') private importedURLService: () => ImportedURLService;
  public importedURL: IImportedURL = new ImportedURL();
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.importedURLId) {
        vm.retrieveImportedURL(to.params.importedURLId);
      }
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.importedURL.id) {
      this.importedURLService()
        .update(this.importedURL)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.importedURL.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.importedURLService()
        .create(this.importedURL)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.importedURL.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveImportedURL(importedURLId): void {
    this.importedURLService()
      .find(importedURLId)
      .then(res => {
        this.importedURL = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
