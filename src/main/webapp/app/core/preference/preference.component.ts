import { Component, Vue, Inject } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';
import axios from 'axios';

import { IPreferences } from '@/shared/model/transfer-object.model';

import AlertService from '@/shared/alert/alert.service';
import AlertMixin from '@/shared/alert/alert.mixin';

const baseApiUrl = '/api/admin/preference';

@Component
export default class Preference extends mixins(AlertMixin, Vue) {

  @Inject('alertService') alertService: () => AlertService;

  public preferences: IPreferences = {};

  public portArr: number[] = [5544];

  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.retrievePreference();
    });
  }

  public save(): void {
    this.isSaving = true;
    this.update(1, this.preferences)
      .then(param => {
        this.isSaving = false;
        const message = this.$t('ca3SApp.preference.updated', { param: 1 });
        this.alertService().showAlert(message, 'info');
        console.log(message);
      });
  }

  public retrievePreference(): void {
    this.find(1)
      .then(res => {
        this.preferences = res;
        const parts = this.preferences.acmeHTTP01CallbackPorts.split(',');
        this.portArr = [];
        for (let i = 0; i < parts.length; i++) {
          this.portArr[i] = Number(parts[i]);
        }
        this.portArr.push(0);
      });
  }

  public find(id: number): Promise<IPreferences> {
    return new Promise<IPreferences>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(function(res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public update(id: number, entity: IPreferences): Promise<IPreferences> {

    this.preferences.acmeHTTP01CallbackPorts = '';
    for (let i = 0; i < this.portArr.length - 1; i++) {
      if ( this.portArr[i] > 0) {
        if ( i > 0) {
          this.preferences.acmeHTTP01CallbackPorts += ',';
        }
        this.preferences.acmeHTTP01CallbackPorts += this.portArr[i];
      }
    }
    window.console.info('acmeHTTP01TimeoutMilliSec: ' + this.preferences.acmeHTTP01TimeoutMilliSec);
    window.console.info('acmeHTTP01CallbackPorts: ' + this.preferences.acmeHTTP01CallbackPorts);

    return new Promise<IPreferences>((resolve, reject) => {
      axios
        .put(`${baseApiUrl}/${id}`, entity)
        .then(function(res) {
          resolve(res.data);
        })
        .catch(err => {
          console.log('update err: ' + err);
          reject(err);
        });
    });
  }

  public previousState(): void {
    this.retrievePreference();
        const message = this.$t('ca3SApp.preference.changes.canceled');
        console.log(message );
        this.alertService().showAlert(message, 'info');
  }

  public alignPortArraySize(valueIndex: number): void {

    window.console.info('in alignPortArraySize(' + valueIndex + ')');
    const currentSize = this.portArr.length;

    const currentValue = this.portArr[valueIndex];

    if ( currentValue === 0 ) {
      if ( currentSize > 1 ) {
        // preserve last element
        this.portArr.splice(valueIndex, 1);
        window.console.info('in alignPortArraySize(' + valueIndex + '): dropped empty element');
      }
    } else {
      if ( valueIndex + 1 === currentSize ) {
        this.portArr.push(0);
        window.console.info('in alignPortArraySize(' + valueIndex + '): appended one element');
      }
    }

  }

}
