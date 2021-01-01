import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import RequestAttributeService from '../request-attribute/request-attribute.service';
import { IRequestAttribute } from '@/shared/model/request-attribute.model';

import AlertService from '@/shared/alert/alert.service';
import { IRequestAttributeValue, RequestAttributeValue } from '@/shared/model/request-attribute-value.model';
import RequestAttributeValueService from './request-attribute-value.service';

const validations: any = {
  requestAttributeValue: {
    attributeValue: {
      required
    }
  }
};

@Component({
  validations
})
export default class RequestAttributeValueUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('requestAttributeValueService') private requestAttributeValueService: () => RequestAttributeValueService;
  public requestAttributeValue: IRequestAttributeValue = new RequestAttributeValue();

  @Inject('requestAttributeService') private requestAttributeService: () => RequestAttributeService;

  public requestAttributes: IRequestAttribute[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.requestAttributeValueId) {
        vm.retrieveRequestAttributeValue(to.params.requestAttributeValueId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.requestAttributeValue.id) {
      this.requestAttributeValueService()
        .update(this.requestAttributeValue)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.requestAttributeValue.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.requestAttributeValueService()
        .create(this.requestAttributeValue)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.requestAttributeValue.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveRequestAttributeValue(requestAttributeValueId): void {
    this.requestAttributeValueService()
      .find(requestAttributeValueId)
      .then(res => {
        this.requestAttributeValue = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.requestAttributeService()
      .retrieve()
      .then(res => {
        this.requestAttributes = res.data;
      });
  }
}
