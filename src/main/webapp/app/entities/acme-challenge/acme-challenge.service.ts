import axios from 'axios';

import { IAcmeChallenge } from '@/shared/model/acme-challenge.model';

const baseApiUrl = 'api/acme-challenges';

export default class AcmeChallengeService {
  public find(id: number): Promise<IAcmeChallenge> {
    return new Promise<IAcmeChallenge>(resolve => {
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

  public create(entity: IAcmeChallenge): Promise<IAcmeChallenge> {
    return new Promise<IAcmeChallenge>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IAcmeChallenge): Promise<IAcmeChallenge> {
    return new Promise<IAcmeChallenge>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
