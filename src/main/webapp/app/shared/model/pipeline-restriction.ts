
export interface IPipelineRestriction {
  name: string;
  cardinality: string;
  template: string;
  regex: boolean;
  readOnly: boolean;
  required: boolean;

  alignContent(): void;

}

export class PipelineRestriction implements IPipelineRestriction {
  public readOnly: boolean;
  public required: boolean;
  constructor(
    public name: string,
    public cardinality: string,
    public template: string,
    public regex: boolean
  ) {
    this.template = template || '';
    this.regex = regex || false;
    this.alignContent();
  }

  public alignContent(): void {

    window.console.info('alignContent ');

    this.readOnly = false;
    if (this.template.trim().length > 0 && !this.regex) {
      this.readOnly = true;
    }
    this.required = false;
    if (this.cardinality === 'ONE' || this.cardinality === 'ONE_OR_MANY' ) {
      this.required = true;
    }
  }
}
