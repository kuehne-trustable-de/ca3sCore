import axios from 'axios';

import { IPipeline } from '@/shared/model/pipeline.model';

const baseApiUrl = 'api/pipelines';

export default class PipelineService {
  public find(id: number): Promise<IPipeline> {
    return new Promise<IPipeline>(resolve => {
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

  public create(entity: IPipeline): Promise<IPipeline> {
    return new Promise<IPipeline>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IPipeline): Promise<IPipeline> {
    return new Promise<IPipeline>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
