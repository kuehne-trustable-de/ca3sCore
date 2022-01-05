import { Component, Inject, Vue } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import axios from 'axios';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

import { IACMEAccountView, INamedValue } from '@/shared/model/transfer-object.model';

import ArItem from '../csr-list/ar-item.component';

@Component({
  components: {
    ArItem,
    Fragment,
    CopyClipboardButton,
    HelpTag,
    AuditTag
  }
})
export default class AcmeAccountInfo extends mixins(JhiDataUtils, Vue) {
  @Inject('alertService') private alertService: () => AlertService;
  public acmeAccountView: IACMEAccountView = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.accountId) {
        vm.fillAcmeAccountData(to.params.accountId);
      }
    });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public mounted(): void {
    window.console.info('in mounted()) ');

    window.console.info('++++++++++++++++++ route.query : ' + this.$route.query.accountId);
    if (this.$route.query.accountId) {
      this.fillAcmeAccountData(this.$route.query.accountId);
    }
  }

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public isRAOfficer() {
    return this.hasRole('ROLE_RA');
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

  public fillAcmeAccountData(accountId): void {
    window.console.info('calling fillAcmeAccountData');
    const self = this;

    axios({
      method: 'get',
      url: 'api/acmeAccountViews/' + encodeURIComponent(accountId),
      responseType: 'stream'
    }).then(function(response) {
      self.acmeAccountView = response.data;
      window.console.info('acmeAccountView :' + self.acmeAccountView);
    });
  }
}
