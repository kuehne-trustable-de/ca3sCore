import { IProtectedContent } from '@/shared/model/protected-content.model';

export const enum CAConnectorType {
  INTERNAL = 'INTERNAL',
  CMP = 'CMP',
  ADCS = 'ADCS',
  ADCS_CERTIFICATE_INVENTORY = 'ADCS_CERTIFICATE_INVENTORY',
  DIRECTORY = 'DIRECTORY',
}

export const enum Interval {
  MINUTE = 'MINUTE',
  HOUR = 'HOUR',
  DAY = 'DAY',
  WEEK = 'WEEK',
  MONTH = 'MONTH',
}

export interface ICAConnectorConfig {
  id?: number;
  name?: string;
  caConnectorType?: CAConnectorType;
  caUrl?: string;
  pollingOffset?: number;
  defaultCA?: boolean;
  active?: boolean;
  selector?: string;
  interval?: Interval;
  plainSecret?: string;
  secret?: IProtectedContent;
}

export class CAConnectorConfig implements ICAConnectorConfig {
  constructor(
    public id?: number,
    public name?: string,
    public caConnectorType?: CAConnectorType,
    public caUrl?: string,
    public pollingOffset?: number,
    public defaultCA?: boolean,
    public active?: boolean,
    public selector?: string,
    public interval?: Interval,
    public plainSecret?: string,
    public secret?: IProtectedContent
  ) {
    this.defaultCA = this.defaultCA || false;
    this.active = this.active || false;
  }
}
