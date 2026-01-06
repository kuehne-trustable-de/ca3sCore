import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import AcmeContactService from '../acme-contact/acme-contact.service';
import { IAcmeContact } from '@/shared/model/acme-contact.model';

import AcmeOrderService from '../acme-order/acme-order.service';
import { IAcmeOrder } from '@/shared/model/acme-order.model';

import AlertService from '@/shared/alert/alert.service';
import { IAcmeAccount, AcmeAccount } from '@/shared/model/acme-account.model';
import AcmeAccountService from './acme-account.service';

const validations: any = {
  aCMEAccount: {
    accountId: {
      required,
      numeric,
    },
    realm: {
      required,
    },
    status: {},
    termsOfServiceAgreed: {
      required,
    },
    publicKeyHash: {
      required,
    },
    publicKey: {
      required,
    },
  },
};

@Component({
  validations,
})
export default class AcmeAccountUpdate extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('aCMEAccountService') private aCMEAccountService: () => AcmeAccountService;
  public aCMEAccount: IAcmeAccount = new AcmeAccount();

  @Inject('acmeContactService') private acmeContactService: () => AcmeContactService;

  public acmeContacts: IAcmeContact[] = [];

  @Inject('acmeOrderService') private acmeOrderService: () => AcmeOrderService;

  public acmeOrders: IAcmeOrder[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.aCMEAccountId) {
        vm.retrieveAcmeAccount(to.params.aCMEAccountId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    // console.info('saving ...');
    if (this.aCMEAccount.id) {
      this.aCMEAccountService()
        .update(this.aCMEAccount)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.aCMEAccount.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.aCMEAccountService()
        .create(this.aCMEAccount)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.aCMEAccount.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveAcmeAccount(aCMEAccountId): void {
    this.aCMEAccountService()
      .find(aCMEAccountId)
      .then(res => {
        this.aCMEAccount = res;
      });
  }

  public previousState(): void {
    this.$router.push('/acme-account-list');
  }

  public initRelationships(): void {
    this.acmeContactService()
      .retrieve()
      .then(res => {
        this.acmeContacts = res.data;
      });
    this.acmeOrderService()
      .retrieve()
      .then(res => {
        this.acmeOrders = res.data;
      });
  }
}
