import { Component, Inject, Vue } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import axios from 'axios';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';
import ChallengesTag from './challenges-tag.vue';

import { IACMEOrderView, INamedValue } from '@/shared/model/transfer-object.model';

@Component({
  components: {
    Fragment,
    CopyClipboardButton,
    HelpTag,
    ChallengesTag
  }
})
export default class AcmeOrderInfo extends mixins(JhiDataUtils, Vue) {
  @Inject('alertService') private alertService: () => AlertService;
  public acmeOrderView: IACMEOrderView = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.orderId) {
        vm.fillAcmeOrderData(to.params.orderId);
      }
    });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public mounted(): void {
    window.console.info('in mounted()) ');

    window.console.info('++++++++++++++++++ route.query : ' + this.$route.query.orderId);
    if (this.$route.query.orderId) {
      this.fillAcmeOrderData(this.$route.query.orderId);
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

  public fillAcmeOrderData(orderId): void {
    window.console.info('calling fillAcmeAccountData');
    const self = this;

    axios({
      method: 'get',
      url: 'api/acmeOrderViews/' + encodeURIComponent(orderId),
      responseType: 'stream'
    }).then(function(response) {
      self.acmeOrderView = response.data;
      window.console.info('acmeOrderView :' + self.acmeOrderView);
    });
  }
}
