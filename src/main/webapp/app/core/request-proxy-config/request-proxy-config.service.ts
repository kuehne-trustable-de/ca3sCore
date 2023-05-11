import axios from 'axios';

import { IRequestProxyConfigView } from '@/shared/model/transfer-object.model';

const baseApiUrl = 'api/request-proxy-config';

export default class RequestProxyConfigService {
  public find(id: number): Promise<IRequestProxyConfigView> {
    return new Promise<IRequestProxyConfigView>(resolve => {
      axios.get(`${baseApiUrl}/${id}`).then(function (res) {
        resolve(res.data);
      });
    });
  }

  public retrieve(): Promise<any> {
    return new Promise<any>(resolve => {
      axios.get(baseApiUrl).then(function (res) {
        resolve(res);
      });
    });
  }

  public delete(id: number): Promise<any> {
    return new Promise<any>(resolve => {
      axios.delete(`${baseApiUrl}/${id}`).then(function (res) {
        resolve(res);
      });
    });
  }

  public create(entity: IRequestProxyConfigView): Promise<IRequestProxyConfigView> {
    return new Promise<IRequestProxyConfigView>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function (res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IRequestProxyConfigView): Promise<IRequestProxyConfigView> {
    return new Promise<IRequestProxyConfigView>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function (res) {
        resolve(res.data);
      });
    });
  }
}
