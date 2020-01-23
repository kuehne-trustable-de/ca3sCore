import axios from 'axios';

import { IImportedURL } from '@/shared/model/imported-url.model';

const baseApiUrl = 'api/imported-urls';

export default class ImportedURLService {
  public find(id: number): Promise<IImportedURL> {
    return new Promise<IImportedURL>(resolve => {
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

  public create(entity: IImportedURL): Promise<IImportedURL> {
    return new Promise<IImportedURL>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IImportedURL): Promise<IImportedURL> {
    return new Promise<IImportedURL>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
