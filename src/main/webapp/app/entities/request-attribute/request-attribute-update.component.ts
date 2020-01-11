import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import RequestAttributeValueService from '../request-attribute-value/request-attribute-value.service';
import { IRequestAttributeValue } from '@/shared/model/request-attribute-value.model';

import CSRService from '../csr/csr.service';
import { ICSR } from '@/shared/model/csr.model';

import AlertService from '@/shared/alert/alert.service';
import { IRequestAttribute, RequestAttribute } from '@/shared/model/request-attribute.model';
import RequestAttributeService from './request-attribute.service';

const validations: any = {
  requestAttribute: {
    attributeType: {
      required
    }
  }
};

@Component({
  validations
})
export default class RequestAttributeUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('requestAttributeService') private requestAttributeService: () => RequestAttributeService;
  public requestAttribute: IRequestAttribute = new RequestAttribute();

  @Inject('requestAttributeValueService') private requestAttributeValueService: () => RequestAttributeValueService;

  public requestAttributeValues: IRequestAttributeValue[] = [];

  @Inject('cSRService') private cSRService: () => CSRService;

  public cSRS: ICSR[] = [];
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.requestAttributeId) {
        vm.retrieveRequestAttribute(to.params.requestAttributeId);
      }
      vm.initRelationships();
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.requestAttribute.id) {
      this.requestAttributeService()
        .update(this.requestAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.requestAttribute.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.requestAttributeService()
        .create(this.requestAttribute)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.requestAttribute.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveRequestAttribute(requestAttributeId): void {
    this.requestAttributeService()
      .find(requestAttributeId)
      .then(res => {
        this.requestAttribute = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.requestAttributeValueService()
      .retrieve()
      .then(res => {
        this.requestAttributeValues = res.data;
      });
    this.requestAttributeValueService()
      .retrieve()
      .then(res => {
        this.requestAttributeValues = res.data;
      });
    this.cSRService()
      .retrieve()
      .then(res => {
        this.cSRS = res.data;
      });
  }
}
