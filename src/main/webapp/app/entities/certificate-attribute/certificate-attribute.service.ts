import axios from 'axios';

import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';

const baseApiUrl = 'api/certificate-attributes';

export default class CertificateAttributeService {
  public find(id: number): Promise<ICertificateAttribute> {
    return new Promise<ICertificateAttribute>(resolve => {
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

  public create(entity: ICertificateAttribute): Promise<ICertificateAttribute> {
    return new Promise<ICertificateAttribute>(resolve => {
      axios.post(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }

  public update(entity: ICertificateAttribute): Promise<ICertificateAttribute> {
    return new Promise<ICertificateAttribute>(resolve => {
      axios.put(`${baseApiUrl}`, entity).then(function(res) {
        resolve(res.data);
      });
    });
  }
}
