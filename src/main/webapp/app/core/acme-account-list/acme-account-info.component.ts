import { Component, Inject, Vue } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import axios from 'axios';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

import { IAcmeAccountView, IAcmeAccountStatusAdministration } from '@/shared/model/transfer-object.model';

import ArItem from '../csr-list/ar-item.component';

@Component({
  components: {
    ArItem,
    Fragment,
    CopyClipboardButton,
    HelpTag,
    AuditTag,
  },
})
export default class AcmeAccountInfo extends mixins(JhiDataUtils, Vue) {
  @Inject('alertService') private alertService: () => AlertService;
  public acmeAccountView: IAcmeAccountView = {};

  public acmeAccountStatusAdministration: IAcmeAccountStatusAdministration = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.id) {
        vm.fillAcmeAccountData(to.params.id);
      }
      if (to.params.accountId) {
        vm.fillAcmeAccountDataByAccountId(to.params.accountId);
      }
    });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public mounted(): void {
    window.console.info('in mounted()) ');

    window.console.info('++++++++++++++++++ route.query : ' + this.$route.query.accountId);
    if (this.$route.query.id) {
      this.fillAcmeAccountData(this.$route.query.id);
    }
    if (this.$route.query.accountId) {
      this.fillAcmeAccountDataByAccountId(this.$route.query.accountId);
    }
  }

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public isRAOfficer() {
    return this.hasRole('ROLE_RA') || this.hasRole('ROLE_RA_DOMAIN');
  }

  public isAdmin() {
    return this.hasRole('ROLE_ADMIN');
  }

  public hasRole(targetRole: string) {
    for (const role of this.$store.getters.account.authorities) {
      if (targetRole === role) {
        return true;
      }
    }
    return false;
  }

  public get roles(): string {
    return this.$store.getters.account ? this.$store.getters.account.authorities[0] : '';
  }

  public getUsername(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public fillAcmeAccountData(id): void {
    window.console.info('calling fillAcmeAccountData');
    const self = this;

    axios({
      method: 'get',
      url: 'api/acmeAccountViews/' + encodeURIComponent(id),
      responseType: 'stream',
    }).then(function (response) {
      self.acmeAccountView = response.data;
      window.console.info('acmeAccountView :' + self.acmeAccountView);
    });
  }

  public fillAcmeAccountDataByAccountId(accountId): void {
    window.console.info('calling fillAcmeAccountData');
    const self = this;

    axios({
      method: 'get',
      url: 'api/acmeAccountViews/acmeAccoundId/' + encodeURIComponent(accountId),
      responseType: 'stream',
    }).then(function (response) {
      self.acmeAccountView = response.data;
      window.console.info('acmeAccountView :' + self.acmeAccountView);
    });
  }

  public deactivateAccount(): void {
    window.console.info('calling updateAcmeAccountStatus(deactivated)');
    this.updateAcmeAccountStatus('deactivated');
  }

  public reactivateAccount(): void {
    window.console.info('calling updateAcmeAccountStatus(valid)');
    this.updateAcmeAccountStatus('valid');
  }

  public async updateAcmeAccountStatus(accountStatus) {
    const statusUpdateURL = '/api/acme-accounts/' + this.acmeAccountView.id + '/status';

    this.acmeAccountStatusAdministration.status = accountStatus;
    try {
      document.body.style.cursor = 'wait';
      const response = await axios.post(`${statusUpdateURL}`, this.acmeAccountStatusAdministration);

      this.fillAcmeAccountData(this.acmeAccountView.id);
      this.acmeAccountStatusAdministration.comment = '';
      document.body.style.cursor = 'default';
    } catch (error) {
      console.error('####################' + error);
      document.body.style.cursor = 'default';

      const message = this.$t('problem processing request: ' + error);
      this.alertService().showAlert(message, 'info');
    }
  }
}
