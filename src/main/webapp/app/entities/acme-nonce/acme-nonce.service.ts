import axios from 'axios';

import { IAcmeNonce } from '@/shared/model/acme-nonce.model';

const baseApiUrl = 'api/acme-nonces';

export default class AcmeNonceService {
  public find(id: number): Promise<IAcmeNonce> {
    return new Promise<IAcmeNonce>(resolve => {
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

  public create(entity: IAcmeNonce): Promise<IAcmeNonce> {
    return new Promise<IAcmeNonce>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: IAcmeNonce): Promise<IAcmeNonce> {
    return new Promise<IAcmeNonce>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
