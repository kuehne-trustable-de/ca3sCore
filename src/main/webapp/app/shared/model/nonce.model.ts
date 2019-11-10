import { Moment } from 'moment';

export interface INonce {
  id?: number;
  nonceValue?: string;
  expiresAt?: Moment;
}

export class Nonce implements INonce {
  constructor(public id?: number, public nonceValue?: string, public expiresAt?: Moment) {}
}
