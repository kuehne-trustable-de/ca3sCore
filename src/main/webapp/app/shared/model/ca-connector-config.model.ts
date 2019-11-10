import { CAConnectorType } from 'app/shared/model/enumerations/ca-connector-type.model';

export interface ICAConnectorConfig {
  id?: number;
  configId?: number;
  name?: string;
  caConnectorType?: CAConnectorType;
  caUrl?: string;
  secret?: string;
  pollingOffset?: number;
  active?: boolean;
}

export class CAConnectorConfig implements ICAConnectorConfig {
  constructor(
    public id?: number,
    public configId?: number,
    public name?: string,
    public caConnectorType?: CAConnectorType,
    public caUrl?: string,
    public secret?: string,
    public pollingOffset?: number,
    public active?: boolean
  ) {
    this.active = this.active || false;
  }
}
