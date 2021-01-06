import axios from 'axios';

import { IRequestAttribute } from '@/shared/model/request-attribute.model';

const baseApiUrl = 'api/request-attributes';

export default class RequestAttributeService {
  public find(id: number): Promise<IRequestAttribute> {
    return new Promise<IRequestAttribute>((resolve, reject) => {
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

  public create(entity: IRequestAttribute): Promise<IRequestAttribute> {
    return new Promise<IRequestAttribute>((resolve, reject) => {
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

  public update(entity: IRequestAttribute): Promise<IRequestAttribute> {
    return new Promise<IRequestAttribute>((resolve, reject) => {
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
