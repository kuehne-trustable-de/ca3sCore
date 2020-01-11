import axios from 'axios';

import { IRequestAttribute } from '@/shared/model/request-attribute.model';

const baseApiUrl = 'api/request-attributes';

export default class RequestAttributeService {
  public find(id: number): Promise<IRequestAttribute> {
    return new Promise<IRequestAttribute>(resolve => {
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

  public create(entity: IRequestAttribute): Promise<IRequestAttribute> {
    return new Promise<IRequestAttribute>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IRequestAttribute): Promise<IRequestAttribute> {
    return new Promise<IRequestAttribute>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
