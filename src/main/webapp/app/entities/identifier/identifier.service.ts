import axios from 'axios';

import { IIdentifier } from '@/shared/model/identifier.model';

const baseApiUrl = 'api/identifiers';

export default class IdentifierService {
  public find(id: number): Promise<IIdentifier> {
    return new Promise<IIdentifier>(resolve => {
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

  public create(entity: IIdentifier): Promise<IIdentifier> {
    return new Promise<IIdentifier>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IIdentifier): Promise<IIdentifier> {
    return new Promise<IIdentifier>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
