import { email, maxLength, minLength, required } from 'vuelidate/lib/validators';
import axios, { AxiosError } from 'axios';
import { EMAIL_ALREADY_USED_TYPE } from '@/constants';
import { Vue, Component, Inject } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';
import JhiDataUtils from '@/shared/data/data-utils.service';

const validations = {
  settingsAccount: {
    firstName: {
      required,
      minLength: minLength(1),
      maxLength: maxLength(50),
    },
    lastName: {
      required,
      minLength: minLength(1),
      maxLength: maxLength(50),
    },
    email: {
      required,
      email,
      minLength: minLength(5),
      maxLength: maxLength(254),
    },
    phone: {
      minLength: minLength(0),
      maxLength: maxLength(254),
    },
  },
};

@Component({
  validations,
})
export default class Settings extends mixins(AlertMixin, JhiDataUtils) {
  public success: string = null;
  public error: string = null;
  public errorEmailExists: string = null;
  public languages: any = this.$store.getters.languages || [];
  public secret: string = null;

  public save(): void {
    this.error = null;
    this.errorEmailExists = null;
    axios
      .post('api/account', this.settingsAccount)
      .then(() => {
        this.error = null;
        this.success = 'OK';
        this.errorEmailExists = null;
      })
      .catch(error => {
        this.success = null;
        this.error = 'ERROR';
        if (error.response.status === 400 && error.response.data.type === EMAIL_ALREADY_USED_TYPE) {
          this.errorEmailExists = 'ERROR';
          this.error = null;
        }
      });
  }

  public get settingsAccount(): any {
    return this.$store.getters.account;
  }

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public showRequiredWarning(isRequired: boolean, value: string): boolean {
    console.log('showRequiredWarning( ' + isRequired + ', "' + value + '"');
    if (isRequired) {
      if (!value) {
        return true;
      }
      if (value.trim().length === 0) {
        return true;
      }
    }
    return false;
  }

  public showRegExpFieldWarningNonEmpty(value: string, regEx: string): boolean {
    if (!value) {
      return true;
    }
    if (value.trim().length > 0) {
      return this.showRegExpFieldWarning(value, regEx);
    }
    return false;
  }

  public showRegExpFieldWarning(value: string, regEx: string): boolean {
    const regexp = new RegExp(regEx);
    const valid = regexp.test(value);
    console.log('showRegExpFieldWarning( ' + regEx + ', "' + value + '") -> ' + valid);
    return !valid;
  }

  public regExpSecret(): string {
    if (
      this.$store.state.uiConfigStore.config.cryptoConfigView !== undefined &&
      this.$store.state.uiConfigStore.config.cryptoConfigView.pkcs12SecretRegexp !== undefined
    ) {
      return this.$store.state.uiConfigStore.config.cryptoConfigView.pkcs12SecretRegexp;
    }
    return '';
  }

  public regExpSecretDescription(): string {
    if (
      this.$store.state.uiConfigStore.config.cryptoConfigView !== undefined &&
      this.$store.state.uiConfigStore.config.cryptoConfigView.regexpPkcs12SecretDescription !== undefined
    ) {
      console.log('regExpSecretDescription : ' + this.$store.state.uiConfigStore.config.cryptoConfigView.regexpPkcs12SecretDescription);
      return this.$store.state.uiConfigStore.config.cryptoConfigView.regexpPkcs12SecretDescription;
    }
    return '';
  }
}
