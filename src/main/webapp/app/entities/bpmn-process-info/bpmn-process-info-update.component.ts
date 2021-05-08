import { Component, Inject } from 'vue-property-decorator';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import { required } from 'vuelidate/lib/validators';
// import dayjs from 'dayjs';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import { IBPMNProcessInfo, BPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';
import BPMNProcessInfoService from './bpmn-process-info.service';
import format from 'date-fns/format';
import parse from 'date-fns/parse';

const validations: any = {
  bPMNProcessInfo: {
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
    },
    bpmnHashBase64: {
      required
    },
    bpmnContent: {
      required
    }
  }
};

@Component({
  validations
})
export default class BPMNProcessInfoUpdate extends mixins(JhiDataUtils) {
  @Inject('bPMNProcessInfoService') private bPMNProcessInfoService: () => BPMNProcessInfoService;
  public bPMNProcessInfo: IBPMNProcessInfo = new BPMNProcessInfo();
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.bPMNProcessInfoId) {
        vm.retrieveBPMNProcessInfo(to.params.bPMNProcessInfoId);
      }
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
    if (this.bPMNProcessInfo.id) {
      this.bPMNProcessInfoService()
        .update(this.bPMNProcessInfo)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('tmpGenApp.bPMNProcessInfo.updated', { param: param.id });
          return this.$root.$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Info',
            variant: 'info',
            solid: true,
            autoHideDelay: 5000
          });
        });
    } else {
      this.bPMNProcessInfoService()
        .create(this.bPMNProcessInfo)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('tmpGenApp.bPMNProcessInfo.created', { param: param.id });
          this.$root.$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Success',
            variant: 'success',
            solid: true,
            autoHideDelay: 5000
          });
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
      this.bPMNProcessInfo[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.bPMNProcessInfo[field] = null;
    }
  }

  public updateZonedDateTimeField(field, event) {
    if (event.target.value) {
      this.bPMNProcessInfo[field] = parse(event.target.value, DATE_TIME_LONG_FORMAT, new Date());
    } else {
      this.bPMNProcessInfo[field] = null;
    }
  }

  public retrieveBPMNProcessInfo(bPMNProcessInfoId): void {
    this.bPMNProcessInfoService()
      .find(bPMNProcessInfoId)
      .then(res => {
        res.lastChange = new Date(res.lastChange);
        this.bPMNProcessInfo = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
