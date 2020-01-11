import axios from 'axios';

import { ICsrAttribute } from '@/shared/model/csr-attribute.model';

const baseApiUrl = 'api/csr-attributes';

export default class CsrAttributeService {
  public find(id: number): Promise<ICsrAttribute> {
    return new Promise<ICsrAttribute>(resolve => {
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

  public create(entity: ICsrAttribute): Promise<ICsrAttribute> {
    return new Promise<ICsrAttribute>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: ICsrAttribute): Promise<ICsrAttribute> {
    return new Promise<ICsrAttribute>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
