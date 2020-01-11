import axios from 'axios';

import { IPipelineAttribute } from '@/shared/model/pipeline-attribute.model';

const baseApiUrl = 'api/pipeline-attributes';

export default class PipelineAttributeService {
  public find(id: number): Promise<IPipelineAttribute> {
    return new Promise<IPipelineAttribute>(resolve => {
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

  public create(entity: IPipelineAttribute): Promise<IPipelineAttribute> {
    return new Promise<IPipelineAttribute>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IPipelineAttribute): Promise<IPipelineAttribute> {
    return new Promise<IPipelineAttribute>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
