import { Component, Inject } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import axios from 'axios';

import { IPreferences } from '@/shared/model/transfer-object.model';

import AlertService from '@/shared/alert/alert.service';
import AlertMixin from '@/shared/alert/alert.mixin';

import { integer, minValue, maxValue, required } from 'vuelidate/lib/validators';

@Component
export default class Help extends mixins(AlertMixin) {
  @Inject('alertService') alertService: () => AlertService;

  public helpId = 'start';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.id) {
        vm.helpId = to.params.id;
      }
    });
  }
}
