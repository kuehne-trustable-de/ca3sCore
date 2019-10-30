import { IAcmeContact } from 'app/shared/model/acme-contact.model';
import { IAcmeOrder } from 'app/shared/model/acme-order.model';
import { AccountStatus } from 'app/shared/model/enumerations/account-status.model';

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
