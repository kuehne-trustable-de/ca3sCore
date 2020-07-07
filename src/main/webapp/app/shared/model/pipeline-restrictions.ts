import { IPipelineRestriction, PipelineRestriction } from '@/shared/model/pipeline-restriction';

export interface IPipelineRestrictions {
  cn: IPipelineRestriction;
  c: IPipelineRestriction;
  o: IPipelineRestriction;
  ou: IPipelineRestriction;
  l: IPipelineRestriction;
  st: IPipelineRestriction;
  san: IPipelineRestriction;
}

export class PipelineRestrictions implements IPipelineRestrictions {

    public cn: PipelineRestriction;
    public c: PipelineRestriction;
    public o: PipelineRestriction;
    public ou: PipelineRestriction;
    public l: PipelineRestriction;
    public st: PipelineRestriction;
    public san: PipelineRestriction;

  constructor(
    cn?: PipelineRestriction,
    c?: PipelineRestriction,
    o?: PipelineRestriction,
    ou?: PipelineRestriction,
    l?: PipelineRestriction,
    st?: PipelineRestriction,
    san?: PipelineRestriction
  ) {
//    const dummyPR: PipelineRestriction = new PipelineRestriction('NOT_ALLOWED', '', false);
    this.cn = cn || new PipelineRestriction('NOT_ALLOWED', '', false);
    this.c = c || new PipelineRestriction('NOT_ALLOWED', '', false);
    this.o = o || new PipelineRestriction('NOT_ALLOWED', '', false);
    this.ou = ou || new PipelineRestriction('NOT_ALLOWED', '', false);
    this.l = l || new PipelineRestriction('NOT_ALLOWED', '', false);
    this.st = st || new PipelineRestriction('NOT_ALLOWED', '', false);
    this.san = san || new PipelineRestriction('NOT_ALLOWED', '', false);
  }
}
