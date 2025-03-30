import Vue from 'vue';
import { Component, Inject } from 'vue-property-decorator';
import { email, helpers, maxLength, minLength, required, sameAs } from 'vuelidate/lib/validators';
import LoginService from '@/account/login.service';
import RegisterService from '@/account/register/register.service';
import { EMAIL_ALREADY_USED_TYPE, LOGIN_ALREADY_USED_TYPE } from '@/constants';

const loginPattern = helpers.regex('alpha', /^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$/);
const validations: any = {
  registerAccount: {
    login: {
      required,
      minLength: minLength(1),
      maxLength: maxLength(50),
      pattern: loginPattern,
    },
    email: {
      required,
      minLength: minLength(5),
      maxLength: maxLength(254),
      //      email,
    },
    phone: {
      minLength: minLength(0),
      maxLength: maxLength(254),
    },
    password: {
      required,
      minLength: minLength(4),
      maxLength: maxLength(254),
    },
  },
  confirmPassword: {
    required,
    minLength: minLength(4),
    maxLength: maxLength(50),
    // prettier-ignore
    sameAsPassword: sameAs(function() {
      return this.registerAccount.password;
    }),
  },
};

@Component({
  validations,
})
export default class Register extends Vue {
  @Inject('registerService') private registerService: () => RegisterService;
  @Inject('loginService') private loginService: () => LoginService;
  public registerAccount: any = {
    login: undefined,
    email: undefined,
    password: undefined,
  };
  public confirmPassword: any = null;
  public error = '';
  public errorEmailExists = '';
  public errorUserExists = '';
  public success = false;

  public register(): void {
    this.error = null;
    this.errorUserExists = null;
    this.errorEmailExists = null;
    this.registerAccount.langKey = this.$store.getters.currentLanguage;
    this.registerService()
      .processRegistration(this.registerAccount)
      .then(() => {
        this.success = true;
      })
      .catch(error => {
        this.success = null;
        console.log('error.response.data.type : ' + error.response.data.type);
        console.log('LOGIN_ALREADY_USED_TYPE : ' + LOGIN_ALREADY_USED_TYPE);
        console.log('EMAIL_ALREADY_USED_TYPE : ' + EMAIL_ALREADY_USED_TYPE);
        if (error.response.status === 400 && error.response.data.type === LOGIN_ALREADY_USED_TYPE) {
          this.errorUserExists = 'ERROR';
        } else if (error.response.status === 400 && error.response.data.type === EMAIL_ALREADY_USED_TYPE) {
          this.errorEmailExists = 'ERROR';
        } else {
          this.error = 'ERROR';
        }
      });
  }

  public openLogin(): void {
    this.loginService().openLogin((<any>this).$root);
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
      this.$store.state.uiConfigStore.config.cryptoConfigView.passwordRegexp !== undefined
    ) {
      return this.$store.state.uiConfigStore.config.cryptoConfigView.passwordRegexp;
    }
    return '';
  }

  public regExpSecretDescription(): string {
    if (
      this.$store.state.uiConfigStore.config.cryptoConfigView !== undefined &&
      this.$store.state.uiConfigStore.config.cryptoConfigView.regexpPasswordDescription !== undefined
    ) {
      console.log('regExpSecretDescription : ' + this.$store.state.uiConfigStore.config.cryptoConfigView.regexpPasswordDescription);
      return this.$store.state.uiConfigStore.config.cryptoConfigView.regexpPasswordDescription;
    }
    return '';
  }
}
