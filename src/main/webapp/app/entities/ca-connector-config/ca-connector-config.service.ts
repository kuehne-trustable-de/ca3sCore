import axios from 'axios';

import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';

const baseApiUrl = 'api/ca-connector-configs';

export default class CAConnectorConfigService {
  public find(id: number): Promise<ICAConnectorConfig> {
    return new Promise<ICAConnectorConfig>(resolve => {
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

  public create(entity: ICAConnectorConfig): Promise<ICAConnectorConfig> {
    return new Promise<ICAConnectorConfig>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: ICAConnectorConfig): Promise<ICAConnectorConfig> {
    return new Promise<ICAConnectorConfig>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
