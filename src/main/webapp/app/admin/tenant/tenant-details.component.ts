import { Component, Vue, Inject } from 'vue-property-decorator';

import { ITenant } from '@/shared/model/tenant.model';
import TenantService from './tenant.service';
import AlertService from '@/shared/alert/alert.service';

@Component
export default class TenantDetails extends Vue {
  @Inject('tenantService') private tenantService: () => TenantService;
  @Inject('alertService') private alertService: () => AlertService;

  public tenant: ITenant = {};

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.tenantId) {
        vm.retrieveTenant(to.params.tenantId);
      }
    });
  }

  public retrieveTenant(tenantId) {
    this.tenantService()
      .find(tenantId)
      .then(res => {
        this.tenant = res;
      })
      .catch(error => {
        this.alertService().showHttpError(this, error.response);
      });
  }

  public previousState() {
    this.$router.push('/admin/tenant');
  }
}
