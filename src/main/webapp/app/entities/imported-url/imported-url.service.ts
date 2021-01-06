import axios from 'axios';

import { IImportedURL } from '@/shared/model/imported-url.model';

const baseApiUrl = 'api/imported-urls';

export default class ImportedURLService {
  public find(id: number): Promise<IImportedURL> {
    return new Promise<IImportedURL>((resolve, reject) => {
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

  public create(entity: IImportedURL): Promise<IImportedURL> {
    return new Promise<IImportedURL>((resolve, reject) => {
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

  public update(entity: IImportedURL): Promise<IImportedURL> {
    return new Promise<IImportedURL>((resolve, reject) => {
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
