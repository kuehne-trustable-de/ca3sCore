import { Component, Inject, Vue } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import UserManagementService from './user-management.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class JhiUserManagementComponent extends Vue {
  @Inject('userService') private userManagementService: () => UserManagementService;
  public error = '';
  public success = '';
  public users: any[] = [];
  public itemsPerPage = 20;
  public queryCount: number = null;
  public page = 1;
  public previousPage = 1;
  public propOrder = 'id';
  public reverse = false;
  public totalItems = 0;
  public isLoading = false;
  public removeId: number = null;

  public mounted(): void {
    this.loadAll();
  }

  public setActive(user, isActivated): void {
    user.activated = isActivated;
    this.userManagementService()
      .update(user)
      .then(() => {
        this.error = null;
        this.success = 'OK';
        this.loadAll();
      })
      .catch(() => {
        this.success = null;
        this.error = 'ERROR';
        user.activated = false;
      });
  }
  public setUnblocked(user): void {
    user.blockedUntilDate = null;
    user.failedLogins = 0;
    this.userManagementService()
      .update(user)
      .then(() => {
        this.error = null;
        this.success = 'OK';
        this.loadAll();
      })
      .catch(() => {
        this.success = null;
        this.error = 'ERROR';
        user.activated = false;
      });
  }

  public loadAll(): void {
    this.isLoading = true;

    this.userManagementService()
      .retrieve({
        page: this.page - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
      })
      .then(res => {
        this.isLoading = false;
        this.users = res.data;
        this.totalItems = Number(res.headers['x-total-count']);
        this.queryCount = this.totalItems;
      })
      .catch(() => {
        this.isLoading = false;
      });
  }

  public handleSyncList(): void {
    this.loadAll();
  }

  public sort(): any {
    const result = [this.propOrder + ',' + (this.reverse ? 'desc' : 'asc')];
    if (this.propOrder !== 'id') {
      result.push('id');
    }
    return result;
  }

  public loadPage(page: number): void {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.transition();
    }
  }

  public transition(): void {
    this.loadAll();
  }

  public changeOrder(propOrder: string): void {
    this.propOrder = propOrder;
    this.reverse = !this.reverse;
    this.transition();
  }

  public deleteUser(): void {
    const self = this;

    this.userManagementService()
      .remove(this.removeId)
      .then(res => {
        const message = self.$t('userManagement.deleted', { param: self.removeId });
        self.alertService().showAlert(message, 'info');
        self.removeId = null;
        self.loadAll();
        self.closeDialog();
      });
  }

  public prepareRemove(instance): void {
    this.removeId = instance.login;
    if (<any>this.$refs.removeUser) {
      (<any>this.$refs.removeUser).show();
    }
  }

  public closeDialog(): void {
    if (<any>this.$refs.removeUser) {
      (<any>this.$refs.removeUser).hide();
    }
  }

  public get username(): string {
    return this.$store.getters.account?.login ?? '';
  }
}
