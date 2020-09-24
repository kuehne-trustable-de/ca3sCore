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
      });
  }

  public retrievePreference(): void {
    this.find(1)
      .then(res => {
        this.preferences = res;
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
    return new Promise<IPreferences>((resolve, reject) => {
      axios
        .put(`${baseApiUrl}/${id}`, entity)
        .then(function(res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public previousState(): void {
    this.retrievePreference();
        const message = this.$t('ca3SApp.preference.changes.canceled');
        this.alertService().showAlert(message, 'info');
  }

  public initRelationships(): void {}
}
