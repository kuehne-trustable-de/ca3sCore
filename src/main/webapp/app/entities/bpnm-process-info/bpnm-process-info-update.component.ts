import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';
import format from 'date-fns/format';
import parse from 'date-fns/parse';
import parseISO from 'date-fns/parseISO';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import AlertService from '@/shared/alert/alert.service';
import { IBPMNProcessInfo, BPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';
import BPNMProcessInfoService from './bpnm-process-info.service';

const validations: any = {
  bPNMProcessInfo: {
    name: {
      required
    },
    version: {
      required
    },
    type: {
      required
    },
    author: {
      required
    },
    lastChange: {
      required
    },
    signatureBase64: {
      required
    }
  }
};

@Component({
  validations
})
export default class BPNMProcessInfoUpdate extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;
  public bPNMProcessInfo: IBPMNProcessInfo = new BPMNProcessInfo();
  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.bPNMProcessInfoId) {
        vm.retrieveBPNMProcessInfo(to.params.bPNMProcessInfoId);
      }
    });
  }

  public save(): void {
    this.isSaving = true;
    if (this.bPNMProcessInfo.id) {
      this.bPNMProcessInfoService()
        .update(this.bPNMProcessInfo)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.bPNMProcessInfo.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.bPNMProcessInfoService()
        .create(this.bPNMProcessInfo)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.bPNMProcessInfo.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public convertDateTimeFromServer(date: Date): string {
    if (date) {
      return format(date, DATE_TIME_LONG_FORMAT);
    }
    return null;
  }

  public updateInstantField(field, event) {
    if (event.target.value) {
      this.bPNMProcessInfo[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.bPNMProcessInfo[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.bPNMProcessInfo[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.bPNMProcessInfo[field] = null;
    }
  }

  public retrieveBPNMProcessInfo(bPNMProcessInfoId): void {
    this.bPNMProcessInfoService()
      .find(bPNMProcessInfoId)
      .then(res => {
        res.lastChange = new Date(res.lastChange);
        this.bPNMProcessInfo = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
