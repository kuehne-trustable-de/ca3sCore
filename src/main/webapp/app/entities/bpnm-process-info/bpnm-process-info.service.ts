import axios from 'axios';

import { IBPNMProcessInfo } from '@/shared/model/bpmn-process-info.model';

const baseApiUrl = 'api/bpnm-process-infos';

export default class BPNMProcessInfoService {
  public find(id: number): Promise<IBPNMProcessInfo> {
    return new Promise<IBPNMProcessInfo>(resolve => {
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

  public create(entity: IBPNMProcessInfo): Promise<IBPNMProcessInfo> {
    return new Promise<IBPNMProcessInfo>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IBPNMProcessInfo): Promise<IBPNMProcessInfo> {
    return new Promise<IBPNMProcessInfo>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
