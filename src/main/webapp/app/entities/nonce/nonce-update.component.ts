import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';
import { INonce, Nonce } from '@/shared/model/nonce.model';
import NonceService from './nonce.service';

const validations: any = {
  nonce: {
    nonceValue: {},
    expiresAt: {}
  }
};

@Component({
  validations
})
export default class NonceUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('nonceService') private nonceService: () => NonceService;
  public nonce: INonce = new Nonce();
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.nonceId) {
        vm.retrieveNonce(to.params.nonceId);
      }
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.nonce.id) {
      this.nonceService()
        .update(this.nonce)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.nonce.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.nonceService()
        .create(this.nonce)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.nonce.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveNonce(nonceId): void {
    this.nonceService()
      .find(nonceId)
      .then(res => {
        this.nonce = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
