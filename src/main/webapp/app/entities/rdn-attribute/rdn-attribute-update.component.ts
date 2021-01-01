import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import RDNService from '../rdn/rdn.service';
import { IRDN } from '@/shared/model/rdn.model';

import AlertService from '@/shared/alert/alert.service';
import { IRDNAttribute, RDNAttribute } from '@/shared/model/rdn-attribute.model';
import RDNAttributeService from './rdn-attribute.service';

const validations: any = {
  rDNAttribute: {
    attributeType: {
      required
    },
    attributeValue: {
      required
    }
  }
};

@Component({
  validations
})
export default class RDNAttributeUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('rDNAttributeService') private rDNAttributeService: () => RDNAttributeService;
  public rDNAttribute: IRDNAttribute = new RDNAttribute();

  @Inject('rDNService') private rDNService: () => RDNService;

  public rDNS: IRDN[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.rDNAttributeId) {
        vm.retrieveRDNAttribute(to.params.rDNAttributeId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.rDNAttribute.id) {
      this.rDNAttributeService()
        .update(this.rDNAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.rDNAttribute.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.rDNAttributeService()
        .create(this.rDNAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.rDNAttribute.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveRDNAttribute(rDNAttributeId): void {
    this.rDNAttributeService()
      .find(rDNAttributeId)
      .then(res => {
        this.rDNAttribute = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.rDNService()
      .retrieve()
      .then(res => {
        this.rDNS = res.data;
      });
  }
}
