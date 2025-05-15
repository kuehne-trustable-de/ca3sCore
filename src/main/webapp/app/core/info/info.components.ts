import { Component, Vue } from 'vue-property-decorator';
import axios from 'axios';
import { ISerializable } from '@/shared/model/transfer-object.model';

export interface IInfo {
  git?: IGit;
  activeProfiles?: string[];
}

export interface IGit {
  branch?: string;
  commit?: ICommit;
}

export interface ICommit {
  id?: string;
  time?: string;
}

@Component
export default class Info extends Vue {
  public info: IInfo = {};

  public mounted(): void {
    this.getInfo();
  }

  public getInfo(): void {
    window.console.info('calling management/info');
    const self = this;

    axios({
      method: 'get',
      url: '/actuator/info',
      responseType: 'stream',
    }).then(function (response) {
      window.console.info('actuator/info returns ' + response.data);
      self.info = response.data;
      window.console.info('actuator/info returns ' + self.info);
    });
  }

  public postSchedule(methodName: string): void {
    window.console.info('calling schedule ...');
    const self = this;

    axios({
      method: 'post',
      url: 'api/schedule/' + methodName,
      responseType: 'stream',
    }).then(function (response) {
      window.console.info('api/schedule returns ' + response.data);
      self.info = response.data;
      window.console.info('api/schedule returns ' + self.info);
    });
  }

  public isRAOfficer() {
    window.console.info('isRAOfficer: ' + this.hasRole('ROLE_RA') || this.hasRole('ROLE_RA_DOMAIN'));
    return this.hasRole('ROLE_RA') || this.hasRole('ROLE_RA_DOMAIN');
  }

  public isAdmin() {
    window.console.info('isAdmin: ' + this.hasRole('ROLE_ADMIN'));
    return this.hasRole('ROLE_ADMIN');
  }

  public isRAOrAdmin() {
    return this.isRAOfficer() || this.isAdmin();
  }

  public hasRole(targetRole: string) {
    if (this.$store.getters.account === null || this.$store.getters.account.authorities === null) {
      return false;
    }

    for (const role of this.$store.getters.account.authorities) {
      if (targetRole === role) {
        return true;
      }
    }
    return false;
  }
}
