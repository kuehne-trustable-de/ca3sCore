import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';
import { IAcmeNonce, AcmeNonce } from '@/shared/model/acme-nonce.model';
import AcmeNonceService from './acme-nonce.service';

const validations: any = {
  acmeNonce: {
    nonceValue: {},
    expiresAt: {}
  }
};

@Component({
  validations
})
export default class AcmeNonceUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('acmeNonceService') private acmeNonceService: () => AcmeNonceService;
  public acmeNonce: IAcmeNonce = new AcmeNonce();
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeNonceId) {
        vm.retrieveAcmeNonce(to.params.acmeNonceId);
      }
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.acmeNonce.id) {
      this.acmeNonceService()
        .update(this.acmeNonce)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeNonce.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.acmeNonceService()
        .create(this.acmeNonce)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeNonce.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveAcmeNonce(acmeNonceId): void {
    this.acmeNonceService()
      .find(acmeNonceId)
      .then(res => {
        this.acmeNonce = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
