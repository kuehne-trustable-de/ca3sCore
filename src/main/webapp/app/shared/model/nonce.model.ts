export interface INonce {
  id?: number;
  nonceValue?: string;
  expiresAt?: Date;
}

export class Nonce implements INonce {
  constructor(public id?: number, public nonceValue?: string, public expiresAt?: Date) {}
}
