/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2025-09-06 16:51:10.

export interface IADCSInstanceDetailsView extends ISerializable {
  caName?: string;
  caType?: string;
  templates?: string[];
}

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

export interface IAcmeAccountView extends ISerializable {
  id?: number;
  accountId?: number;
  eabKid?: string;
  eabUser?: IUser;
  realm?: string;
  createdOn?: Date;
  status?: IAccountStatus;
  termsOfServiceAgreed?: boolean;
  publicKeyHash?: string;
  publicKey?: string;
  keyLength?: string;
  keyAlgorithm?: string;
  altKeyAlgorithm?: string;
  contactUrls?: string[];
  orderCount?: number;
}

export interface IAcmeOrderView extends ISerializable {
  id?: number;
  orderId?: string;
  status?: IAcmeOrderStatus;
  realm?: string;
  challenges?: IAcmeChallengeView[];
  challengeTypes?: string;
  challengeUrls?: string;
  wildcard?: boolean;
  createdOn?: Date;
  expires?: Date;
  notBefore?: Date;
  notAfter?: Date;
  error?: string;
  finalizeUrl?: string;
  certificateUrl?: string;
  csrId?: number;
  certificateId?: number;
  accountId?: string;
}

export interface IAuthenticationParameter {
  kdfType?: IKDFType;
  plainSecret?: string;
  secretValidTo?: Date;
  salt?: string;
  cycles?: number;
  apiKeySalt?: string;
  apiKeyCycles?: number;
}

export interface IScepOrderView extends ISerializable {
  id?: number;
  transId?: string;
  status?: IScepOrderStatus;
  realm?: string;
  pipelineName?: string;
  subject?: string;
  sans?: string;
  sanArr?: string[];
  requestedOn?: Date;
  requestedBy?: string;
  asyncProcessing?: boolean;
  passwordAuthentication?: boolean;
  csrId?: number;
  certificateId?: number;
}

export interface IBPMNUpload {
  id?: number;
  name?: string;
  type?: IBPMNProcessType;
  version?: string;
  contentXML?: string;
  bpmnProcessAttributes?: IBPMNProcessAttribute[];
}

export interface ICaConnectorConfigView extends ISerializable {
  id?: number;
  name?: string;
  caConnectorType?: ICAConnectorType;
  caUrl?: string;
  sni?: string;
  disableHostNameVerifier?: boolean;
  msgContentType?: string;
  pollingOffset?: number;
  lastUpdate?: Date;
  defaultCA?: boolean;
  active?: boolean;
  trustSelfsignedCertificates?: boolean;
  selector?: string;
  role?: string;
  interval?: IInterval;
  messageProtectionPassphrase?: boolean;
  tlsAuthenticationId?: number;
  messageProtectionId?: number;
  ignoreResponseMessageVerification?: boolean;
  issuerName?: string;
  aTaVArr?: INamedValue[];
  multipleMessages?: boolean;
  implicitConfirm?: boolean;
  fillEmptySubjectWithSAN?: boolean;
  expiryDate?: Date;
  authenticationParameter?: IAuthenticationParameter;
}

export interface IPipelineView extends ISerializable {
  id?: number;
  name?: string;
  type?: IPipelineType;
  urlPart?: string;
  description?: string;
  listOrder?: number;
  approvalRequired?: boolean;
  active?: boolean;
  expiryDate?: Date;
  caConnectorId?: number;
  caConnectorName?: string;
  processInfoNameCreate?: string;
  processInfoNameRevoke?: string;
  processInfoNameNotify?: string;
  keyUniqueness?: IKeyUniqueness;
  restriction_C?: IRDNRestriction;
  restriction_CN?: IRDNRestriction;
  restriction_L?: IRDNRestriction;
  restriction_O?: IRDNRestriction;
  restriction_OU?: IRDNRestriction;
  restriction_S?: IRDNRestriction;
  restriction_E?: IRDNRestriction;
  restriction_SAN?: IRDNRestriction;
  rdnRestrictions?: IRDNRestriction[];
  araRestrictions?: IARARestriction[];
  tosAgreementRequired?: boolean;
  tosAgreementLink?: string;
  website?: string;
  caaIdentitiyList?: string[];
  selectedCaaIdentitiyList?: string[];
  eabMode?: string;
  domainRaOfficerList?: string[];
  allTenantList?: ITenant[];
  selectedTenantList?: ITenant[];
  toPendingOnFailedRestrictions?: boolean;
  ipAsSubjectAllowed?: boolean;
  ipAsSANAllowed?: boolean;
  acmeConfigItems?: IAcmeConfigItems;
  scepConfigItems?: ISCEPConfigItems;
  webConfigItems?: IWebConfigItems;
  auditViewArr?: IAuditView[];
  networkAcceptArr?: string[];
  networkRejectArr?: string[];
  csrUsage?: ICsrUsage;
  requestProxyConfigIds?: number[];
}

export interface ICertificateView extends ISerializable {
  id?: number;
  csrId?: number;
  issuerId?: number;
  rootId?: number;
  tbsDigest?: string;
  subject?: string;
  rdn_c?: string;
  rdn_cn?: string;
  issuer_rdn_cn?: string;
  root_rdn_cn?: string;
  rdn_o?: string;
  rdn_ou?: string;
  rdn_s?: string;
  rdn_l?: string;
  sans?: string;
  issuer?: string;
  root?: string;
  trusted?: boolean;
  fingerprintSha1?: string;
  fingerprintSha256?: string;
  ski?: string;
  type?: string;
  keyLength?: string;
  keyAlgorithm?: string;
  altKeyAlgorithm?: string;
  signingAlgorithm?: string;
  paddingAlgorithm?: string;
  hashAlgorithm?: string;
  description?: string;
  comment?: string;
  csrComment?: string;
  serial?: string;
  serialHex?: string;
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
  usageString?: string;
  extUsage?: string[];
  extUsageString?: string;
  sanArr?: string[];
  sansString?: string;
  typedSansString?: string;
  caConnectorId?: number;
  caProcessingId?: number;
  processingCa?: string;
  acmeAccountId?: string;
  acmeOrderId?: string;
  scepTransId?: string;
  scepRecipient?: string;
  fileSource?: string;
  uploadedBy?: string;
  revokedBy?: string;
  requestedBy?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  tenantName?: string;
  tenantId?: number;
  crlUrl?: string;
  crlExpirationNotificationId?: number;
  crlNextUpdate?: Date;
  certB64?: string;
  downloadFilename?: string;
  isServersideKeyGeneration?: boolean;
  serversideKeyValidTo?: Date;
  serversideKeyLeftUsages?: number;
  replacedCertArr?: string[];
  arArr?: INamedValue[];
  serversideKeyGeneration?: boolean;
  fullChainAvailable?: boolean;
  issuingActiveCertificates?: boolean;
  auditPresent?: boolean;
}

export interface ICryptoConfigView extends ISerializable {
  validPBEAlgoArr?: string[];
  defaultPBEAlgo?: string;
  allHashAlgoArr?: string[];
  allSignAlgoArr?: string[];
  pkcs12SecretRegexp?: string;
  regexpPkcs12SecretDescription?: string;
  regexpPasswordDescription?: string;
  passwordRegexp?: string;
  clientAuthTarget?: string;
}

export interface IUIConfigView extends ISerializable {
  appName?: string;
  cryptoConfigView?: ICryptoConfigView;
  autoSSOLogin?: boolean;
  ssoProvider?: string[];
  samlEntityBaseUrl?: string;
  scndFactorTypes?: IAuthSecondFactor[];
  extUsageArr?: string[];
  infoMsg?: string;
}

export interface IRequestProxyConfigView extends ISerializable {
  id?: number;
  name?: string;
  requestProxyUrl?: string;
  active?: boolean;
  plainSecret?: string;
}

export interface IUserLoginData {
  login?: string;
  password?: string;
  rememberMe?: boolean;
}

export interface ICSRView extends ISerializable {
  id?: number;
  certificateId?: number;
  status?: ICsrStatus;
  subject?: string;
  sans?: string;
  sanArr?: string[];
  pipelineType?: IPipelineType;
  rejectedOn?: Date;
  rejectionReason?: string;
  requestedBy?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  tenantName?: string;
  processingCA?: string;
  pipelineName?: string;
  pipelineId?: number;
  x509KeySpec?: string;
  hashAlgorithm?: string;
  keyAlgorithm?: string;
  keyLength?: string;
  signingAlgorithm?: string;
  publicKeyAlgorithm?: string;
  requestedOn?: Date;
  serversideKeyGeneration?: boolean;
  processInstanceId?: string;
  publicKeyHash?: string;
  administeredBy?: string;
  acceptedBy?: string;
  approvedOn?: Date;
  comment?: string;
  arArr?: INamedValue[];
  csrBase64?: string;
  auditViewArr?: IAuditView[];
  isAdministrable?: boolean;
  tosAgreed?: boolean;
  tosAgreementLink?: string;
  administrable?: boolean;
  csrvalid?: boolean;
}

export interface IPreferences extends ISerializable {
  serverSideKeyCreationAllowed?: boolean;
  deleteKeyAfterDays?: number;
  deleteKeyAfterUses?: number;
  checkCRL?: boolean;
  notifyRAOnRequest?: boolean;
  maxNextUpdatePeriodCRLHour?: number;
  acmeHTTP01TimeoutMilliSec?: number;
  acmeHTTP01CallbackPortArr?: number[];
  availableHashes?: string[];
  availableSigningAlgos?: string[];
  selectedHashes?: string[];
  selectedSigningAlgos?: string[];
  authClientCert?: boolean;
  authTotp?: boolean;
  authEmail?: boolean;
  sms?: boolean;
  authClientCertEnabled?: boolean;
  smsEnabled?: boolean;
  infoMsg?: string;
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
  pemCertificate?: string;
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
  warnings?: string[];
  badKeysResult?: IBadKeysResult;
  replacementCandidates?: ICertificateNameId[];
  replacementCandidatesFromList?: ICertificate[];
}

export interface ICsrReqAttribute extends ISerializable {
  oid?: string;
  oidName?: string;
  value?: string;
}

export interface IAccountCredentialView extends ISerializable {
  id?: number;
  createdOn?: Date;
  validTo?: Date;
  leftUsages?: number;
  relationType?: IAccountCredentialsType;
  pipelineName?: string;
  status?: IProtectedContentStatus;
}

export interface IPasswordChangeDTO {
  credentialUpdateType?: ICredentialUpdateType;
  currentPassword?: string;
  newPassword?: string;
  seed?: string;
  otpTestValue?: string;
  apiTokenValue?: string;
  apiTokenValiditySeconds?: number;
  clientAuthCertId?: number;
  pipelineId?: number;
  eabKid?: string;
  secondFactorRequired?: boolean;
}

export interface ICSRAdministrationData extends ISerializable {
  csrId?: number;
  administrationType?: IAdministrationType;
  rejectionReason?: string;
  comment?: string;
  arAttributes?: INamedValue[];
}

export interface ICSRAdministrationResponse extends ISerializable {
  csrId?: number;
  certId?: number;
  administrationType?: IAdministrationType;
  problemOccured?: string;
}

export interface ICertificateAdministrationData extends ISerializable {
  certificateId?: number;
  revocationReason?: string;
  comment?: string;
  administrationType?: IAdministrationType;
  trusted?: boolean;
  arAttributes?: INamedValue[];
}

export interface IUploadPrecheckData {
  passphrase?: string;
  importKey?: boolean;
  secret?: string;
  requestorcomment?: string;
  pipelineId?: number;
  content?: string;
  creationMode?: ICreationMode;
  keyAlgoLength?: string;
  containerType?: IContainerType;
  namedValues?: INamedValue[];
  certificateAttributes?: INamedValues[];
  arAttributes?: INamedValues[];
  tosAgreed?: boolean;
}

export interface ICertificateFilter extends ISerializable {
  attributeName?: string;
  attributeValue?: string;
  attributeValueArr?: string[];
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

export interface IDataCollection {
  labels?: string[];
  datasets?: IDataSet[];
}

export interface IDataSet {
  label?: string;
  data?: number[];
  backgroundColor?: string[];
}

export interface IBpmnCheckResult extends ISerializable {
  active?: boolean;
  failureReason?: string;
  isActive?: boolean;
  status?: string;
  csrAttributes?: { [index: string]: any }[];
  responseAttributes?: { [index: string]: any }[];
}

export interface IAcmeAccountStatusAdministration extends ISerializable {
  status?: IAccountStatus;
  comment?: string;
}

export interface ILoginData {
  username?: string;
  password?: string;
  rememberMe?: boolean;
  secondSecret?: string;
  authSecondFactor?: IAuthSecondFactor;
}

export interface ITokenRequest {
  credentialType?: IAccountCredentialsType;
  validitySeconds?: number;
}

export interface ISerializable {}

export interface IURI extends IComparable<IURI>, ISerializable {}

export interface IUser extends IAbstractAuditingEntity<any>, ISerializable {
  id?: number;
  login?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  secondFactorRequired?: boolean;
  activated?: boolean;
  langKey?: string;
  imageUrl?: string;
  resetDate?: Date;
  failedLogins?: number;
  lastloginDate?: Date;
  blockedUntilDate?: Date;
  credentialsValidToDate?: Date;
  managedExternally?: boolean;
  lastUserDetailsUpdate?: Date;
  tenant?: ITenant;
}

export interface IAcmeChallengeView extends ISerializable {
  authorizationType?: string;
  authorizationValue?: string;
  challengeId?: number;
  type?: string;
  value?: string;
  validated?: Date;
  status?: IChallengeStatus;
  lastError?: string;
}

export interface IBPMNProcessAttribute extends ISerializable {
  id?: number;
  name?: string;
  protectedContent?: boolean;
  bpmnProcessInfo?: IBPMNProcessInfo;
  value?: string;
}

export interface INamedValue {
  name?: string;
  value?: string;
}

export interface IRDNRestriction {
  rdnName?: string;
  cardinalityRestriction?: IRDNCardinalityRestriction;
  contentTemplate?: string;
  regEx?: string;
  regExMatch?: boolean;
}

export interface IARARestriction {
  name?: string;
  contentTemplate?: string;
  regEx?: string;
  comment?: string;
  contentType?: IARAContentType;
  regExMatch?: boolean;
  required?: boolean;
}

export interface ITenant extends ISerializable {
  id?: number;
  name?: string;
  longname?: string;
  active?: boolean;
  pipelines?: IPipeline[];
}

export interface IAcmeConfigItems extends ISerializable {
  allowChallengeHTTP01?: boolean;
  allowChallengeAlpn?: boolean;
  allowChallengeDNS?: boolean;
  allowWildcards?: boolean;
  checkCAA?: boolean;
  notifyContactsOnError?: boolean;
  contactEMailRegEx?: string;
  contactEMailRejectRegEx?: string;
  caNameCAA?: string;
  processInfoNameAccountAuthorization?: string;
  processInfoNameOrderValidation?: string;
  processInfoNameChallengeValidation?: string;
  externalAccountRequired?: boolean;
}

export interface ISCEPConfigItems extends ISerializable {
  capabilityRenewal?: boolean;
  capabilityPostPKIOperation?: boolean;
  periodDaysRenewal?: number;
  percentageOfValidtyBeforeRenewal?: number;
  recepientCertSubject?: string;
  recepientCertSerial?: string;
  recepientCertId?: number;
  scepSecretPCId?: string;
  scepSecret?: string;
  scepSecretValidTo?: Date;
  keyAlgoLength?: IKeyAlgoLengthOrSpec;
  scepRecipientDN?: string;
  caConnectorRecipientName?: string;
}

export interface IWebConfigItems extends ISerializable {
  additionalEMailRecipients?: string;
  notifyRAOfficerOnPendingRequest?: boolean;
  notifyDomainRAOfficerOnPendingRequest?: boolean;
  processInfoNameRequestAuthorization?: string;
  issuesSecondFactorClientCert?: boolean;
}

export interface INamedValues {
  name?: string;
  values?: ITypedValue[];
}

export interface IPkcs10RequestHolderShallow {
  csrvalid?: boolean;
  signingAlgorithmName?: string;
  isCSRValid?: boolean;
  x509KeySpec?: string;
  hashAlgName?: string;
  sigAlgName?: string;
  keyAlgName?: string;
  paddingAlgName?: string;
  mfgName?: string;
  keyLength?: number;
  sans?: string[];
  subject?: string;
  csrExtensionRequests?: ICsrReqAttribute[];
  publicKeyAlgorithmName?: string;
}

export interface IBadKeysResult extends ISerializable {
  valid?: boolean;
  installationValid?: boolean;
  messsage?: string;
  response?: IBadKeysResultResponse;
}

export interface ICertificateNameId extends ISerializable {
  id?: number;
  name?: string;
}

export interface ICertificate extends ISerializable {
  id?: number;
  tbsDigest?: string;
  subject?: string;
  sans?: string;
  issuer?: string;
  root?: string;
  type?: string;
  description?: string;
  fingerprint?: string;
  serial?: string;
  validFrom?: Date;
  validTo?: Date;
  keyAlgorithm?: string;
  keyLength?: number;
  curveName?: string;
  hashingAlgorithm?: string;
  paddingAlgorithm?: string;
  signingAlgorithm?: string;
  creationExecutionId?: string;
  contentAddedAt?: Date;
  revokedSince?: Date;
  revocationReason?: string;
  revoked?: boolean;
  revocationExecutionId?: string;
  administrationComment?: string;
  endEntity?: boolean;
  selfsigned?: boolean;
  trusted?: boolean;
  active?: boolean;
  content?: string;
  tenant?: ITenant;
  csr?: ICSR;
  comment?: ICertificateComment;
  certificateAttributes?: ICertificateAttribute[];
  issuingCertificate?: ICertificate;
  rootCertificate?: ICertificate;
  revocationCA?: ICAConnectorConfig;
  serialAsHex?: string;
}

export interface IAbstractAuditingEntity<T> extends ISerializable {
  id?: T;
}

export interface IBPMNProcessInfo extends ISerializable {
  id?: number;
  name?: string;
  version?: string;
  type?: IBPMNProcessType;
  author?: string;
  lastChange?: Date;
  signatureBase64?: string;
  bpmnHashBase64?: string;
  processId?: string;
  bpmnProcessAttributes?: IBPMNProcessAttribute[];
}

export interface IPipeline extends ISerializable {
  id?: number;
  name?: string;
  type?: IPipelineType;
  urlPart?: string;
  active?: boolean;
  description?: string;
  approvalRequired?: boolean;
  pipelineAttributes?: IPipelineAttribute[];
  caConnector?: ICAConnectorConfig;
  processInfoCreate?: IBPMNProcessInfo;
  processInfoRevoke?: IBPMNProcessInfo;
  processInfoNotify?: IBPMNProcessInfo;
  processInfoRequestAuthorization?: IBPMNProcessInfo;
  processInfoAccountAuthorization?: IBPMNProcessInfo;
  algorithms?: IAlgorithmRestriction[];
  requestProxies?: IRequestProxyConfig[];
  tenants?: ITenant[];
}

export interface IKeyAlgoLengthOrSpec {
  algoName?: string;
  contentBuilderName?: string;
  providerName?: string;
  algoGroup?: string;
  keyLength?: number;
  algorithmParameterSpec?: IAlgorithmParameterSpec;
}

export interface ITypedValue {
  type?: string;
  value?: string;
}

export interface IBadKeysResultResponse extends ISerializable {
  type?: string;
  n?: number;
  e?: number;
  x?: number;
  y?: number;
  bits?: number;
  spkisha256?: string;
  results?: IBadKeysResultDetails;
}

export interface ICSR extends ISerializable {
  id?: number;
  csrBase64?: string;
  subject?: string;
  sans?: string;
  requestedOn?: Date;
  requestedBy?: string;
  acceptedBy?: string;
  pipelineType?: IPipelineType;
  status?: ICsrStatus;
  administeredBy?: string;
  approvedOn?: Date;
  rejectedOn?: Date;
  rejectionReason?: string;
  processInstanceId?: string;
  signingAlgorithm?: string;
  isCSRValid?: boolean;
  x509KeySpec?: string;
  publicKeyAlgorithm?: string;
  keyAlgorithm?: string;
  keyLength?: number;
  publicKeyHash?: string;
  serversideKeyGeneration?: boolean;
  subjectPublicKeyInfoBase64?: string;
  requestorComment?: string;
  administrationComment?: string;
  comment?: ICSRComment;
  rdns?: IRDN[];
  ras?: IRequestAttribute[];
  csrAttributes?: ICsrAttribute[];
  tenant?: ITenant;
  pipeline?: IPipeline;
}

export interface ICertificateComment extends ISerializable {
  id?: number;
  comment?: string;
  certificate?: ICertificate;
}

export interface ICertificateAttribute extends ISerializable {
  id?: number;
  name?: string;
  value?: string;
  certificate?: ICertificate;
}

export interface ICAConnectorConfig extends ISerializable {
  id?: number;
  name?: string;
  caConnectorType?: ICAConnectorType;
  caUrl?: string;
  pollingOffset?: number;
  lastUpdate?: Date;
  defaultCA?: boolean;
  active?: boolean;
  trustSelfsignedCertificates?: boolean;
  selector?: string;
  interval?: IInterval;
  plainSecret?: string;
  checkActive?: boolean;
  caConnectorAttributes?: ICAConnectorConfigAttribute[];
  tlsAuthentication?: ICertificate;
  messageProtection?: ICertificate;
  expiryDate?: Date;
}

export interface IComparable<T> {}

export interface IPipelineAttribute extends ISerializable {
  id?: number;
  name?: string;
  value?: string;
  pipeline?: IPipeline;
}

export interface IAlgorithmRestriction extends ISerializable {
  id?: number;
  type?: IAlgorithmType;
  notAfter?: Date;
  identifier?: string;
  name?: string;
  acceptable?: boolean;
  pipelines?: IPipeline[];
}

export interface IRequestProxyConfig extends ISerializable {
  id?: number;
  name?: string;
  requestProxyUrl?: string;
  active?: boolean;
  pipelines?: IPipeline[];
}

export interface IAlgorithmParameterSpec {}

export interface IBadKeysResultDetails extends ISerializable {
  blocklist?: IBadKeysBlocklist;
  rsaInvalid?: IBadKeysResultInvalid;
  roca?: IBadKeysResultInvalid;
  pattern?: IBadKeysResultInvalid;
  fermat?: IBadKeysResultFermat;
  resultType?: string;
}

export interface ICSRComment extends ISerializable {
  id?: number;
  comment?: string;
  csr?: ICSR;
}

export interface IRDN extends ISerializable {
  id?: number;
  rdnAttributes?: IRDNAttribute[];
  csr?: ICSR;
}

export interface IRequestAttribute extends ISerializable {
  id?: number;
  attributeType?: string;
  requestAttributeValues?: IRequestAttributeValue[];
  holdingRequestAttribute?: IRequestAttributeValue;
  csr?: ICSR;
}

export interface ICsrAttribute extends ISerializable {
  id?: number;
  name?: string;
  value?: string;
  csr?: ICSR;
}

export interface ICAConnectorConfigAttribute extends ISerializable {
  id?: number;
  name?: string;
  caConnector?: ICAConnectorConfig;
  value?: string;
}

export interface IBadKeysBlocklist extends IBadKeysResultInvalid {
  blid?: number;
  lookup?: string;
  debug?: string;
}

export interface IBadKeysResultInvalid extends ISerializable {
  detected?: boolean;
  subtest?: string;
}

export interface IBadKeysResultFermat extends ISerializable {
  p?: number;
  q?: number;
  a?: number;
  b?: number;
  debug?: string;
}

export interface IRDNAttribute extends ISerializable {
  id?: number;
  attributeType?: string;
  attributeValue?: string;
  rdn?: IRDN;
}

export interface IRequestAttributeValue extends ISerializable {
  id?: number;
  attributeValue?: string;
  reqAttr?: IRequestAttribute;
}

export type ICAStatus = 'Active' | 'Deactivated' | 'Problem' | 'Unknown';

export type ISelector =
  | 'EQUAL'
  | 'NOT_EQUAL'
  | 'LIKE'
  | 'NOTLIKE'
  | 'LESSTHAN'
  | 'GREATERTHAN'
  | 'HEX'
  | 'DECIMAL'
  | 'ON'
  | 'BEFORE'
  | 'AFTER'
  | 'ISTRUE'
  | 'ISFALSE'
  | 'IN'
  | 'NOT_IN'
  | 'PERIOD_BEFORE'
  | 'PERIOD_AFTER';

export type IAccountStatus = 'valid' | 'pending' | 'deactivated' | 'revoked';

export type IAcmeOrderStatus = 'pending' | 'ready' | 'processing' | 'valid' | 'invalid';

export type IKDFType = 'PBKDF2';

export type IScepOrderStatus = 'PENDING' | 'READY' | 'INVALID';

export type IBPMNProcessType =
  | 'CA_INVOCATION'
  | 'CERTIFICATE_CREATION'
  | 'CERTIFICATE_REVOCATION'
  | 'CERTIFICATE_NOTIFY'
  | 'REQUEST_AUTHORIZATION'
  | 'ACME_ACCOUNT_AUTHORIZATION'
  | 'SEND_SMS'
  | 'TIMED';

export type ICAConnectorType =
  | 'INTERNAL'
  | 'CMP'
  | 'ADCS'
  | 'ADCS_CERTIFICATE_INVENTORY'
  | 'DIRECTORY'
  | 'VAULT'
  | 'VAULT_INVENTORY'
  | 'EJBCA_INVENTORY';

export type IInterval = 'MINUTE' | 'HOUR' | 'DAY' | 'WEEK' | 'MONTH';

export type IPipelineType = 'ACME' | 'SCEP' | 'EST' | 'WEB' | 'INTERNAL' | 'MANUAL_REQUEST';

export type IKeyUniqueness = 'KEY_UNIQUE' | 'DOMAIN_REUSE' | 'KEY_REUSE' | 'KEY_UNIQUE_WARN_ONLY' | 'DOMAIN_REUSE_WARN_ONLY';

export type ICsrUsage = 'TLS_SERVER' | 'TLS_CLIENT' | 'DOC_SIGNING' | 'CODE_SIGNING';

export type IAuthSecondFactor = 'NONE' | 'CLIENT_CERT' | 'TOTP' | 'EMAIL' | 'SMS';

export type ICsrStatus = 'PROCESSING' | 'ISSUED' | 'REJECTED' | 'PENDING';

export type IPKCSDataType =
  | 'CSR'
  | 'X509_CERTIFICATE'
  | 'X509_CERTIFICATE_CREATED'
  | 'UNKNOWN'
  | 'CONTAINER'
  | 'CONTAINER_WITH_KEY'
  | 'CONTAINER_REQUIRING_PASSPHRASE';

export type IAccountCredentialsType =
  | 'ACCOUNT_TOKEN'
  | 'OTP_SECRET'
  | 'CLIENT_CERTIFICATE'
  | 'API_TOKEN'
  | 'SCEP_TOKEN'
  | 'EST_TOKEN'
  | 'EAB_PASSWORD'
  | 'SMS_ENABLED';

export type IProtectedContentStatus = 'PENDING' | 'ACTIVE' | 'DEACTIVATED';

export type ICredentialUpdateType =
  | 'PASSWORD'
  | 'CLIENT_CERT'
  | 'TOTP'
  | 'TOKEN'
  | 'EMAIL'
  | 'SMS'
  | 'API_TOKEN'
  | 'SCEP_TOKEN'
  | 'EST_TOKEN'
  | 'EAB_PASSWORD';

export type IAdministrationType = 'ACCEPT' | 'REJECT' | 'REVOKE' | 'UPDATE' | 'UPDATE_CRL';

export type ICreationMode = 'CSR_AVAILABLE' | 'COMMANDLINE_TOOL' | 'SERVERSIDE_KEY_CREATION';

export type IContainerType = 'PKCS_12' | 'JKS';

export type IChallengeStatus = 'pending' | 'valid' | 'invalid' | 'deactivated' | 'expired' | 'revoked';

export type IRDNCardinalityRestriction = 'NOT_ALLOWED' | 'ZERO_OR_ONE' | 'ONE' | 'ONE_OR_SAN' | 'ZERO_OR_MANY' | 'ONE_OR_MANY';

export type IARAContentType = 'NO_TYPE' | 'EMAIL_ADDRESS';

export type IAlgorithmType = 'SIGNING' | 'PADDING' | 'HASH' | 'CURVE';
