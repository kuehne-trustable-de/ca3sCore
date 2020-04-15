import { ICertificate } from '@/shared/model/certificate.model';

export interface ICertificateAttribute {
  id?: number;
  name?: string;
  value?: string;
  certificate?: ICertificate;
}

export class CertificateAttribute implements ICertificateAttribute {
  constructor(public id?: number, public name?: string, public value?: string, public certificate?: ICertificate) {}
}
