import { email, maxLength, minLength, required } from 'vuelidate/lib/validators';
import { Component, Inject, Vue } from 'vue-property-decorator';
import UserManagementService from './user-management.service';
import AlertService from '@/shared/alert/alert.service';
import { IUser, User } from '@/shared/model/user.model';
import { ITenant } from '../../shared/model/tenant.model';
import TenantService from '../tenant/tenant.service';
import { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';

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
    phone: {
      minLength: minLength(0),
      maxLength: maxLength(254),
    },
    tenantId: {},
  },
};

@Component({
  validations,
})
export default class JhiUserManagementEdit extends mixins(AlertMixin) {
  @Inject('alertService') private alertService: () => AlertService;
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
      }else {
        vm.userAccount = { "authorities": ["ROLE_USER"]};
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
          this.alertService().showAlert(err.response, 'warn');
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
    this.$router.go(-1);
  }

  public save(): void {
    this.isSaving = true;
    const self = this;
    if (this.userAccount.id) {
      this.userManagementService()
        .update(this.userAccount)
        .then(res => {
          self.returnToList();
          self.alertService().showAlert(self.$t('userManagement.updated', { param: self.userAccount.id }), 'info');
        })
        .catch(function (error) {
          console.log(error);
          const message = self.$t('problem processing request: ' + error);
          self.alertService().showAlert(message, 'info');
        });
    } else {
      this.userManagementService()
        .create(this.userAccount)
        .then(res => {
          self.returnToList();
          self.alertService().showAlert(self.$t('userManagement.created', { param: res.id }), 'info');
        })
        .catch(function (error) {
          console.log(error);
          const message = self.$t('problem processing request: ' + error);
          self.alertService().showAlert(message, 'info');
        });
    }
  }

  private returnToList(): void {
    this.isSaving = false;
    this.$router.push('/admin/user-list');
  }
}
