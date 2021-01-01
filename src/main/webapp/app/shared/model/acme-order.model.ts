import { IAcmeAuthorization } from '@/shared/model/acme-authorization.model';
import { IAcmeIdentifier } from '@/shared/model/acme-identifier.model';
import { ICSR } from '@/shared/model/csr.model';
import { ICertificate } from '@/shared/model/certificate.model';
import { IACMEAccount } from '@/shared/model/acme-account.model';

export const enum AcmeOrderStatus {
  PENDING = 'PENDING',
  READY = 'READY',
  PROCESSING = 'PROCESSING',
  VALID = 'VALID',
  INVALID = 'INVALID'
}

export interface IAcmeOrder {
  id?: number;
  orderId?: number;
  status?: AcmeOrderStatus;
  expires?: Date;
  notBefore?: Date;
  notAfter?: Date;
  error?: string;
  finalizeUrl?: string;
  certificateUrl?: string;
  acmeAuthorizations?: IAcmeAuthorization[];
  acmeIdentifiers?: IAcmeIdentifier[];
  csr?: ICSR;
  certificate?: ICertificate;
  account?: IACMEAccount;
}

export class AcmeOrder implements IAcmeOrder {
  constructor(
    public id?: number,
    public orderId?: number,
    public status?: AcmeOrderStatus,
    public expires?: Date,
    public notBefore?: Date,
    public notAfter?: Date,
    public error?: string,
    public finalizeUrl?: string,
    public certificateUrl?: string,
    public acmeAuthorizations?: IAcmeAuthorization[],
    public acmeIdentifiers?: IAcmeIdentifier[],
    public csr?: ICSR,
    public certificate?: ICertificate,
    public account?: IACMEAccount
  ) {}
}
