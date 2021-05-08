import axios from 'axios';

import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';

const baseApiUrl = 'api/bpmn-process-infos';

export default class BPNMProcessInfoService {
  public find(id: number): Promise<IBPMNProcessInfo> {
    return new Promise<IBPMNProcessInfo>(resolve => {
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

  public create(entity: IBPMNProcessInfo): Promise<IBPMNProcessInfo> {
    return new Promise<IBPMNProcessInfo>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IBPMNProcessInfo): Promise<IBPMNProcessInfo> {
    return new Promise<IBPMNProcessInfo>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
