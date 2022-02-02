import { IPipelineRestriction, PipelineRestriction } from '@/shared/model/pipeline-restriction';

export interface IPipelineRestrictions {
  cn: IPipelineRestriction;
  c: IPipelineRestriction;
  o: IPipelineRestriction;
  ou: IPipelineRestriction;
  l: IPipelineRestriction;
  st: IPipelineRestriction;
  e: IPipelineRestriction;
  san: IPipelineRestriction;
}

export class PipelineRestrictions implements IPipelineRestrictions {
  public cn: PipelineRestriction;
  public c: PipelineRestriction;
  public o: PipelineRestriction;
  public ou: PipelineRestriction;
  public l: PipelineRestriction;
  public st: PipelineRestriction;
  public e: PipelineRestriction;
  public san: PipelineRestriction;

  constructor(
    cn?: PipelineRestriction,
    c?: PipelineRestriction,
    o?: PipelineRestriction,
    ou?: PipelineRestriction,
    l?: PipelineRestriction,
    st?: PipelineRestriction,
    e?: PipelineRestriction,
    san?: PipelineRestriction
  ) {
    //    const dummyPR: PipelineRestriction = new PipelineRestriction('NOT_ALLOWED', '', false);
    this.cn = cn || new PipelineRestriction('CN', 'NOT_ALLOWED', '', false, '');
    this.c = c || new PipelineRestriction('C', 'NOT_ALLOWED', '', false, '');
    this.o = o || new PipelineRestriction('O', 'NOT_ALLOWED', '', false, '');
    this.ou = ou || new PipelineRestriction('OU', 'NOT_ALLOWED', '', false, '');
    this.l = l || new PipelineRestriction('L', 'NOT_ALLOWED', '', false, '');
    this.st = st || new PipelineRestriction('ST', 'NOT_ALLOWED', '', false, '');
    this.e = e || new PipelineRestriction('ST', 'NOT_ALLOWED', '', false, '');
    this.san = san || new PipelineRestriction('SAN', 'NOT_ALLOWED', '', false, '');
  }
}
