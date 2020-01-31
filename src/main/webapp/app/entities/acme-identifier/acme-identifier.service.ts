import axios from 'axios';

import { IAcmeIdentifier } from '@/shared/model/acme-identifier.model';

const baseApiUrl = 'api/acme-identifiers';

export default class AcmeIdentifierService {
  public find(id: number): Promise<IAcmeIdentifier> {
    return new Promise<IAcmeIdentifier>(resolve => {
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

  public create(entity: IAcmeIdentifier): Promise<IAcmeIdentifier> {
    return new Promise<IAcmeIdentifier>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IAcmeIdentifier): Promise<IAcmeIdentifier> {
    return new Promise<IAcmeIdentifier>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
