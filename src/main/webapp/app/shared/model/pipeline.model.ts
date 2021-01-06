import { IPipelineAttribute } from '@/shared/model/pipeline-attribute.model';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import { IBPNMProcessInfo } from '@/shared/model/bpnm-process-info.model';

export const enum PipelineType {
  ACME = 'ACME',
  SCEP = 'SCEP',
  WEB = 'WEB',
  INTERNAL = 'INTERNAL',
}

export interface IPipeline {
  id?: number;
  name?: string;
  type?: PipelineType;
  urlPart?: string;
  description?: string;
  approvalRequired?: boolean;
  pipelineAttributes?: IPipelineAttribute[];
  caConnector?: ICAConnectorConfig;
  processInfo?: IBPNMProcessInfo;
}

export class Pipeline implements IPipeline {
  constructor(
    public id?: number,
    public name?: string,
    public type?: PipelineType,
    public urlPart?: string,
    public description?: string,
    public approvalRequired?: boolean,
    public pipelineAttributes?: IPipelineAttribute[],
    public caConnector?: ICAConnectorConfig,
    public processInfo?: IBPNMProcessInfo
  ) {
    this.approvalRequired = this.approvalRequired || false;
  }
}
