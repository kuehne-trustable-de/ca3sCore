import { Component, Vue, Inject } from 'vue-property-decorator';

import { IImportedURL } from '@/shared/model/imported-url.model';
import ImportedURLService from './imported-url.service';

@Component
export default class ImportedURLDetails extends Vue {
  @Inject('importedURLService') private importedURLService: () => ImportedURLService;
  public importedURL: IImportedURL = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.importedURLId) {
        vm.retrieveImportedURL(to.params.importedURLId);
      }
    });
  }

  public retrieveImportedURL(importedURLId) {
    this.importedURLService()
      .find(importedURLId)
      .then(res => {
        this.importedURL = res;
      });
  }

  public previousState() {
    this.$router.go(-1);
  }
}
