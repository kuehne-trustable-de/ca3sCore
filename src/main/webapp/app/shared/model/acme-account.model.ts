import { IAcmeContact } from '@/shared/model/acme-contact.model';
import { IAcmeOrder } from '@/shared/model/acme-order.model';

export const enum AccountStatus {
  VALID = 'VALID',
  DEACTIVATED = 'DEACTIVATED',
  REVOKED = 'REVOKED'
}

export interface IACMEAccount {
  id?: number;
  accountId?: number;
  realm?: string;
  status?: AccountStatus;
  termsOfServiceAgreed?: boolean;
  publicKeyHash?: string;
  publicKey?: any;
  contacts?: IAcmeContact[];
  orders?: IAcmeOrder[];
}

export class ACMEAccount implements IACMEAccount {
  constructor(
    public id?: number,
    public accountId?: number,
    public realm?: string,
    public status?: AccountStatus,
    public termsOfServiceAgreed?: boolean,
    public publicKeyHash?: string,
    public publicKey?: any,
    public contacts?: IAcmeContact[],
    public orders?: IAcmeOrder[]
  ) {
    this.termsOfServiceAgreed = this.termsOfServiceAgreed || false;
  }
}
