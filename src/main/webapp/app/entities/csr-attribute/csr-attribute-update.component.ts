import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import CSRService from '../csr/csr.service';
import { ICSR } from '@/shared/model/csr.model';

import AlertService from '@/shared/alert/alert.service';
import { ICsrAttribute, CsrAttribute } from '@/shared/model/csr-attribute.model';
import CsrAttributeService from './csr-attribute.service';

const validations: any = {
  csrAttribute: {
    name: {
      required,
    },
    value: {},
  },
};

@Component({
  validations,
})
export default class CsrAttributeUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('csrAttributeService') private csrAttributeService: () => CsrAttributeService;
  public csrAttribute: ICsrAttribute = new CsrAttribute();

  @Inject('cSRService') private cSRService: () => CSRService;

  public cSRS: ICSR[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.csrAttributeId) {
        vm.retrieveCsrAttribute(to.params.csrAttributeId);
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
    if (this.csrAttribute.id) {
      this.csrAttributeService()
        .update(this.csrAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.csrAttribute.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.csrAttributeService()
        .create(this.csrAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.csrAttribute.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveCsrAttribute(csrAttributeId): void {
    this.csrAttributeService()
      .find(csrAttributeId)
      .then(res => {
        this.csrAttribute = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.cSRService()
      .retrieve()
      .then(res => {
        this.cSRS = res.data;
      });
  }
}
