import Vue from 'vue';
import Component from 'vue-class-component';
Component.registerHooks([
  'beforeRouteEnter',
  'beforeRouteLeave',
  'beforeRouteUpdate' // for vue-router 2.2+
]);
import Router from 'vue-router';
const Home = () => import('../core/home/home.vue');
const PkcsXX = () => import('../core/pkcsxx/pkcsxx.vue');
const CertList = () => import('../core/cert-list/cert-list.vue');
const CertInfo = () => import('../core/cert-list/cert-info.vue');
const CsrList = () => import('../core/csr-list/csr-list.vue');
const CsrInfo = () => import('../core/csr-list/csr-info.vue');
const Error = () => import('../core/error/error.vue');
const Register = () => import('../account/register/register.vue');
const Activate = () => import('../account/activate/activate.vue');
const ResetPasswordInit = () => import('../account/reset-password/init/reset-password-init.vue');
const ResetPasswordFinish = () => import('../account/reset-password/finish/reset-password-finish.vue');
const ChangePassword = () => import('../account/change-password/change-password.vue');
const Settings = () => import('../account/settings/settings.vue');
const JhiUserManagementComponent = () => import('../admin/user-management/user-management.vue');
const JhiUserManagementViewComponent = () => import('../admin/user-management/user-management-view.vue');
const JhiUserManagementEditComponent = () => import('../admin/user-management/user-management-edit.vue');
const JhiConfigurationComponent = () => import('../admin/configuration/configuration.vue');
const JhiDocsComponent = () => import('../admin/docs/docs.vue');
const JhiHealthComponent = () => import('../admin/health/health.vue');
const JhiLogsComponent = () => import('../admin/logs/logs.vue');
const JhiAuditsComponent = () => import('../admin/audits/audits.vue');
const JhiMetricsComponent = () => import('../admin/metrics/metrics.vue');
const JhiTrackerComponent = () => import('../admin/tracker/tracker.vue');
/* tslint:disable */

// prettier-ignore
const ConfPipeline = () => import('../core/pipeline/pipeline.vue');
// prettier-ignore
const ConfPipelineUpdate = () => import('../core/pipeline/pipeline-update.vue');
// prettier-ignore
const ConfPipelineDetails = () => import('../core/pipeline/pipeline-details.vue');

// prettier-ignore
const ConfCaConnector = () => import('../core/ca-connector-config/ca-connector-config.vue');
// prettier-ignore
const ConfCaConnectorUpdate = () => import('../core/ca-connector-config/ca-connector-config-update.vue');
// prettier-ignore
const ConfCaConnectorDetails = () => import('../core/ca-connector-config/ca-connector-config-details.vue');



// prettier-ignore
const CAConnectorConfig = () => import('../entities/ca-connector-config/ca-connector-config.vue');
// prettier-ignore
const CAConnectorConfigUpdate = () => import('../entities/ca-connector-config/ca-connector-config-update.vue');
// prettier-ignore
const CAConnectorConfigDetails = () => import('../entities/ca-connector-config/ca-connector-config-details.vue');
// prettier-ignore
const CertificateAttribute = () => import('../entities/certificate-attribute/certificate-attribute.vue');
// prettier-ignore
const CertificateAttributeUpdate = () => import('../entities/certificate-attribute/certificate-attribute-update.vue');
// prettier-ignore
const CertificateAttributeDetails = () => import('../entities/certificate-attribute/certificate-attribute-details.vue');
// prettier-ignore
const Certificate = () => import('../entities/certificate/certificate.vue');
// prettier-ignore
const CertificateUpdate = () => import('../entities/certificate/certificate-update.vue');
// prettier-ignore
const CertificateDetails = () => import('../entities/certificate/certificate-details.vue');
// prettier-ignore
const CSR = () => import('../entities/csr/csr.vue');
// prettier-ignore
const CSRUpdate = () => import('../entities/csr/csr-update.vue');
// prettier-ignore
const CSRDetails = () => import('../entities/csr/csr-details.vue');
// prettier-ignore
const CsrAttribute = () => import('../entities/csr-attribute/csr-attribute.vue');
// prettier-ignore
const CsrAttributeUpdate = () => import('../entities/csr-attribute/csr-attribute-update.vue');
// prettier-ignore
const CsrAttributeDetails = () => import('../entities/csr-attribute/csr-attribute-details.vue');
// prettier-ignore
const RDN = () => import('../entities/rdn/rdn.vue');
// prettier-ignore
const RDNUpdate = () => import('../entities/rdn/rdn-update.vue');
// prettier-ignore
const RDNDetails = () => import('../entities/rdn/rdn-details.vue');
// prettier-ignore
const RDNAttribute = () => import('../entities/rdn-attribute/rdn-attribute.vue');
// prettier-ignore
const RDNAttributeUpdate = () => import('../entities/rdn-attribute/rdn-attribute-update.vue');
// prettier-ignore
const RDNAttributeDetails = () => import('../entities/rdn-attribute/rdn-attribute-details.vue');
// prettier-ignore
const RequestAttribute = () => import('../entities/request-attribute/request-attribute.vue');
// prettier-ignore
const RequestAttributeUpdate = () => import('../entities/request-attribute/request-attribute-update.vue');
// prettier-ignore
const RequestAttributeDetails = () => import('../entities/request-attribute/request-attribute-details.vue');
// prettier-ignore
const RequestAttributeValue = () => import('../entities/request-attribute-value/request-attribute-value.vue');
// prettier-ignore
const RequestAttributeValueUpdate = () => import('../entities/request-attribute-value/request-attribute-value-update.vue');
// prettier-ignore
const RequestAttributeValueDetails = () => import('../entities/request-attribute-value/request-attribute-value-details.vue');
// prettier-ignore
const Pipeline = () => import('../entities/pipeline/pipeline.vue');
// prettier-ignore
const PipelineUpdate = () => import('../entities/pipeline/pipeline-update.vue');
// prettier-ignore
const PipelineDetails = () => import('../entities/pipeline/pipeline-details.vue');
// prettier-ignore
const PipelineAttribute = () => import('../entities/pipeline-attribute/pipeline-attribute.vue');
// prettier-ignore
const PipelineAttributeUpdate = () => import('../entities/pipeline-attribute/pipeline-attribute-update.vue');
// prettier-ignore
const PipelineAttributeDetails = () => import('../entities/pipeline-attribute/pipeline-attribute-details.vue');
// prettier-ignore
const ACMEAccount = () => import('../entities/acme-account/acme-account.vue');
// prettier-ignore
const ACMEAccountUpdate = () => import('../entities/acme-account/acme-account-update.vue');
// prettier-ignore
const ACMEAccountDetails = () => import('../entities/acme-account/acme-account-details.vue');
// prettier-ignore
const AcmeContact = () => import('../entities/acme-contact/acme-contact.vue');
// prettier-ignore
const AcmeContactUpdate = () => import('../entities/acme-contact/acme-contact-update.vue');
// prettier-ignore
const AcmeContactDetails = () => import('../entities/acme-contact/acme-contact-details.vue');
// prettier-ignore
const AcmeOrder = () => import('../entities/acme-order/acme-order.vue');
// prettier-ignore
const AcmeOrderUpdate = () => import('../entities/acme-order/acme-order-update.vue');
// prettier-ignore
const AcmeOrderDetails = () => import('../entities/acme-order/acme-order-details.vue');
// prettier-ignore
const Identifier = () => import('../entities/identifier/identifier.vue');
// prettier-ignore
const IdentifierUpdate = () => import('../entities/identifier/identifier-update.vue');
// prettier-ignore
const IdentifierDetails = () => import('../entities/identifier/identifier-details.vue');
// prettier-ignore
const Authorization = () => import('../entities/authorization/authorization.vue');
// prettier-ignore
const AuthorizationUpdate = () => import('../entities/authorization/authorization-update.vue');
// prettier-ignore
const AuthorizationDetails = () => import('../entities/authorization/authorization-details.vue');
// prettier-ignore
const AcmeChallenge = () => import('../entities/acme-challenge/acme-challenge.vue');
// prettier-ignore
const AcmeChallengeUpdate = () => import('../entities/acme-challenge/acme-challenge-update.vue');
// prettier-ignore
const AcmeChallengeDetails = () => import('../entities/acme-challenge/acme-challenge-details.vue');
// prettier-ignore
const Nonce = () => import('../entities/nonce/nonce.vue');
// prettier-ignore
const NonceUpdate = () => import('../entities/nonce/nonce-update.vue');
// prettier-ignore
const NonceDetails = () => import('../entities/nonce/nonce-details.vue');
// prettier-ignore
const ImportedURL = () => import('../entities/imported-url/imported-url.vue');
// prettier-ignore
const ImportedURLUpdate = () => import('../entities/imported-url/imported-url-update.vue');
// prettier-ignore
const ImportedURLDetails = () => import('../entities/imported-url/imported-url-details.vue');
// prettier-ignore
const ProtectedContent = () => import('../entities/protected-content/protected-content.vue');
// prettier-ignore
const ProtectedContentUpdate = () => import('../entities/protected-content/protected-content-update.vue');
// prettier-ignore
const ProtectedContentDetails = () => import('../entities/protected-content/protected-content-details.vue');
// prettier-ignore
const AcmeIdentifier = () => import('../entities/acme-identifier/acme-identifier.vue');
// prettier-ignore
const AcmeIdentifierUpdate = () => import('../entities/acme-identifier/acme-identifier-update.vue');
// prettier-ignore
const AcmeIdentifierDetails = () => import('../entities/acme-identifier/acme-identifier-details.vue');
// prettier-ignore
const AcmeAuthorization = () => import('../entities/acme-authorization/acme-authorization.vue');
// prettier-ignore
const AcmeAuthorizationUpdate = () => import('../entities/acme-authorization/acme-authorization-update.vue');
// prettier-ignore
const AcmeAuthorizationDetails = () => import('../entities/acme-authorization/acme-authorization-details.vue');
// prettier-ignore
const AcmeNonce = () => import('../entities/acme-nonce/acme-nonce.vue');
// prettier-ignore
const AcmeNonceUpdate = () => import('../entities/acme-nonce/acme-nonce-update.vue');
// prettier-ignore
const AcmeNonceDetails = () => import('../entities/acme-nonce/acme-nonce-details.vue');
// prettier-ignore
const BPNMProcessInfo = () => import('../entities/bpnm-process-info/bpnm-process-info.vue');
// prettier-ignore
const BPNMProcessInfoUpdate = () => import('../entities/bpnm-process-info/bpnm-process-info-update.vue');
// prettier-ignore
const BPNMProcessInfoDetails = () => import('../entities/bpnm-process-info/bpnm-process-info-details.vue');
// prettier-ignore
const RequestProxyConfig = () => import('../entities/request-proxy-config/request-proxy-config.vue');
// prettier-ignore
const RequestProxyConfigUpdate = () => import('../entities/request-proxy-config/request-proxy-config-update.vue');
// prettier-ignore
const RequestProxyConfigDetails = () => import('../entities/request-proxy-config/request-proxy-config-details.vue');
// prettier-ignore
const UserPreference = () => import('../entities/user-preference/user-preference.vue');
// prettier-ignore
const UserPreferenceUpdate = () => import('../entities/user-preference/user-preference-update.vue');
// prettier-ignore
const UserPreferenceDetails = () => import('../entities/user-preference/user-preference-details.vue');
// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

Vue.use(Router);

// prettier-ignore
export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/forbidden',
      name: 'Forbidden',
      component: Error,
      meta: { error403: true }
    },
    {
      path: '/not-found',
      name: 'NotFound',
      component: Error,
      meta: { error404: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/activate',
      name: 'Activate',
      component: Activate
    },
    {
      path: '/reset/request',
      name: 'ResetPasswordInit',
      component: ResetPasswordInit
    },
    {
      path: '/reset/finish',
      name: 'ResetPasswordFinish',
      component: ResetPasswordFinish
    },
    {
      path: '/pkcsxx',
      name: 'PkcsXX',
      component: PkcsXX
    },
    {
      path: '/cert-list',
      name: 'CertList',
      component: CertList
    },
    {
      path: '/cert-info',
      name: 'CertInfo',
      component: CertInfo
    },
    {
      path: '/csr-list',
      name: 'CsrList',
      component: CsrList,
      meta: { authorities: ['ROLE_USER', 'ROLE_RA', 'ROLE_ADMIN'] }
    },
    {
      path: '/csr-info',
      name: 'CsrInfo',
      component: CsrInfo,
      meta: { authorities: ['ROLE_USER', 'ROLE_RA', 'ROLE_ADMIN'] }
    },


    {
      path: '/csr-info',
      name: 'CsrInfo',
      component: CsrInfo,
      meta: { authorities: ['ROLE_USER', 'ROLE_RA', 'ROLE_ADMIN'] }
    },

    {
      path: '/confPipeline',
      name: 'ConfPipeline',
      component: ConfPipeline,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },
    {
      path: '/confPipeline/new',
      name: 'ConfPipelineCreate',
      component: ConfPipelineUpdate,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },
    {
      path: '/confPipeline/:pipelineId/:mode',
      name: 'ConfPipelineEdit',
      component: ConfPipelineUpdate,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },
    {
      path: '/confPipeline/:pipelineId/:mode',
      name: 'ConfPipelineCopy',
      component: ConfPipelineUpdate,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },
    {
      path: '/confPipeline/:pipelineId/view',
      name: 'ConfPipelineView',
      component: ConfPipelineDetails,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },

    {
      path: '/confCaConnector',
      name: 'ConfCaConnector',
      component: ConfCaConnector,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },
    {
      path: '/confCaConnector/new',
      name: 'ConfCaConnectorCreate',
      component: ConfCaConnectorUpdate,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },
    {
      path: '/confCaConnector/:cAConnectorConfigId/:mode',
      name: 'ConfCaConnectorEdit',
      component: ConfCaConnectorUpdate,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },
    {
      path: '/confCaConnector/:cAConnectorConfigId/:mode',
      name: 'ConfCaConnectorCopy',
      component: ConfCaConnectorUpdate,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },
    {
      path: '/confCaConnector/:caConnectorId/view',
      name: 'ConfCaConnectorView',
      component: ConfCaConnectorDetails,
      meta: { authorities: ['ROLE_ADMIN', 'ROLE_RA'] }
    },


    {
      path: '/account/password',
      name: 'ChangePassword',
      component: ChangePassword,
      meta: { authorities: ['ROLE_USER', 'ROLE_RA'] }
    },
    {
      path: '/account/settings',
      name: 'Settings',
      component: Settings,
      meta: { authorities: ['ROLE_USER', 'ROLE_RA'] }
    },
    {
      path: '/admin/user-management',
      name: 'JhiUser',
      component: JhiUserManagementComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/user-management/new',
      name: 'JhiUserCreate',
      component: JhiUserManagementEditComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/user-management/:userId/edit',
      name: 'JhiUserEdit',
      component: JhiUserManagementEditComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/user-management/:userId/view',
      name: 'JhiUserView',
      component: JhiUserManagementViewComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/docs',
      name: 'JhiDocsComponent',
      component: JhiDocsComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/audits',
      name: 'JhiAuditsComponent',
      component: JhiAuditsComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/jhi-health',
      name: 'JhiHealthComponent',
      component: JhiHealthComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/logs',
      name: 'JhiLogsComponent',
      component: JhiLogsComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/jhi-metrics',
      name: 'JhiMetricsComponent',
      component: JhiMetricsComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    },
    {
      path: '/admin/jhi-configuration',
      name: 'JhiConfigurationComponent',
      component: JhiConfigurationComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    }
,
    {
      path: '/admin/jhi-tracker',
      name: 'JhiTrackerComponent',
      component: JhiTrackerComponent,
      meta: { authorities: ['ROLE_ADMIN'] }
    }
    ,
    {
      path: '/ca-connector-config',
      name: 'CAConnectorConfig',
      component: CAConnectorConfig,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/ca-connector-config/new',
      name: 'CAConnectorConfigCreate',
      component: CAConnectorConfigUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/ca-connector-config/:cAConnectorConfigId/edit',
      name: 'CAConnectorConfigEdit',
      component: CAConnectorConfigUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/ca-connector-config/:cAConnectorConfigId/view',
      name: 'CAConnectorConfigView',
      component: CAConnectorConfigDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/certificate-attribute',
      name: 'CertificateAttribute',
      component: CertificateAttribute,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/certificate-attribute/new',
      name: 'CertificateAttributeCreate',
      component: CertificateAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/certificate-attribute/:certificateAttributeId/edit',
      name: 'CertificateAttributeEdit',
      component: CertificateAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/certificate-attribute/:certificateAttributeId/view',
      name: 'CertificateAttributeView',
      component: CertificateAttributeDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/certificate',
      name: 'Certificate',
      component: Certificate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/certificate/new',
      name: 'CertificateCreate',
      component: CertificateUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/certificate/:certificateId/edit',
      name: 'CertificateEdit',
      component: CertificateUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/certificate/:certificateId/view',
      name: 'CertificateView',
      component: CertificateDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/csr',
      name: 'CSR',
      component: CSR,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/csr/new',
      name: 'CSRCreate',
      component: CSRUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/csr/:cSRId/edit',
      name: 'CSREdit',
      component: CSRUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/csr/:cSRId/view',
      name: 'CSRView',
      component: CSRDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/csr-attribute',
      name: 'CsrAttribute',
      component: CsrAttribute,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/csr-attribute/new',
      name: 'CsrAttributeCreate',
      component: CsrAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/csr-attribute/:csrAttributeId/edit',
      name: 'CsrAttributeEdit',
      component: CsrAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/csr-attribute/:csrAttributeId/view',
      name: 'CsrAttributeView',
      component: CsrAttributeDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/rdn',
      name: 'RDN',
      component: RDN,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/rdn/new',
      name: 'RDNCreate',
      component: RDNUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/rdn/:rDNId/edit',
      name: 'RDNEdit',
      component: RDNUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/rdn/:rDNId/view',
      name: 'RDNView',
      component: RDNDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/rdn-attribute',
      name: 'RDNAttribute',
      component: RDNAttribute,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/rdn-attribute/new',
      name: 'RDNAttributeCreate',
      component: RDNAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/rdn-attribute/:rDNAttributeId/edit',
      name: 'RDNAttributeEdit',
      component: RDNAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/rdn-attribute/:rDNAttributeId/view',
      name: 'RDNAttributeView',
      component: RDNAttributeDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/request-attribute',
      name: 'RequestAttribute',
      component: RequestAttribute,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-attribute/new',
      name: 'RequestAttributeCreate',
      component: RequestAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-attribute/:requestAttributeId/edit',
      name: 'RequestAttributeEdit',
      component: RequestAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-attribute/:requestAttributeId/view',
      name: 'RequestAttributeView',
      component: RequestAttributeDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/request-attribute-value',
      name: 'RequestAttributeValue',
      component: RequestAttributeValue,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-attribute-value/new',
      name: 'RequestAttributeValueCreate',
      component: RequestAttributeValueUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-attribute-value/:requestAttributeValueId/edit',
      name: 'RequestAttributeValueEdit',
      component: RequestAttributeValueUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-attribute-value/:requestAttributeValueId/view',
      name: 'RequestAttributeValueView',
      component: RequestAttributeValueDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/pipeline',
      name: 'Pipeline',
      component: Pipeline,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/pipeline/new',
      name: 'PipelineCreate',
      component: PipelineUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/pipeline/:pipelineId/edit',
      name: 'PipelineEdit',
      component: PipelineUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/pipeline/:pipelineId/view',
      name: 'PipelineView',
      component: PipelineDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/pipeline-attribute',
      name: 'PipelineAttribute',
      component: PipelineAttribute,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/pipeline-attribute/new',
      name: 'PipelineAttributeCreate',
      component: PipelineAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/pipeline-attribute/:pipelineAttributeId/edit',
      name: 'PipelineAttributeEdit',
      component: PipelineAttributeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/pipeline-attribute/:pipelineAttributeId/view',
      name: 'PipelineAttributeView',
      component: PipelineAttributeDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/acme-account',
      name: 'ACMEAccount',
      component: ACMEAccount,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-account/new',
      name: 'ACMEAccountCreate',
      component: ACMEAccountUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-account/:aCMEAccountId/edit',
      name: 'ACMEAccountEdit',
      component: ACMEAccountUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-account/:aCMEAccountId/view',
      name: 'ACMEAccountView',
      component: ACMEAccountDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/acme-contact',
      name: 'AcmeContact',
      component: AcmeContact,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-contact/new',
      name: 'AcmeContactCreate',
      component: AcmeContactUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-contact/:acmeContactId/edit',
      name: 'AcmeContactEdit',
      component: AcmeContactUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-contact/:acmeContactId/view',
      name: 'AcmeContactView',
      component: AcmeContactDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/acme-order',
      name: 'AcmeOrder',
      component: AcmeOrder,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-order/new',
      name: 'AcmeOrderCreate',
      component: AcmeOrderUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-order/:acmeOrderId/edit',
      name: 'AcmeOrderEdit',
      component: AcmeOrderUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-order/:acmeOrderId/view',
      name: 'AcmeOrderView',
      component: AcmeOrderDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/identifier',
      name: 'Identifier',
      component: Identifier,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/identifier/new',
      name: 'IdentifierCreate',
      component: IdentifierUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/identifier/:identifierId/edit',
      name: 'IdentifierEdit',
      component: IdentifierUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/identifier/:identifierId/view',
      name: 'IdentifierView',
      component: IdentifierDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/authorization',
      name: 'Authorization',
      component: Authorization,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/authorization/new',
      name: 'AuthorizationCreate',
      component: AuthorizationUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/authorization/:authorizationId/edit',
      name: 'AuthorizationEdit',
      component: AuthorizationUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/authorization/:authorizationId/view',
      name: 'AuthorizationView',
      component: AuthorizationDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/acme-challenge',
      name: 'AcmeChallenge',
      component: AcmeChallenge,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-challenge/new',
      name: 'AcmeChallengeCreate',
      component: AcmeChallengeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-challenge/:acmeChallengeId/edit',
      name: 'AcmeChallengeEdit',
      component: AcmeChallengeUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-challenge/:acmeChallengeId/view',
      name: 'AcmeChallengeView',
      component: AcmeChallengeDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/nonce',
      name: 'Nonce',
      component: Nonce,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/nonce/new',
      name: 'NonceCreate',
      component: NonceUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/nonce/:nonceId/edit',
      name: 'NonceEdit',
      component: NonceUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/nonce/:nonceId/view',
      name: 'NonceView',
      component: NonceDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/imported-url',
      name: 'ImportedURL',
      component: ImportedURL,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/imported-url/new',
      name: 'ImportedURLCreate',
      component: ImportedURLUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/imported-url/:importedURLId/edit',
      name: 'ImportedURLEdit',
      component: ImportedURLUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/imported-url/:importedURLId/view',
      name: 'ImportedURLView',
      component: ImportedURLDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/protected-content',
      name: 'ProtectedContent',
      component: ProtectedContent,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/protected-content/new',
      name: 'ProtectedContentCreate',
      component: ProtectedContentUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/protected-content/:protectedContentId/edit',
      name: 'ProtectedContentEdit',
      component: ProtectedContentUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/protected-content/:protectedContentId/view',
      name: 'ProtectedContentView',
      component: ProtectedContentDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/acme-identifier',
      name: 'AcmeIdentifier',
      component: AcmeIdentifier,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-identifier/new',
      name: 'AcmeIdentifierCreate',
      component: AcmeIdentifierUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-identifier/:acmeIdentifierId/edit',
      name: 'AcmeIdentifierEdit',
      component: AcmeIdentifierUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-identifier/:acmeIdentifierId/view',
      name: 'AcmeIdentifierView',
      component: AcmeIdentifierDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/acme-authorization',
      name: 'AcmeAuthorization',
      component: AcmeAuthorization,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-authorization/new',
      name: 'AcmeAuthorizationCreate',
      component: AcmeAuthorizationUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-authorization/:acmeAuthorizationId/edit',
      name: 'AcmeAuthorizationEdit',
      component: AcmeAuthorizationUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-authorization/:acmeAuthorizationId/view',
      name: 'AcmeAuthorizationView',
      component: AcmeAuthorizationDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/acme-nonce',
      name: 'AcmeNonce',
      component: AcmeNonce,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-nonce/new',
      name: 'AcmeNonceCreate',
      component: AcmeNonceUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-nonce/:acmeNonceId/edit',
      name: 'AcmeNonceEdit',
      component: AcmeNonceUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/acme-nonce/:acmeNonceId/view',
      name: 'AcmeNonceView',
      component: AcmeNonceDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/bpnm-process-info',
      name: 'BPNMProcessInfo',
      component: BPNMProcessInfo,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/bpnm-process-info/new',
      name: 'BPNMProcessInfoCreate',
      component: BPNMProcessInfoUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/bpnm-process-info/:bPNMProcessInfoId/edit',
      name: 'BPNMProcessInfoEdit',
      component: BPNMProcessInfoUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/bpnm-process-info/:bPNMProcessInfoId/view',
      name: 'BPNMProcessInfoView',
      component: BPNMProcessInfoDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    ,
    {
      path: '/request-proxy-config',
      name: 'RequestProxyConfig',
      component: RequestProxyConfig,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-proxy-config/new',
      name: 'RequestProxyConfigCreate',
      component: RequestProxyConfigUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-proxy-config/:requestProxyConfigId/edit',
      name: 'RequestProxyConfigEdit',
      component: RequestProxyConfigUpdate,
      meta: { authorities: ['ROLE_USER'] }
    },
    {
      path: '/request-proxy-config/:requestProxyConfigId/view',
      name: 'RequestProxyConfigView',
      component: RequestProxyConfigDetails,
      meta: { authorities: ['ROLE_USER'] }
    }
    // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
  ]
});
