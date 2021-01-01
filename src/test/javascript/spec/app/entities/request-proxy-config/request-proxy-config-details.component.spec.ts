/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import RequestProxyConfigDetailComponent from '@/entities/request-proxy-config/request-proxy-config-details.vue';
import RequestProxyConfigClass from '@/entities/request-proxy-config/request-proxy-config-details.component';
import RequestProxyConfigService from '@/entities/request-proxy-config/request-proxy-config.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('RequestProxyConfig Management Detail Component', () => {
    let wrapper: Wrapper<RequestProxyConfigClass>;
    let comp: RequestProxyConfigClass;
    let requestProxyConfigServiceStub: SinonStubbedInstance<RequestProxyConfigService>;

    beforeEach(() => {
      requestProxyConfigServiceStub = sinon.createStubInstance<RequestProxyConfigService>(RequestProxyConfigService);

      wrapper = shallowMount<RequestProxyConfigClass>(RequestProxyConfigDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { requestProxyConfigService: () => requestProxyConfigServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundRequestProxyConfig = { id: 123 };
        requestProxyConfigServiceStub.find.resolves(foundRequestProxyConfig);

        // WHEN
        comp.retrieveRequestProxyConfig(123);
        await comp.$nextTick();

        // THEN
        expect(comp.requestProxyConfig).toBe(foundRequestProxyConfig);
      });
    });
  });
});
