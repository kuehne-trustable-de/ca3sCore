import axios from 'axios';

import { IRequestProxyConfig } from '@/shared/model/request-proxy-config.model';

const baseApiUrl = 'api/request-proxy-configs';

export default class RequestProxyConfigService {
  public find(id: number): Promise<IRequestProxyConfig> {
    return new Promise<IRequestProxyConfig>(resolve => {
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

  public create(entity: IRequestProxyConfig): Promise<IRequestProxyConfig> {
    return new Promise<IRequestProxyConfig>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IRequestProxyConfig): Promise<IRequestProxyConfig> {
    return new Promise<IRequestProxyConfig>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
