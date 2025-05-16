import { Component, Inject } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import axios from 'axios';

import { IPreferences } from '@/shared/model/transfer-object.model';

import AlertService from '@/shared/alert/alert.service';
import AlertMixin from '@/shared/alert/alert.mixin';
import HelpTag from '@/core/help/help-tag.vue';

import { integer, minValue, maxValue, required } from 'vuelidate/lib/validators';

const baseApiUrl = '/api/preference';

const validations: any = {
  preferences: {
    deleteKeyAfterDays: {
      required,
      integer,
      minValue: minValue(1),
      maxValue: maxValue(65535),
    },
    deleteKeyAfterUses: {
      required,
      integer,
      minValue: minValue(1),
      maxValue: maxValue(65535),
    },
    acmeHTTP01TimeoutMilliSec: {
      required,
      integer,
      minValue: minValue(1),
      maxValue: maxValue(65535),
    },
    acmeHTTP01CallbackPortArr: {
      $each: {
        required,
        integer,
        minValue: minValue(0),
        maxValue: maxValue(65535),
      },
    },
  },
};

@Component({
  validations,
  components: {
    HelpTag,
  },
})
export default class Preference extends mixins(AlertMixin, JhiDataUtils) {
  @Inject('alertService') alertService: () => AlertService;

  public preferences: IPreferences = {};

  public portArr: number[] = [5544];

  public selectedHashes: any[] = [];
  public selectedSigningAlgos: any[] = [];

  public allHashes = [
    { id: 'sha-1', name: 'SHA-1' },
    { id: 'sha-256', name: 'SHA-256' },
    { id: 'sha-512', name: 'SHA-512' },
  ];

  public allSignAlgos = [
    { id: 'rsa-1024', name: 'RSA-1024' },
    { id: 'rsa-2048', name: 'RSA-2048' },
    { id: 'rsa-3072', name: 'RSA-3072' },
    { id: 'rsa-4096', name: 'RSA-4096' },
    { id: 'rsa-8192', name: 'RSA-8192' },
    { id: 'dilithium2-20224', name: 'Dilithium2' },
  ];

  public isSaving = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.retrievePreference();
    });
  }

  created(): void {
    const config = this.$store.getters.config;
    if (config.cryptoConfigView.allHashAlgoArr !== undefined) {
      this.allHashes = [];
      for (const algo of config.cryptoConfigView.allHashAlgoArr) {
        this.allHashes.push({ id: algo, name: algo });
      }
    }

    if (config.cryptoConfigView.allSignAlgoArr !== undefined) {
      this.allSignAlgos = [];
      for (const algo of config.cryptoConfigView.allSignAlgoArr) {
        this.allSignAlgos.push({ id: algo, name: algo });
      }
    }
  }

  public save(): void {
    this.isSaving = true;
    this.update(1, this.preferences).then(param => {
      this.isSaving = false;
      const message = this.$t('ca3SApp.preference.updated', { param: 1 });
      this.alertService().showAlert(message, 'info');

      self.$store.state.uiConfigStore.config.infoMsg = self.preferences.infoMsg;
      self.$store.commit('updateCV', self.$store.state.uiConfigStore);

      console.log(message);
    });
  }

  public retrievePreference(): void {
    this.find(1).then(res => {
      this.preferences = res;

      this.allHashes = [];
      for (let i = 0; i < this.preferences.availableHashes.length; i++) {
        const hash = this.preferences.availableHashes[i];
        this.allHashes.push({ id: hash, name: hash });
      }

      this.allSignAlgos = [];
      for (let i = 0; i < this.preferences.availableSigningAlgos.length; i++) {
        const algo = this.preferences.availableSigningAlgos[i];
        this.allSignAlgos.push({ id: algo, name: algo });
      }

      /*
        const parts = this.preferences.acmeHTTP01CallbackPorts.split(',');
        this.portArr = [];
        for (let i = 0; i < parts.length; i++) {
          this.portArr[i] = Number(parts[i]);
        }
        this.portArr.push(0);
 */
    });
  }

  public find(id: number): Promise<IPreferences> {
    return new Promise<IPreferences>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(function (res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public update(id: number, entity: IPreferences): Promise<IPreferences> {
    /*
    this.preferences.acmeHTTP01CallbackPorts = '';
    for (let i = 0; i < this.portArr.length - 1; i++) {
      if ( this.portArr[i] > 0) {
        if ( i > 0) {
          this.preferences.acmeHTTP01CallbackPorts += ',';
        }
        this.preferences.acmeHTTP01CallbackPorts += this.portArr[i];
      }
    }
*/
    window.console.info('acmeHTTP01TimeoutMilliSec: ' + this.preferences.acmeHTTP01TimeoutMilliSec);
    window.console.info('acmeHTTP01CallbackPortArr: ' + this.preferences.acmeHTTP01CallbackPortArr);

    return new Promise<IPreferences>((resolve, reject) => {
      axios
        .put(`${baseApiUrl}/${id}`, entity)
        .then(function (res) {
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
    console.log(message);
    this.alertService().showAlert(message, 'info');
  }

  public alignCallbackPortArraySize(index: number): void {
    const valueIndex = Number(index);
    const currentSize = this.preferences.acmeHTTP01CallbackPortArr.length;
    const currentValue = this.preferences.acmeHTTP01CallbackPortArr[valueIndex];
    window.console.info(
      'in alignCallbackPortArraySize(' +
        valueIndex +
        '), size: ' +
        currentSize +
        ' has value "' +
        currentValue +
        '", isNaN: ' +
        isNaN(currentValue)
    );

    if (currentValue.toString().length === 0) {
      if (currentSize > 1 && valueIndex < currentSize - 1) {
        // preserve last element
        this.preferences.acmeHTTP01CallbackPortArr.splice(valueIndex, 1);
        window.console.info('in alignCallbackPortArraySize(' + valueIndex + '): dropped empty element');
      }
    } else {
      window.console.info('value of ' + valueIndex + ' not empty, valueIndex + 1 === currentSize : ' + (valueIndex + 1 === currentSize));
      if (valueIndex + 1 === currentSize) {
        this.preferences.acmeHTTP01CallbackPortArr.push(0);
        window.console.info('in alignCallbackPortArraySize(' + valueIndex + '): appended one element');
      }
    }
  }
}
