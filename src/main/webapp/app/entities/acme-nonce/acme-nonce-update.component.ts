import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';
import format from 'date-fns/format';
import parse from 'date-fns/parse';
import parseISO from 'date-fns/parseISO';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import AlertService from '@/shared/alert/alert.service';
import { IAcmeNonce, AcmeNonce } from '@/shared/model/acme-nonce.model';
import AcmeNonceService from './acme-nonce.service';

const validations: any = {
  acmeNonce: {
    nonceValue: {},
    expiresAt: {},
  },
};

@Component({
  validations,
})
export default class AcmeNonceUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('acmeNonceService') private acmeNonceService: () => AcmeNonceService;
  public acmeNonce: IAcmeNonce = new AcmeNonce();
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeNonceId) {
        vm.retrieveAcmeNonce(to.params.acmeNonceId);
      }
    });
  }

  created(): void {
    this.currentLanguage = this.$store.getters.currentLanguage;
    this.$store.watch(
      () => this.$store.getters.currentLanguage,
      () => {
        this.currentLanguage = this.$store.getters.currentLanguage;
      }
    );
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

  public convertDateTimeFromServer(date: Date): string {
    if (date) {
      return format(date, DATE_TIME_LONG_FORMAT);
    }
    return null;
  }

  public updateInstantField(field, event) {
    if (event.target.value) {
      this.acmeNonce[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.acmeNonce[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.acmeNonce[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.acmeNonce[field] = null;
    }
  }

  public retrieveAcmeNonce(acmeNonceId): void {
    this.acmeNonceService()
      .find(acmeNonceId)
      .then(res => {
        res.expiresAt = new Date(res.expiresAt);
        this.acmeNonce = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
