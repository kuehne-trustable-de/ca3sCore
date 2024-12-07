import { email, maxLength, minLength, required } from 'vuelidate/lib/validators';
import axios from 'axios';
import { Vue, Component } from 'vue-property-decorator';

const validations = {
  resetAccount: {
    username: {
      required,
      minLength: minLength(5),
      maxLength: maxLength(254),
    },
  },
};

interface ResetAccount {
  username: string;
}

@Component({
  validations,
})
export default class ResetPasswordInit extends Vue {
  public success: boolean = null;
  public error: string = null;
  public resetAccount: ResetAccount = {
    username: null,
  };

  public requestReset(): void {
    this.error = null;
    axios
      .post('api/account/reset-password/init', this.resetAccount.username, {
        headers: {
          'content-type': 'text/plain',
        },
      })
      .then(() => {
        this.success = true;
      })
      .catch(() => {
        this.success = null;
        this.error = 'ERROR';
      });
  }
}
