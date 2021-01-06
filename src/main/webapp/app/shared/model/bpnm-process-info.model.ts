export const enum BPNMProcessType {
  CA_INVOCATION = 'CA_INVOCATION',
  REQUEST_AUTHORIZATION = 'REQUEST_AUTHORIZATION',
}

export interface IBPNMProcessInfo {
  id?: number;
  name?: string;
  version?: string;
  type?: BPNMProcessType;
  author?: string;
  lastChange?: Date;
  signatureBase64?: any;
}

export class BPNMProcessInfo implements IBPNMProcessInfo {
  constructor(
    public id?: number,
    public name?: string,
    public version?: string,
    public type?: BPNMProcessType,
    public author?: string,
    public lastChange?: Date,
    public signatureBase64?: any
  ) {}
}
