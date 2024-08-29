import { email, maxLength, minLength, required } from 'vuelidate/lib/validators';
import { Component, Inject, Vue } from 'vue-property-decorator';
import UserManagementService from './user-management.service';
import { IUser, User } from '@/shared/model/user.model';
import { ITenant } from '../../shared/model/tenant.model';
import TenantService from '../../user-management/tenant/tenant.service';

const loginValidator = (value: string) => {
  if (!value) {
    return true;
  }
  return /^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$/.test(value);
};

const validations: any = {
  userAccount: {
    login: {
      required,
      maxLength: maxLength(254),
      pattern: loginValidator,
    },
    firstName: {
      maxLength: maxLength(50),
    },
    lastName: {
      maxLength: maxLength(50),
    },
    email: {
      required,
      email,
      minLength: minLength(5),
      maxLength: maxLength(50),
    },
    tenantId: {},
  },
};

@Component({
  validations,
})
export default class JhiUserManagementEdit extends Vue {
  @Inject('userService') private userManagementService: () => UserManagementService;
  @Inject('tenantService') private tenantService: () => TenantService;

  public tenants: ITenant[] = [];
  public userAccount: IUser;
  public isSaving = false;
  public authorities: any[] = [];
  public scndFactors: string[] = [];
  public languages: any = this.$store.getters.languages;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.initAuthorities();
      if (to.params.userId) {
        vm.init(to.params.userId);
      }
    });
  }

  public mounted(): void {
    this.retrieveAllTenants();
    this.scndFactors = this.$store.state.uiConfigStore.config.scndFactorTypes;
  }

  public constructor() {
    super();
    this.userAccount = new User();
    this.userAccount.authorities = [];
  }

  public initAuthorities() {
    this.userManagementService()
      .retrieveAuthorities()
      .then(_res => {
        this.authorities = _res.data;
      });
  }

  public retrieveAllTenants(): void {
    this.tenantService()
      .retrieve()
      .then(
        res => {
          this.tenants = res.data;
        },
        err => {
          self.alertService().showAlert(err.response, 'warn');
        }
      );
  }

  public init(userId: number): void {
    this.userManagementService()
      .get(userId)
      .then(res => {
        this.userAccount = res.data;
      });
  }

  public previousState(): void {
    (<any>this).$router.go(-1);
  }

  public save(): void {
    this.isSaving = true;
    if (this.userAccount.id) {
      this.userManagementService()
        .update(this.userAccount)
        .then(res => {
          this.returnToList();
          this.$root.$bvToast.toast(this.getMessageFromHeader(res).toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Info',
            variant: 'info',
            solid: true,
            autoHideDelay: 5000,
          });
        });
    } else {
      this.userManagementService()
        .create(this.userAccount)
        .then(res => {
          this.returnToList();
          this.$root.$bvToast.toast(this.getMessageFromHeader(res).toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Success',
            variant: 'success',
            solid: true,
            autoHideDelay: 5000,
          });
        });
    }
  }

  private returnToList(): void {
    this.isSaving = false;
    (<any>this).$router.go(-1);
  }

  private getMessageFromHeader(res: any): any {
    return this.$t(res.headers['x-ca3sapp-alert'], { param: decodeURIComponent(res.headers['x-ca3sapp-params'].replace(/\+/g, ' ')) });
  }
}
