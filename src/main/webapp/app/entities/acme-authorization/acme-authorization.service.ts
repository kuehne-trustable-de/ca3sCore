import axios from 'axios';

import { IAcmeAuthorization } from '@/shared/model/acme-authorization.model';

const baseApiUrl = 'api/acme-authorizations';

export default class AcmeAuthorizationService {
  public find(id: number): Promise<IAcmeAuthorization> {
    return new Promise<IAcmeAuthorization>(resolve => {
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

  public create(entity: IAcmeAuthorization): Promise<IAcmeAuthorization> {
    return new Promise<IAcmeAuthorization>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IAcmeAuthorization): Promise<IAcmeAuthorization> {
    return new Promise<IAcmeAuthorization>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
