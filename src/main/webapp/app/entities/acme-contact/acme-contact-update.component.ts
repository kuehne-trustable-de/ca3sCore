import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import ACMEAccountService from '../acme-account/acme-account.service';
import { IACMEAccount } from '@/shared/model/acme-account.model';

import AlertService from '@/shared/alert/alert.service';
import { IAcmeContact, AcmeContact } from '@/shared/model/acme-contact.model';
import AcmeContactService from './acme-contact.service';

const validations: any = {
  acmeContact: {
    contactId: {
      required,
      numeric
    },
    contactUrl: {
      required
    }
  }
};

@Component({
  validations
})
export default class AcmeContactUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('acmeContactService') private acmeContactService: () => AcmeContactService;
  public acmeContact: IAcmeContact = new AcmeContact();

  @Inject('aCMEAccountService') private aCMEAccountService: () => ACMEAccountService;

  public aCMEAccounts: IACMEAccount[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.acmeContactId) {
        vm.retrieveAcmeContact(to.params.acmeContactId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.acmeContact.id) {
      this.acmeContactService()
        .update(this.acmeContact)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeContact.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.acmeContactService()
        .create(this.acmeContact)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.acmeContact.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveAcmeContact(acmeContactId): void {
    this.acmeContactService()
      .find(acmeContactId)
      .then(res => {
        this.acmeContact = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.aCMEAccountService()
      .retrieve()
      .then(res => {
        this.aCMEAccounts = res.data;
      });
  }
}
