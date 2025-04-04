import { maxLength, minLength, required, sameAs } from 'vuelidate/lib/validators';
import axios, { AxiosError } from 'axios';
import { mapGetters } from 'vuex';
import Component, { mixins } from 'vue-class-component';
import AccountService from '@/account/account.service';
import AlertMixin from '@/shared/alert/alert.mixin';
import { IAccountCredentialView, IPasswordChangeDTO } from '@/shared/model/transfer-object.model';
import { Inject } from 'vue-property-decorator';

const validations = {
  resetPassword: {
    currentPassword: {
      required,
    },
    newPassword: {
      required,
      minLength: minLength(4),
      maxLength: maxLength(254),
    },
    confirmPassword: {
      // prettier-ignore
      sameAsPassword: sameAs(vm => {
      return vm.newPassword;
      }),
    },
  },
  clientAuthSecret: {
    minLength: minLength(4),
    maxLength: maxLength(254),
  },
};

@Component({
  validations,
  computed: {
    ...mapGetters(['account']),
  },
})
export default class ChangePassword extends mixins(AlertMixin) {
  @Inject('accountService') private accountService: () => AccountService;

  success: string = null;
  error: string = null;
  doNotMatch: string = null;
  updateCounter: number = 1;

  credentialChange: IPasswordChangeDTO = { credentialUpdateType: 'PASSWORD' };

  resetPassword: any = {
    currentPassword: null,
    newPassword: null,
    confirmPassword: null,
  };
  clientAuthSecret: string = null;

  qrCodeImgUrl: string = null;

  private removeInstance: IAccountCredentialView = {};

  public accountCredentialArr: IAccountCredentialView[] = [];

  public mounted(): void {
    this.refreshUIConfig();

    this.getCredentials();

    this.accountService().retrieveAccount();

    this.credentialChange.clientAuthCertId = 0;
  }

  public refreshUIConfig() {
    const self = this;
    axios({
      method: 'get',
      url: 'api/ui/config',
      responseType: 'stream',
    }).then(function (response) {
      window.console.info('ui/config returns ' + response.data);
      self.$store.commit('updateCV', response.data);
    });
  }

  public getCredentials(): void {
    window.console.info('calling getCredentials');
    const self = this;

    axios({
      method: 'get',
      url: 'api/account/credentials',
      responseType: 'stream',
    }).then(function (response) {
      window.console.info('getCredentials returns ' + response.data);
      self.accountCredentialArr = response.data;
    });
  }

  public changePassword(): void {
    if (this.resetPassword.newPassword !== this.resetPassword.confirmPassword) {
      this.error = null;
      this.success = null;
      this.doNotMatch = 'ERROR';
    } else {
      this.doNotMatch = null;
      this.credentialChange.currentPassword = this.resetPassword.currentPassword;
      this.credentialChange.newPassword = this.resetPassword.newPassword;
      axios
        .post('api/account/change-password', this.credentialChange)
        .then(() => {
          this.success = 'OK';
          this.credentialChange.clientAuthCertId = 0;
          this.error = null;
          this.resetPassword.currentPassword = '';
          this.resetPassword.newPassword = '';
          this.resetPassword.confirmPassword = '';
        })
        .catch(() => {
          this.success = null;
          this.error = 'ERROR';
          this.resetPassword.currentPassword = '';
          this.resetPassword.newPassword = '';
          this.resetPassword.confirmPassword = '';
        });
    }
  }

  public get username(): string {
    return this.$store.getters.account?.login ?? '';
  }

  public keystoreFilename(): string {
    return 'personalClientCertificate.p12';
  }

  public hasPhoneNumber(): boolean {
    return this.$store.getters.account.phone !== null;
  }

  public canCreateSecondFactor(secondFactorType: string): boolean {
    return this.$store.state.uiConfigStore.config.scndFactorTypes.includes(secondFactorType);
  }
  public showRequiredWarning(isRequired: boolean, value: string): boolean {
    console.log('showRequiredWarning( ' + isRequired + ', "' + value + '")');
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

  public canCreateCertificate(): boolean {
    const canCreateCertificate = !this.showRegExpFieldWarning(this.$v.clientAuthSecret.$model, this.regExpSecret());
    console.log('canCreateCertificate: ' + canCreateCertificate);
    return canCreateCertificate;
  }

  public canSubmit(): boolean {
    if (this.credentialChange.credentialUpdateType === 'CLIENT_CERT') {
      if (this.credentialChange.clientAuthCertId === 0) {
        console.log('canSubmit, CLIENT_CERT : clientAuthCertId === 0');
        return false;
      }
      if (!this.canCreateCertificate()) {
        console.log('canSubmit, CLIENT_CERT : clientAuthSecret does not match regExp');
        return false;
      }
    } else if (this.credentialChange.credentialUpdateType === 'TOTP') {
      console.log('canSubmit, TOTP');
    }

    console.log('canSubmit: currentPassword.$invalid: ' + this.$v.resetPassword.currentPassword.$invalid);
    return !this.$v.resetPassword.currentPassword.$invalid;
  }

  public toLocalDate(date: Date): string {
    const dateObj = new Date(date);
    if (dateObj.getFullYear() < 9990) {
      return dateObj.toLocaleDateString();
    }
    return '';
  }

  public prepareRemove(instance: IAccountCredentialView): void {
    this.removeInstance = instance;
  }

  public removeCredential(): void {
    const self = this;

    const url =
      '/api/account/credentials/' + encodeURIComponent(this.removeInstance.relationType) + '/' + encodeURIComponent(this.removeInstance.id);

    axios.delete(url).then(function (response) {
      window.console.info('delete credentials returns ' + response.status);
      self.getCredentials();
      self.closeDialog();
    });
  }

  public closeDialog(): void {
    (<any>this.$refs.removeEntity).hide();
  }
}
