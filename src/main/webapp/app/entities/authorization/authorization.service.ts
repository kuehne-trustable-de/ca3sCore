import axios from 'axios';

import { IAuthorization } from '@/shared/model/authorization.model';

const baseApiUrl = 'api/authorizations';

export default class AuthorizationService {
  public find(id: number): Promise<IAuthorization> {
    return new Promise<IAuthorization>(resolve => {
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

  public create(entity: IAuthorization): Promise<IAuthorization> {
    return new Promise<IAuthorization>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IAuthorization): Promise<IAuthorization> {
    return new Promise<IAuthorization>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
