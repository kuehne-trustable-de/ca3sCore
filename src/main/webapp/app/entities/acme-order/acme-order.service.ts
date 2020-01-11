import axios from 'axios';

import { IAcmeOrder } from '@/shared/model/acme-order.model';

const baseApiUrl = 'api/acme-orders';

export default class AcmeOrderService {
  public find(id: number): Promise<IAcmeOrder> {
    return new Promise<IAcmeOrder>(resolve => {
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

  public create(entity: IAcmeOrder): Promise<IAcmeOrder> {
    return new Promise<IAcmeOrder>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IAcmeOrder): Promise<IAcmeOrder> {
    return new Promise<IAcmeOrder>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
