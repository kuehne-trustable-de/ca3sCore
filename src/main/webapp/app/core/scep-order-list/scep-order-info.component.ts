import { Component, Inject, Vue } from 'vue-property-decorator';
import { Fragment } from 'vue-fragment';

import axios from 'axios';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';
import AlertService from '@/shared/alert/alert.service';
import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import HelpTag from '@/core/help/help-tag.vue';

import { IScepOrderView } from '@/shared/model/transfer-object.model';

@Component({
  components: {
    Fragment,
    CopyClipboardButton,
    HelpTag
  }
})
export default class AcmeOrderInfo extends mixins(JhiDataUtils, Vue) {
  @Inject('alertService') private alertService: () => AlertService;
  public scepOrderView: IScepOrderView = {};

  beforeRouteEnter(to, from, next) {
    window.console.info('++++++++++++++++++ beforeRouteEnter ');
    next(vm => {
      if (to.params.id) {
        vm.fillScepOrderData(to.params.id);
      }
    });
  }

  public previousState() {
    this.$router.go(-1);
  }

  public mounted(): void {
    window.console.info('in mounted()) ');

    window.console.info('++++++++++++++++++ route.query : ' + this.$route.query.id);
    if (this.$route.query.id) {
      this.fillScepOrderData(this.$route.query.id);
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

  public fillScepOrderData(orderId): void {
    window.console.info('calling fillScepOrderData');
    const self = this;

    axios({
      method: 'get',
      url: 'api/scepOrderViews/' + encodeURIComponent(orderId),
      responseType: 'stream'
    }).then(function(response) {
      self.scepOrderView = response.data;
      window.console.info('scepOrderView :' + self.scepOrderView);
    });
  }
}
