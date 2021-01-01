import { IPipeline } from '@/shared/model/pipeline.model';

export interface IPipelineAttribute {
  id?: number;
  name?: string;
  value?: string;
  pipeline?: IPipeline;
}

export class PipelineAttribute implements IPipelineAttribute {
  constructor(public id?: number, public name?: string, public value?: string, public pipeline?: IPipeline) {}
}
