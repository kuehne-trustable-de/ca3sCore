import axios from 'axios';

import { IRequestAttributeValue } from '@/shared/model/request-attribute-value.model';

const baseApiUrl = 'api/request-attribute-values';

export default class RequestAttributeValueService {
  public find(id: number): Promise<IRequestAttributeValue> {
    return new Promise<IRequestAttributeValue>(resolve => {
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

  public create(entity: IRequestAttributeValue): Promise<IRequestAttributeValue> {
    return new Promise<IRequestAttributeValue>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IRequestAttributeValue): Promise<IRequestAttributeValue> {
    return new Promise<IRequestAttributeValue>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
