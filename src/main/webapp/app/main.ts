// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.common with an alias.
import Vue from 'vue';
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
import CSRService from '@/entities/csr/csr.service';
import CsrAttributeService from '@/entities/csr-attribute/csr-attribute.service';
import RDNService from '@/entities/rdn/rdn.service';
import RDNAttributeService from '@/entities/rdn-attribute/rdn-attribute.service';
import RequestAttributeService from '@/entities/request-attribute/request-attribute.service';
import RequestAttributeValueService from '@/entities/request-attribute-value/request-attribute-value.service';
import PipelineService from '@/entities/pipeline/pipeline.service';
import PipelineAttributeService from '@/entities/pipeline-attribute/pipeline-attribute.service';
import ACMEAccountService from '@/entities/acme-account/acme-account.service';
import AcmeContactService from '@/entities/acme-contact/acme-contact.service';
import AcmeOrderService from '@/entities/acme-order/acme-order.service';
import IdentifierService from '@/entities/identifier/identifier.service';
import AuthorizationService from '@/entities/authorization/authorization.service';
import AcmeChallengeService from '@/entities/acme-challenge/acme-challenge.service';
import NonceService from '@/entities/nonce/nonce.service';
import ImportedURLService from '@/entities/imported-url/imported-url.service';
import ProtectedContentService from '@/entities/protected-content/protected-content.service';
// jhipster-needle-add-entity-service-to-main-import - JHipster will import entities services here

Vue.config.productionTip = false;
config.initVueApp(Vue);
config.initFortAwesome(Vue);
bootstrapVueConfig.initBootstrapVue(Vue);
Vue.use(Vue2Filters);
Vue.component('font-awesome-icon', FontAwesomeIcon);
Vue.component('jhi-item-count', JhiItemCountComponent);

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
    cSRService: () => new CSRService(),
    csrAttributeService: () => new CsrAttributeService(),
    rDNService: () => new RDNService(),
    rDNAttributeService: () => new RDNAttributeService(),
    requestAttributeService: () => new RequestAttributeService(),
    requestAttributeValueService: () => new RequestAttributeValueService(),
    pipelineService: () => new PipelineService(),
    pipelineAttributeService: () => new PipelineAttributeService(),
    aCMEAccountService: () => new ACMEAccountService(),
    acmeContactService: () => new AcmeContactService(),
    acmeOrderService: () => new AcmeOrderService(),
    identifierService: () => new IdentifierService(),
    authorizationService: () => new AuthorizationService(),
    acmeChallengeService: () => new AcmeChallengeService(),
    nonceService: () => new NonceService(),
    importedURLService: () => new ImportedURLService(),
    protectedContentService: () => new ProtectedContentService(),
    // jhipster-needle-add-entity-service-to-main - JHipster will import entities services here
    accountService: () => accountService
  },
  i18n,
  store
});
