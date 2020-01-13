export const enum CAConnectorType {
  INTERNAL = 'INTERNAL',
  CMP = 'CMP',
  ADCS = 'ADCS'
}

export interface ICAConnectorConfig {
  id?: number;
  name?: string;
  caConnectorType?: CAConnectorType;
  caUrl?: string;
  secret?: string;
  pollingOffset?: number;
  defaultCA?: boolean;
  active?: boolean;
  selector?: string;
}

export class CAConnectorConfig implements ICAConnectorConfig {
  constructor(
    public id?: number,
    public name?: string,
    public caConnectorType?: CAConnectorType,
    public caUrl?: string,
    public secret?: string,
    public pollingOffset?: number,
    public defaultCA?: boolean,
    public active?: boolean,
    public selector?: string
  ) {
    this.defaultCA = this.defaultCA || false;
    this.active = this.active || false;
  }
}
