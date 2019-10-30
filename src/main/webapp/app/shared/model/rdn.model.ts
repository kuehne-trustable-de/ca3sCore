import { IRDNAttribute } from 'app/shared/model/rdn-attribute.model';
import { ICSR } from 'app/shared/model/csr.model';

export interface IRDN {
  id?: number;
  rdnAttributes?: IRDNAttribute[];
  csr?: ICSR;
}

export class RDN implements IRDN {
  constructor(public id?: number, public rdnAttributes?: IRDNAttribute[], public csr?: ICSR) {}
}
