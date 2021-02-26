import { Component, Vue } from 'vue-property-decorator';
import axios from 'axios';
import { ISerializable } from '@/shared/model/transfer-object.model';

export interface IInfo {
  git?: IGit;
  activeProfiles?: string[];
}

export interface IGit {
  build?: IBuild;
  commit?: ICommit;
}

export interface IBuild {
  host?: string;
  version?: string;
  time?: string;
  branch?: string;
}

export interface ICommit {
  id?: IId;
  time?: string;
}

export interface IId {
  describe?: string;
  abbrev?: string;
  full?: string;
}

@Component
export default class Info extends Vue {
  public info: IInfo = {};

  public mounted(): void {
    this.getInfo();
  }

  public getInfo(): void {
    window.console.info('calling getPreference');
    const self = this;

    axios({
      method: 'get',
      url: 'management/info',
      responseType: 'stream'
    }).then(function(response) {
      window.console.info('management/info returns ' + response.data);
      self.info = response.data;
      window.console.info('management/info returns ' + self.info);
    });
  }
}
