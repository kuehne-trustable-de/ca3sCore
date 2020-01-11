import axios from 'axios';

import buildPaginationQueryOpts from '@/shared/sort/sorts';

import { ICertificate } from '@/shared/model/certificate.model';

const baseApiUrl = 'api/certificates';

export default class CertificateService {
  public find(id: number): Promise<ICertificate> {
    return new Promise<ICertificate>(resolve => {
      axios.get(`${baseApiUrl}/${id}`).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public retrieve(paginationQuery?: any): Promise<any> {
    return new Promise<any>(resolve => {
      axios.get(baseApiUrl + `?${buildPaginationQueryOpts(paginationQuery)}`).then(function(res) {
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

  public create(entity: ICertificate): Promise<ICertificate> {
    return new Promise<ICertificate>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: ICertificate): Promise<ICertificate> {
    return new Promise<ICertificate>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
