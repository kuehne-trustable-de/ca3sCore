import axios from 'axios';

import { IRDNAttribute } from '@/shared/model/rdn-attribute.model';

const baseApiUrl = 'api/rdn-attributes';

export default class RDNAttributeService {
  public find(id: number): Promise<IRDNAttribute> {
    return new Promise<IRDNAttribute>(resolve => {
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

  public create(entity: IRDNAttribute): Promise<IRDNAttribute> {
    return new Promise<IRDNAttribute>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IRDNAttribute): Promise<IRDNAttribute> {
    return new Promise<IRDNAttribute>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
