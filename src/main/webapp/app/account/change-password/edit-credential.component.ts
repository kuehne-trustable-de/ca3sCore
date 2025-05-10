import { maxLength, minLength, required, sameAs } from 'vuelidate/lib/validators';
import axios, { AxiosError } from 'axios';
import { mapGetters } from 'vuex';
import Component, { mixins } from 'vue-class-component';
import AlertMixin from '@/shared/alert/alert.mixin';
import { IAccountCredentialView, IPasswordChangeDTO } from '@/shared/model/transfer-object.model';
import { Vue } from 'vue-property-decorator';

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
export default class EditCredential extends mixins(AlertMixin, Vue) {
  base32RegEx: string = '^(?:[A-Z2-7]{8})*(?:[A-Z2-7]{2}={6}|[A-Z2-7]{4}={4}|[A-Z2-7]{5}={3}|[A-Z2-7]{7}=)?$';
  totpRegEx: string = '^([0-9]{6})$';

  success: string = null;
  error: string = null;
  doNotMatch: string = null;
  oldPasswordMismatch = false;
  updateCounter: number = 1;

  public smsSent: string = '';

  credentialChange: IPasswordChangeDTO = { credentialUpdateType: 'TOTP' };

  resetPassword: any = {
    currentPassword: null,
    newPassword: null,
    confirmPassword: null,
  };
  clientAuthSecret: string = null;

  qrCodeImgUrl: string = null;

  public accountCredentialArr: IAccountCredentialView[] = [];

  public currentAccountCredentialId: number = -1;
  public useGivenSeed: boolean = false;

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.credentialType) {
        vm.credentialChange.credentialUpdateType = to.params.credentialType;
      }
      if (to.params.accountCredentialId) {
        vm.currentAccountCredentialId = to.params.accountCredentialId;
      }
    });
  }

  public mounted(): void {
    this.getCredentials();
    if (this.credentialChange.credentialUpdateType === 'TOTP') {
      this.initOtp();
    }
    this.credentialChange.clientAuthCertId = 0;
  }

  public saveCredentials(): void {
    this.credentialChange.currentPassword = this.resetPassword.currentPassword;
    let self = this;
    this.oldPasswordMismatch = false;

    axios
      .post('api/account/change-password', this.credentialChange)
      .then(() => {
        self.success = 'OK';
        self.credentialChange.clientAuthCertId = 0;
        self.error = null;

        self.$router.push('/account/password');

        const message = this.$t('ca3SApp.messages.credential.created');
        self.alertService().showAlert(message, 'success');
      })
      .catch(function (error) {
        self.success = null;
        self.error = 'ERROR';

        const err = error as AxiosError;

        if (err.response.status === 400) {
          if (err.response.data.type === 'https://trustable.eu/problem/invalid-password') {
            self.credentialChange.currentPassword = '';
            document.getElementById('currentPassword').focus();
            self.oldPasswordMismatch = true;
          } else if (err.response.data.type === 'https://trustable.eu/problem/problem-with-message') {
            self.credentialChange.otpTestValue = '';
            document.getElementById('otp-test-value').focus();
          }
          self.alertService().showAlert(err.response.data.message, 'warn');
        } else {
          self.alertService().showAlert('error', 'warn');
        }
        self.getAlertFromStore();
      });
  }

  public getCredentials(): void {
    window.console.info('calling getCredentials');
    let self = this;

    axios({
      method: 'get',
      url: 'api/account/credentials',
      responseType: 'stream',
    })
      .then(function (response) {
        window.console.info('getCredentials returns ' + response.data);
        self.accountCredentialArr = response.data;
      })
      .catch(function (error) {
        console.log(error);
        const message = self.$t('problem processing request: ' + error);

        const err = error as AxiosError;
        if (err.response) {
          console.log(err.response.status);
          console.log(err.response.data);
          if (err.response.status === 401) {
            self.alertService().showAlert('Action not allowed', 'warn');
          } else {
            self.alertService().showAlert(message, 'info');
          }
        } else {
          self.alertService().showAlert(message, 'info');
        }
        self.getAlertFromStore();
      });
  }

  public get username(): string {
    return this.$store.getters.account?.login ?? '';
  }

  public keystoreFilename(): string {
    return 'personalClientCertificate.p12';
  }
  public downloadPersonalCertificate(secret: string) {
    const mimetype = 'application/x-pkcs12';
    const pbeAlgo: string = this.$store.state.uiConfigStore.config.cryptoConfigView.defaultPBEAlgo;

    const url = '/publicapi/clientAuthKeystore/' + this.username + '/' + encodeURIComponent(this.keystoreFilename());

    const headers: any = {
      //      Accept: mimetype,
      X_pbeAlgo: pbeAlgo,
      X_keyEx: false,
    };

    this.download(url, secret, this.keystoreFilename(), mimetype, headers);

    this.success = ''; // refresh
  }

  public download(url: string, secret: string, filename: string, mimetype: string, headers: any): void {
    this.credentialChange.clientAuthCertId = 0;

    const self = this;

    const config = {};
    config['responseType'] = 'blob';
    config['headers'] = headers;

    const data = {};
    data['secret'] = secret;

    axios
      .post(url, data, config)
      .then(response => {
        self.credentialChange.clientAuthCertId = parseInt(response.headers['x-cert-id']);
        self.updateCounter++;

        const blob = new Blob([response.data], { type: mimetype, endings: 'transparent' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.type = mimetype;

        window.console.info('cert ' + self.credentialChange.clientAuthCertId + ' downloaded, tmp download lnk : ' + link.href);

        link.click();
        URL.revokeObjectURL(link.href);
      })
      .catch(function (error) {
        console.log(error);
        const message = self.$t('problem processing request: ' + error);

        const err = error as AxiosError;
        if (err.response) {
          console.log(err.response.status);
          console.log(err.response.data);
          if (err.response.status === 401) {
            self.alertService().showAlert('Action not allowed', 'warn');
          } else {
            self.alertService().showAlert(message, 'info');
          }
        } else {
          self.alertService().showAlert(message, 'info');
        }
        self.getAlertFromStore();
      });
  }

  public requestClientCert(): void {
    const clientAuthTarget = this.$store.state.uiConfigStore.config.cryptoConfigView.clientAuthTarget;

    const self = this;

    axios
      //      .post(clientAuthTarget + '/publicapi/clientAuth', userLoginData
      .get(clientAuthTarget + '/publicapi/clientAuth')
      .then(result => {
        console.info('connected to client auth port');
        if (result.data && result.data.id_token) {
          console.info('client_cert_token: ' + result.data.id_token);
        }
      })
      .catch(error => {
        // Handle the error response
        console.error('----' + error);
      });
  }

  public showBase32RegExpFieldWarning(value: string): boolean {
    const regexp = new RegExp(this.base32RegEx);
    const valid = regexp.test(value);
    console.log('showBase32RegExpFieldWarning( ' + this.base32RegEx + ', "' + value + '") -> ' + valid);
    return !valid;
  }

  public showTOTPExpFieldWarning(value: string): boolean {
    const regexp = new RegExp(this.totpRegEx);
    const valid = regexp.test(value);
    console.log('showTOTPExpFieldWarning( ' + this.totpRegEx + ', "' + value + '") -> ' + valid);
    return !valid;
  }

  public updateSeedMode(): void {
    this.credentialChange.seed = '';
    if (!this.useGivenSeed) {
      this.initOtp();
    }
  }

  public initOtp(): void {
    const self = this;
    axios
      .post('/api/account/initOTP')
      .then(response => {
        self.credentialChange.seed = response.data['seed'];
        self.qrCodeImgUrl = 'data:image/png;base64,' + response.data['qrCodeImg'];
      })
      .catch(function (error) {
        console.log(error);
        const message = self.$t('problem processing request: ' + error);

        const err = error as AxiosError;
        if (err.response) {
          console.log(err.response.status);
          console.log(err.response.data);
          if (err.response.status === 401) {
            self.alertService().showAlert('Action not allowed', 'warn');
          } else {
            self.alertService().showAlert(message, 'info');
          }
        } else {
          self.alertService().showAlert(message, 'info');
        }
        self.getAlertFromStore();
      });
  }

  public sendSMS(): void {
    this.smsSent = '';
    const self = this;
    axios
      .post('/api/smsDelivery')
      .then(response => {
        self.smsSent = response.data.toString();
        //        document.getElementById("sms-test-value").focus();
      })
      .catch(function (error) {
        console.log(error);
        const message = self.$t('problem processing request: ' + error);

        const err = error as AxiosError;
        if (err.response) {
          console.log(err.response.status);
          console.log(err.response.data);
          if (err.response.status === 401) {
            self.alertService().showAlert('Action not allowed', 'warn');
          } else {
            self.alertService().showAlert(message, 'info');
          }
        } else {
          self.alertService().showAlert(message, 'info');
        }
        self.getAlertFromStore();
      });
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
    } else if (this.credentialChange.credentialUpdateType === 'TOTP') {
      if (
        this.showBase32RegExpFieldWarning(this.credentialChange.seed) ||
        this.showTOTPExpFieldWarning(this.credentialChange.otpTestValue)
      ) {
        console.log('canSubmit TOTP: seed or test value invalid');
        return false;
      }
      console.log('canSubmit TOTP: values OK');
    } else if (this.credentialChange.credentialUpdateType === 'SMS') {
      if (this.smsSent.length === 0) {
        console.log('canSubmit SMS: no test SMS sent');
        return false;
      }
      if (this.showTOTPExpFieldWarning(this.credentialChange.otpTestValue)) {
        console.log('canSubmit SMS: seed or test value invalid');
        return false;
      }
      if (this.credentialChange.otpTestValue.trim() !== this.smsSent.trim()) {
        console.log('canSubmit SMS: test value and SMS value do nat match');
        return false;
      }

      console.log('canSubmit SMS: values OK');
    }

    console.log('canSubmit: currentPassword.$invalid: ' + this.$v.resetPassword.currentPassword.$invalid);
    return !this.$v.resetPassword.currentPassword.$invalid;
  }

  public previousState() {
    this.$router.go(-1);
  }
}
