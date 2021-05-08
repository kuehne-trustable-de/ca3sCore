export const enum BPMNProcessType {
  CA_INVOCATION = 'CA_INVOCATION',
  REQUEST_AUTHORIZATION = 'REQUEST_AUTHORIZATION'
}

export interface IBPMNProcessInfo {
  id?: number;
  name?: string;
  version?: string;
  type?: BPMNProcessType;
  author?: string;
  lastChange?: Date;
  signatureBase64?: string;
  bpmnHashBase64?: string;
  processId?: string;
}

export class BPMNProcessInfo implements IBPMNProcessInfo {
  constructor(
    public id?: number,
    public name?: string,
    public version?: string,
    public type?: BPMNProcessType,
    public author?: string,
    public lastChange?: Date,
    public signatureBase64?: string,
    public bpmnHashBase64?: string,
    public processId?: string
  ) {}
}
