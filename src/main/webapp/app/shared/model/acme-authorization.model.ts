import { IAcmeChallenge } from '@/shared/model/acme-challenge.model';
import { IAcmeOrder } from '@/shared/model/acme-order.model';

export interface IAcmeAuthorization {
  id?: number;
  acmeAuthorizationId?: number;
  type?: string;
  value?: string;
  challenges?: IAcmeChallenge[];
  order?: IAcmeOrder;
}

export class AcmeAuthorization implements IAcmeAuthorization {
  constructor(
    public id?: number,
    public acmeAuthorizationId?: number,
    public type?: string,
    public value?: string,
    public challenges?: IAcmeChallenge[],
    public order?: IAcmeOrder
  ) {}
}
