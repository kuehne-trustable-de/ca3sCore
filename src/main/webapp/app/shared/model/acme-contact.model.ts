import { IAcmeAccount } from '@/shared/model/acme-account.model';

export interface IAcmeContact {
  id?: number;
  contactId?: number;
  contactUrl?: string;
  account?: IAcmeAccount;
}

export class AcmeContact implements IAcmeContact {
  constructor(public id?: number, public contactId?: number, public contactUrl?: string, public account?: IAcmeAccount) {}
}
