export interface IPipelineRestriction {
  name: string;
  cardinality: string;
  template: string;
  templateReadOnly: boolean,
  contentType: string,
  comment: string;
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

  constructor(
    public name: string,
    public cardinality: string,
    public template: string,
    public templateReadOnly: boolean,
    public contentType: string,
    public comment: string,
    public regExMatch: boolean,
    public regEx: string
  ) {
    this.template = template || '';
    this.templateReadOnly = this.templateReadOnly || false;
    this.contentType = contentType || '';
    this.readOnly = this.templateReadOnly || false;
    this.comment = comment || '';
    this.regEx = regEx || '';
    this.regExMatch = regExMatch || false;
    this.alignContent();
  }

  public alignContent(): void {
    window.console.info('alignContent ');

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
