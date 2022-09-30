// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.common with an alias.
import Vue from 'vue';
import { Datetime } from 'vue-datetime';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import App from './app.vue';
import Vue2Filters from 'vue2-filters';

import router from './router';
import * as config from './shared/config/config';
import * as bootstrapVueConfig from './shared/config/config-bootstrap-vue';
import JhiItemCountComponent from './shared/jhi-item-count.vue';
import AuditsService from './admin/audits/audits.service';

import HealthService from './admin/health/health.service';
import MetricsService from './admin/metrics/metrics.service';
import LogsService from './admin/logs/logs.service';
import ActivateService from './account/activate/activate.service';
import RegisterService from './account/register/register.service';
import UserManagementService from '@/admin/user-management/user-management.service';

import LoginService from './account/login.service';
import AccountService from './account/account.service';

import '../content/scss/vendor.scss';
import AlertService from '@/shared/alert/alert.service';
import TranslationService from '@/locale/translation.service';
import ConfigurationService from '@/admin/configuration/configuration.service';

import TrackerService from './admin/tracker/tracker.service';
import CAConnectorConfigService from '@/entities/ca-connector-config/ca-connector-config.service';
import CertificateAttributeService from '@/entities/certificate-attribute/certificate-attribute.service';
import CertificateService from '@/entities/certificate/certificate.service';
import CertificateViewService from '@/entities/certificate/certificate-view.service';
import CSRService from '@/entities/csr/csr.service';
import CSRServiceView from '@/entities/csr/csr-view.service';
import CsrAttributeService from '@/entities/csr-attribute/csr-attribute.service';
import RDNService from '@/entities/rdn/rdn.service';
import RDNAttributeService from '@/entities/rdn-attribute/rdn-attribute.service';
import RequestAttributeService from '@/entities/request-attribute/request-attribute.service';
import RequestAttributeValueService from '@/entities/request-attribute-value/request-attribute-value.service';
import PipelineViewService from '@/core/pipeline/pipelineview.service';
import PipelineService from '@/entities/pipeline/pipeline.service';
import PipelineAttributeService from '@/entities/pipeline-attribute/pipeline-attribute.service';
import AcmeAccountService from '@/entities/acme-account/acme-account.service';
import AcmeContactService from '@/entities/acme-contact/acme-contact.service';
import AcmeOrderService from '@/entities/acme-order/acme-order.service';
import IdentifierService from '@/entities/identifier/identifier.service';
import AuthorizationService from '@/entities/authorization/authorization.service';
import AcmeChallengeService from '@/entities/acme-challenge/acme-challenge.service';
import NonceService from '@/entities/nonce/nonce.service';
import ImportedURLService from '@/entities/imported-url/imported-url.service';
import AcmeIdentifierService from '@/entities/acme-identifier/acme-identifier.service';
import AcmeAuthorizationService from '@/entities/acme-authorization/acme-authorization.service';
import AcmeNonceService from '@/entities/acme-nonce/acme-nonce.service';
import BPNMProcessInfoService from '@/entities/bpnm-process-info/bpnm-process-info.service';
import RequestProxyConfigService from '@/entities/request-proxy-config/request-proxy-config.service';
import UserPreferenceService from '@/entities/user-preference/user-preference.service';
// jhipster-needle-add-entity-service-to-main-import - JHipster will import entities services here

Vue.config.productionTip = false;
config.initVueApp(Vue);
config.initFortAwesome(Vue);
bootstrapVueConfig.initBootstrapVue(Vue);
Vue.use(Vue2Filters);
Vue.component('font-awesome-icon', FontAwesomeIcon);
Vue.component('jhi-item-count', JhiItemCountComponent);

Vue.component('datetime', Datetime);
import 'vue-datetime/dist/vue-datetime.css';

const i18n = config.initI18N(Vue);
const store = config.initVueXStore(Vue);

const alertService = new AlertService(store);
const trackerService = new TrackerService(router);
const translationService = new TranslationService(store, i18n);
const loginService = new LoginService();
const accountService = new AccountService(store, translationService, trackerService, router);

router.beforeEach((to, from, next) => {
  if (!to.matched.length) {
    next('/not-found');
  }

  if (to.meta && to.meta.authorities && to.meta.authorities.length > 0) {
    if (!accountService.hasAnyAuthority(to.meta.authorities)) {
      sessionStorage.setItem('requested-url', to.fullPath);
      next('/forbidden');
    } else {
      next();
    }
  } else {
    // no authorities, so just proceed
    next();
  }
});

/* tslint:disable */
new Vue({
  el: '#app',
  components: { App },
  template: '<App/>',
  router,
  provide: {
    loginService: () => loginService,
    activateService: () => new ActivateService(),
    registerService: () => new RegisterService(),
    userService: () => new UserManagementService(),

    auditsService: () => new AuditsService(),

    healthService: () => new HealthService(),

    configurationService: () => new ConfigurationService(),
    logsService: () => new LogsService(),
    metricsService: () => new MetricsService(),
    trackerService: () => trackerService,
    alertService: () => alertService,
    translationService: () => translationService,
    cAConnectorConfigService: () => new CAConnectorConfigService(),
    certificateAttributeService: () => new CertificateAttributeService(),
    certificateService: () => new CertificateService(),
    certificateViewService: () => new CertificateViewService(),
    cSRService: () => new CSRService(),
    cSRViewService: () => new CSRServiceView(),
    csrAttributeService: () => new CsrAttributeService(),
    rDNService: () => new RDNService(),
    rDNAttributeService: () => new RDNAttributeService(),
    requestAttributeService: () => new RequestAttributeService(),
    requestAttributeValueService: () => new RequestAttributeValueService(),
    pipelineViewService: () => new PipelineViewService(),
    pipelineService: () => new PipelineService(),
    pipelineAttributeService: () => new PipelineAttributeService(),
    aCMEAccountService: () => new AcmeAccountService(),
    acmeContactService: () => new AcmeContactService(),
    acmeOrderService: () => new AcmeOrderService(),
    identifierService: () => new IdentifierService(),
    authorizationService: () => new AuthorizationService(),
    acmeChallengeService: () => new AcmeChallengeService(),
    nonceService: () => new NonceService(),
    importedURLService: () => new ImportedURLService(),
    acmeIdentifierService: () => new AcmeIdentifierService(),
    acmeAuthorizationService: () => new AcmeAuthorizationService(),
    acmeNonceService: () => new AcmeNonceService(),
    bPNMProcessInfoService: () => new BPNMProcessInfoService(),
    requestProxyConfigService: () => new RequestProxyConfigService(),
    userPreferenceService: () => new UserPreferenceService(),
    // jhipster-needle-add-entity-service-to-main - JHipster will import entities services here
    accountService: () => accountService
  },
  i18n,
  store
});
