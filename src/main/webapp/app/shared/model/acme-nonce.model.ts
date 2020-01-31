export interface IAcmeNonce {
  id?: number;
  nonceValue?: string;
  expiresAt?: Date;
}

export class AcmeNonce implements IAcmeNonce {
  constructor(public id?: number, public nonceValue?: string, public expiresAt?: Date) {}
}
