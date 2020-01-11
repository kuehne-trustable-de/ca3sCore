import axios from 'axios';

import { IAcmeContact } from '@/shared/model/acme-contact.model';

const baseApiUrl = 'api/acme-contacts';

export default class AcmeContactService {
  public find(id: number): Promise<IAcmeContact> {
    return new Promise<IAcmeContact>(resolve => {
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

  public create(entity: IAcmeContact): Promise<IAcmeContact> {
    return new Promise<IAcmeContact>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IAcmeContact): Promise<IAcmeContact> {
    return new Promise<IAcmeContact>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
