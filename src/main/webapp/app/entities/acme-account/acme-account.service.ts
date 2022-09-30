import axios from 'axios';

import { IAcmeAccount } from '@/shared/model/acme-account.model';

const baseApiUrl = 'api/acme-accounts';

export default class AcmeAccountService {
  public find(id: number): Promise<IAcmeAccount> {
    return new Promise<IAcmeAccount>(resolve => {
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

  public create(entity: IAcmeAccount): Promise<IAcmeAccount> {
    return new Promise<IAcmeAccount>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IAcmeAccount): Promise<IAcmeAccount> {
    return new Promise<IAcmeAccount>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
