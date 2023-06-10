import axios from 'axios';

import { ICaConnectorConfigView } from '@/shared/model/transfer-object.model';

const baseApiUrl = 'api/ca-connector-configViews';

export default class CAConnectorConfigViewService {
  public find(id: number): Promise<ICaConnectorConfigView> {
    return new Promise<ICaConnectorConfigView>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(function (res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public retrieve(): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(baseApiUrl)
        .then(function (res) {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public delete(id: number): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .delete(`${baseApiUrl}/${id}`)
        .then(function (res) {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public create(entity: ICaConnectorConfigView): Promise<ICaConnectorConfigView> {
    return new Promise<ICaConnectorConfigView>((resolve, reject) => {
      axios
        .post(`${baseApiUrl}`, entity)
        .then(function (res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public update(entity: ICaConnectorConfigView): Promise<ICaConnectorConfigView> {
    return new Promise<ICaConnectorConfigView>((resolve, reject) => {
      axios
        .put(`${baseApiUrl}`, entity)
        .then(function (res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }
}
