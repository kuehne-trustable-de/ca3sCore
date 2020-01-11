import { IPipelineAttribute } from '@/shared/model/pipeline-attribute.model';
import { ICSR } from '@/shared/model/csr.model';

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
  pipelineAttributes?: IPipelineAttribute[];
  csr?: ICSR;
}

export class Pipeline implements IPipeline {
  constructor(
    public id?: number,
    public name?: string,
    public type?: PipelineType,
    public urlPart?: string,
    public pipelineAttributes?: IPipelineAttribute[],
    public csr?: ICSR
  ) {}
}
