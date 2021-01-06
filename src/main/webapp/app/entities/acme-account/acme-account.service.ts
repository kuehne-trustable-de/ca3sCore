import axios from 'axios';

import { IACMEAccount } from '@/shared/model/acme-account.model';

const baseApiUrl = 'api/acme-accounts';

export default class ACMEAccountService {
  public find(id: number): Promise<IACMEAccount> {
    return new Promise<IACMEAccount>((resolve, reject) => {
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

  public retrieve(): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(baseApiUrl)
        .then(function (res) {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public delete(id: number): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .delete(`${baseApiUrl}/${id}`)
        .then(function (res) {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public create(entity: IACMEAccount): Promise<IACMEAccount> {
    return new Promise<IACMEAccount>((resolve, reject) => {
      axios
        .post(`${baseApiUrl}`, entity)
        .then(function (res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public update(entity: IACMEAccount): Promise<IACMEAccount> {
    return new Promise<IACMEAccount>((resolve, reject) => {
      axios
        .put(`${baseApiUrl}`, entity)
        .then(function (res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }
}
