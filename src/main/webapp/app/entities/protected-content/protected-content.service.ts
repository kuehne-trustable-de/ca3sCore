import axios from 'axios';

import { IProtectedContent } from '@/shared/model/protected-content.model';

const baseApiUrl = 'api/protected-contents';

export default class ProtectedContentService {
  public find(id: number): Promise<IProtectedContent> {
    return new Promise<IProtectedContent>(resolve => {
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

  public create(entity: IProtectedContent): Promise<IProtectedContent> {
    return new Promise<IProtectedContent>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IProtectedContent): Promise<IProtectedContent> {
    return new Promise<IProtectedContent>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
