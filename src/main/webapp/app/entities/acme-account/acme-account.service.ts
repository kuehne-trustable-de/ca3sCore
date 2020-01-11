import axios from 'axios';

import { IACMEAccount } from '@/shared/model/acme-account.model';

const baseApiUrl = 'api/acme-accounts';

export default class ACMEAccountService {
  public find(id: number): Promise<IACMEAccount> {
    return new Promise<IACMEAccount>(resolve => {
      axios.get(`${baseApiUrl}/${id}`).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public retrieve(): Promise<any> {
    return new Promise<any>(resolve => {
      axios.get(baseApiUrl).then(function(res) {
        resolve(res);
      });
    });
  }

  public delete(id: number): Promise<any> {
    return new Promise<any>(resolve => {
      axios.delete(`${baseApiUrl}/${id}`).then(function(res) {
        resolve(res);
      });
    });
  }

  public create(entity: IACMEAccount): Promise<IACMEAccount> {
    return new Promise<IACMEAccount>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IACMEAccount): Promise<IACMEAccount> {
    return new Promise<IACMEAccount>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
