/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.19.577 on 2021-06-04 20:09:37.

export interface ICAConnectorStatus extends ISerializable {
  connectorId?: number;
  name?: string;
  status?: ICAStatus;
}

export interface IProblemDetail {
  type?: IURI;
  instance?: IURI;
  title?: string;
  detail?: string;
  status?: number;
}

export interface IAuditView extends ISerializable {
  id?: number;
  actorName?: string;
  actorRole?: string;
  plainContent?: string;
  contentParts?: string[];
  contentTemplate?: string;
  createdOn?: Date;
}

export interface IAuditTraceView extends ISerializable {
  id?: number;
  actorName?: string;
  actorRole?: string;
  plainContent?: string;
  contentTemplate?: string;
  createdOn?: Date;
  csrId?: number;
  certificateId?: number;
  pipelineId?: number;
  caConnectorId?: number;
  processInfoId?: number;
}

export interface IBPMNUpload {
  name?: string;
  type?: IBPMNProcessType;
  contentXML?: string;
}

export interface IPipelineView extends ISerializable {
  id?: number;
  name?: string;
  type?: IPipelineType;
  urlPart?: string;
  description?: string;
  approvalRequired?: boolean;
  active?: boolean;
  caConnectorName?: string;
  processInfoName?: string;
  restriction_C?: IRDNRestriction;
  restriction_CN?: IRDNRestriction;
  restriction_L?: IRDNRestriction;
  restriction_O?: IRDNRestriction;
  restriction_OU?: IRDNRestriction;
  restriction_S?: IRDNRestriction;
  restriction_SAN?: IRDNRestriction;
  rdnRestrictions?: IRDNRestriction[];
  araRestrictions?: IARARestriction[];
  toPendingOnFailedRestrictions?: boolean;
  ipAsSubjectAllowed?: boolean;
  ipAsSANAllowed?: boolean;
  acmeConfigItems?: IACMEConfigItems;
  scepConfigItems?: ISCEPConfigItems;
  webConfigItems?: IWebConfigItems;
  auditViewArr?: IAuditView[];
  csrUsage?: ICsrUsage;
}

export interface ICertificateView extends ISerializable {
  id?: number;
  csrId?: number;
  issuerId?: number;
  tbsDigest?: string;
  subject?: string;
  rdn_c?: string;
  rdn_cn?: string;
  rdn_o?: string;
  rdn_ou?: string;
  rdn_s?: string;
  rdn_l?: string;
  sans?: string;
  issuer?: string;
  root?: string;
  fingerprintSha1?: string;
  fingerprintSha256?: string;
  type?: string;
  keyLength?: string;
  keyAlgorithm?: string;
  signingAlgorithm?: string;
  paddingAlgorithm?: string;
  hashAlgorithm?: string;
  description?: string;
  comment?: string;
  csrComment?: string;
  serial?: string;
  validFrom?: Date;
  validTo?: Date;
  contentAddedAt?: Date;
  revokedSince?: Date;
  revocationReason?: string;
  revoked?: boolean;
  selfsigned?: boolean;
  ca?: boolean;
  intermediate?: boolean;
  endEntity?: boolean;
  chainLength?: number;
  usage?: string[];
  extUsage?: string[];
  sanArr?: string[];
  caConnectorId?: number;
  caProcessingId?: number;
  processingCa?: string;
  acmeAccountId?: number;
  acmeOrderId?: number;
  scepTransId?: number;
  scepRecipient?: string;
  fileSource?: string;
  uploadedBy?: string;
  revokedBy?: string;
  requestedBy?: string;
  crlUrl?: string;
  crlNextUpdate?: Date;
  certB64?: string;
  downloadFilename?: string;
  isServersideKeyGeneration?: boolean;
  arArr?: INamedValue[];
  auditPresent?: boolean;
}

export interface ICSRView extends ISerializable {
  id?: number;
  certificateId?: number;
  status?: ICsrStatus;
  subject?: string;
  sans?: string;
  pipelineType?: IPipelineType;
  rejectedOn?: Date;
  rejectionReason?: string;
  requestedBy?: string;
  processingCA?: string;
  pipelineName?: string;
  x509KeySpec?: string;
  keyLength?: string;
  signingAlgorithm?: string;
  publicKeyAlgorithm?: string;
  requestedOn?: Date;
  auditViewArr?: IAuditView[];
}

export interface IPreferences extends ISerializable {
  serverSideKeyCreationAllowed?: boolean;
  checkCRL?: boolean;
  acmeHTTP01TimeoutMilliSec?: number;
  acmeHTTP01CallbackPortArr?: number[];
}

export interface ICSRAdministrationData extends ISerializable {
  csrId?: number;
  administrationType?: IAdministrationType;
  rejectionReason?: string;
  comment?: string;
  arAttributes?: INamedValue[];
}

export interface ICertificateAdministrationData extends ISerializable {
  certificateId?: number;
  revocationReason?: string;
  comment?: string;
  administrationType?: IAdministrationType;
  arAttributes?: INamedValue[];
}

export interface IUploadPrecheckData {
  passphrase?: string;
  secret?: string;
  requestorcomment?: string;
  pipelineId?: number;
  content?: string;
  creationMode?: ICreationMode;
  keyAlgoLength?: IKeyAlgoLength;
  containerType?: IContainerType;
  namedValues?: INamedValue[];
  certificateAttributes?: INamedValues[];
  arAttributes?: INamedValues[];
}

export interface IX509CertificateHolderShallow {
  certificateId?: number;
  subject?: string;
  issuer?: string;
  type?: string;
  fingerprint?: string;
  serial?: string;
  validFrom?: Date;
  validTo?: Date;
  subjectParts?: INamedValues[];
  extensions?: string[];
  keyPresent?: boolean;
  certificatePresentInDB?: boolean;
  publicKeyPresentInDB?: boolean;
  pemCertrificate?: string;
  sans?: string[];
}

export interface ICertificateFilter extends ISerializable {
  attributeName?: string;
  attributeValue?: string;
  selector?: ISelector;
}

export interface ICertificateOrder extends ISerializable {
  orderBy?: string;
  orderDir?: string;
}

export interface ICertificateFilterList extends ISerializable {
  filterList?: ICertificateFilter[];
  orderList?: ICertificateOrder;
}

export interface ICertificateSelectionData extends ISerializable {
  itemName?: string;
  itemType?: string;
  itemDefaultSelector?: ISelector;
  itemDefaultValue?: string;
  values?: string[];
}

export interface IPkcsXXData {
  csrPublicKeyPresentInDB?: boolean;
  dataType?: IPKCSDataType;
  p10Holder?: IPkcs10RequestHolderShallow;
  certificates?: IX509CertificateHolderShallow[];
  createdCertificateId?: string;
  passphraseRequired?: boolean;
  csrPending?: boolean;
  createdCSRId?: string;
  messages?: string[];
  replacementCandidates?: ICertificateNameId[];
}

export interface IDataCollection {
  labels?: string[];
  datasets?: IDataSet[];
}

export interface IDataSet {
  label?: string;
  data?: number[];
  backgroundColor?: string[];
}

export interface ISerializable {}

export interface IURI extends IComparable<IURI>, ISerializable {}

export interface IRDNRestriction {
  rdnName?: string;
  cardinalityRestriction?: IRDNCardinalityRestriction;
  contentTemplate?: string;
  regExMatch?: boolean;
}

export interface IARARestriction {
  name?: string;
  contentTemplate?: string;
  regExMatch?: boolean;
  required?: boolean;
}

export interface IACMEConfigItems extends ISerializable {
  allowChallengeHTTP01?: boolean;
  allowChallengeDNS?: boolean;
  allowWildcards?: boolean;
  checkCAA?: boolean;
  caNameCAA?: string;
  processInfoNameAccountValidation?: string;
  processInfoNameOrderValidation?: string;
  processInfoNameChallengeValidation?: string;
}

export interface ISCEPConfigItems extends ISerializable {
  capabilityRenewal?: boolean;
  capabilityPostPKIOperation?: boolean;
  scepSecretPCId?: string;
  scepSecret?: string;
  scepSecretValidTo?: Date;
}

export interface IWebConfigItems extends ISerializable {}

export interface INamedValue {
  name?: string;
  value?: string;
}

export interface INamedValues {
  name?: string;
  values?: string[];
}

export interface IPkcs10RequestHolderShallow {
  csrvalid?: boolean;
  signingAlgorithmName?: string;
  isCSRValid?: boolean;
  x509KeySpec?: string;
  sans?: string[];
  subject?: string;
  publicKeyAlgorithmName?: string;
}

export interface ICertificateNameId extends ISerializable {
  id?: number;
  name?: string;
}

export interface IComparable<T> {}

export type ICAStatus = 'Active' | 'Deactivated' | 'Problem' | 'Unknown';

export type ISelector =
  | 'EQUAL'
  | 'NOT_EQUAL'
  | 'LIKE'
  | 'NOTLIKE'
  | 'LESSTHAN'
  | 'GREATERTHAN'
  | 'ON'
  | 'BEFORE'
  | 'AFTER'
  | 'ISTRUE'
  | 'ISFALSE';

export type IBPMNProcessType = 'CA_INVOCATION' | 'REQUEST_AUTHORIZATION';

export type IPipelineType = 'ACME' | 'SCEP' | 'WEB' | 'INTERNAL';

export type ICsrUsage = 'TLS_SERVER' | 'TLS_CLIENT' | 'DOC_SIGNING' | 'CODE_SIGNING';

export type ICsrStatus = 'PROCESSING' | 'ISSUED' | 'REJECTED' | 'PENDING';

export type IAdministrationType = 'ACCEPT' | 'REJECT' | 'REVOKE' | 'UPDATE';

export type ICreationMode = 'CSR_AVAILABLE' | 'COMMANDLINE_TOOL' | 'SERVERSIDE_KEY_CREATION';

export type IKeyAlgoLength = 'RSA_2048' | 'RSA_4096';

export type IContainerType = 'PKCS_12' | 'JKS';

export type IPKCSDataType =
  | 'CSR'
  | 'X509_CERTIFICATE'
  | 'X509_CERTIFICATE_CREATED'
  | 'UNKNOWN'
  | 'CONTAINER'
  | 'CONTAINER_REQUIRING_PASSPHRASE';

export type IRDNCardinalityRestriction = 'NOT_ALLOWED' | 'ZERO_OR_ONE' | 'ONE' | 'ZERO_OR_MANY' | 'ONE_OR_MANY';
