import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import RDNAttributeService from '../rdn-attribute/rdn-attribute.service';
import { IRDNAttribute } from '@/shared/model/rdn-attribute.model';

import CSRService from '../csr/csr.service';
import { ICSR } from '@/shared/model/csr.model';

import AlertService from '@/shared/alert/alert.service';
import { IRDN, RDN } from '@/shared/model/rdn.model';
import RDNService from './rdn.service';

const validations: any = {
  rDN: {}
};

@Component({
  validations
})
export default class RDNUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('rDNService') private rDNService: () => RDNService;
  public rDN: IRDN = new RDN();

  @Inject('rDNAttributeService') private rDNAttributeService: () => RDNAttributeService;

  public rDNAttributes: IRDNAttribute[] = [];

  @Inject('cSRService') private cSRService: () => CSRService;

  public cSRS: ICSR[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.rDNId) {
        vm.retrieveRDN(to.params.rDNId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.rDN.id) {
      this.rDNService()
        .update(this.rDN)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.rDN.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.rDNService()
        .create(this.rDN)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.rDN.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveRDN(rDNId): void {
    this.rDNService()
      .find(rDNId)
      .then(res => {
        this.rDN = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.rDNAttributeService()
      .retrieve()
      .then(res => {
        this.rDNAttributes = res.data;
      });
    this.cSRService()
      .retrieve()
      .then(res => {
        this.cSRS = res.data;
      });
  }
}
