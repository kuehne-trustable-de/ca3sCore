export interface IPipelineRestriction {
  name: string;
  cardinality: string;
  template: string;
  regExMatch: boolean;
  regEx: string;
  readOnly: boolean;
  required: boolean;
  multipleValues: boolean;

  alignContent(): void;
}

export class PipelineRestriction implements IPipelineRestriction {
  public readOnly: boolean;
  public required: boolean;
  public multipleValues: boolean;

  constructor(public name: string, public cardinality: string, public template: string, public regExMatch: boolean, public regEx: string) {
    this.template = template || '';
    this.regEx = regEx || '';
    this.regExMatch = regExMatch || false;
    this.alignContent();
  }

  public alignContent(): void {
    window.console.info('alignContent ');

    this.readOnly = false;
    if (this.template.trim().length > 0 && !this.regExMatch) {
      this.readOnly = true;
    }
    this.required = false;
    if (this.cardinality === 'ONE' || this.cardinality === 'ONE_OR_MANY') {
      this.required = true;
    }
    this.multipleValues = false;
    if (this.cardinality === 'ZERO_OR_MANY' || this.cardinality === 'ONE_OR_MANY') {
      this.multipleValues = true;
    }
  }
}
