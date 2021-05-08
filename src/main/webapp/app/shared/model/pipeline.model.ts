import { IPipelineAttribute } from '@/shared/model/pipeline-attribute.model';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import { IBPMNProcessInfo } from '@/shared/model/bpmn-process-info.model';

export const enum PipelineType {
  ACME = 'ACME',
  SCEP = 'SCEP',
  WEB = 'WEB'
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
  processInfo?: IBPMNProcessInfo;
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
    public processInfo?: IBPMNProcessInfo
  ) {
    this.approvalRequired = this.approvalRequired || false;
  }
}
