import { IPipeline } from '@/shared/model/pipeline.model';

export interface ITenant {
  id?: number;
  name?: string;
  longname?: string;
  active?: boolean | null;
  pipelines?: IPipeline[] | null;
}

export class Tenant implements ITenant {
  constructor(
    public id?: number,
    public name?: string,
    public longname?: string,
    public active?: boolean | null,
    public pipelines?: IPipeline[] | null
  ) {
    this.active = this.active ?? false;
  }
}
