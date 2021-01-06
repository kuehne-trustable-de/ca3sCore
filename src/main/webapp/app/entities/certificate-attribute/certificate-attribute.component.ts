import { mixins } from 'vue-class-component';

import { Component, Vue, Inject } from 'vue-property-decorator';
import Vue2Filters from 'vue2-filters';
import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';
import AlertMixin from '@/shared/alert/alert.mixin';

import CertificateAttributeService from './certificate-attribute.service';

@Component({
  mixins: [Vue2Filters.mixin],
})
export default class CertificateAttribute extends mixins(AlertMixin) {
  @Inject('certificateAttributeService') private certificateAttributeService: () => CertificateAttributeService;
  private removeId: number = null;

  public certificateAttributes: ICertificateAttribute[] = [];

  public isFetching = false;

  public mounted(): void {
    this.retrieveAllCertificateAttributes();
  }

  public clear(): void {
    this.retrieveAllCertificateAttributes();
  }

  public retrieveAllCertificateAttributes(): void {
    this.isFetching = true;

    this.certificateAttributeService()
      .retrieve()
      .then(
        res => {
          this.certificateAttributes = res.data;
          this.isFetching = false;
        },
        err => {
          this.isFetching = false;
        }
      );
  }

  public prepareRemove(instance: ICertificateAttribute): void {
    this.removeId = instance.id;
    if (<any>this.$refs.removeEntity) {
      (<any>this.$refs.removeEntity).show();
    }
  }

  public removeCertificateAttribute(): void {
    this.certificateAttributeService()
      .delete(this.removeId)
      .then(() => {
        const message = this.$t('ca3SApp.certificateAttribute.deleted', { param: this.removeId });
        this.alertService().showAlert(message, 'danger');
        this.getAlertFromStore();
        this.removeId = null;
        this.retrieveAllCertificateAttributes();
        this.closeDialog();
      });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
