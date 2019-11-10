import { ICertificate } from 'app/shared/model/certificate.model';

export interface ICertificateAttribute {
  id?: number;
  attributeId?: number;
  name?: string;
  value?: string;
  certificate?: ICertificate;
}

export class CertificateAttribute implements ICertificateAttribute {
  constructor(
    public id?: number,
    public attributeId?: number,
    public name?: string,
    public value?: string,
    public certificate?: ICertificate
  ) {}
}
