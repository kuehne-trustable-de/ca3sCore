import { Component, Vue, Inject } from 'vue-property-decorator';

import { required } from 'vuelidate/lib/validators';

import AlertService from '@/shared/alert/alert.service';

import { IPipeline } from '@/shared/model/pipeline.model';

import { ITenant, Tenant } from '@/shared/model/tenant.model';
import TenantService from './tenant.service';

const validations: any = {
  tenant: {
    name: {
      required,
    },
    longname: {
      required,
    },
    active: {},
  },
};

@Component({
  validations,
})
export default class TenantUpdate extends Vue {
  @Inject('tenantService') private tenantService: () => TenantService;
  @Inject('alertService') private alertService: () => AlertService;

  public tenant: ITenant = new Tenant();

  public pipelines: IPipeline[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.tenantId) {
        vm.retrieveTenant(to.params.tenantId);
      }
      vm.initRelationships();
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
    if (this.tenant.id) {
      this.tenantService()
        .update(this.tenant)
        .then(param => {
          this.isSaving = false;
          this.$router.push('/admin/tenant');
          const message = this.$t('ca3SApp.tenant.updated', { param: param.id });
          return (this.$root as any).$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Info',
            variant: 'info',
            solid: true,
            autoHideDelay: 5000,
          });
        })
        .catch(error => {
          this.isSaving = false;
          this.alertService().showAlert(error.response);
        });
    } else {
      this.tenantService()
        .create(this.tenant)
        .then(param => {
          this.isSaving = false;
          this.$router.push('/admin/tenant');
          const message = this.$t('ca3SApp.tenant.created', { param: param.id });
          (this.$root as any).$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Success',
            variant: 'success',
            solid: true,
            autoHideDelay: 5000,
          });
        })
        .catch(error => {
          this.isSaving = false;
          this.alertService().showAlert(error.response);
        });
    }
  }

  public retrieveTenant(tenantId): void {
    this.tenantService()
      .find(tenantId)
      .then(res => {
        this.tenant = res;
      })
      .catch(error => {
        this.alertService().showAlert(error.response);
      });
  }

  public previousState(): void {
    this.$router.push('/admin/tenant');
  }

  public initRelationships(): void {
    /*
    this.pipelineService()
      .retrieve()
      .then(res => {
        this.pipelines = res.data;
      });

     */
  }
}
