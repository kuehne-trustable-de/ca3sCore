/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RequestProxyConfigComponent from '@/entities/request-proxy-config/request-proxy-config.vue';
import RequestProxyConfigClass from '@/entities/request-proxy-config/request-proxy-config.component';
import RequestProxyConfigService from '@/entities/request-proxy-config/request-proxy-config.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('b-alert', {});
localVue.component('b-badge', {});
localVue.directive('b-modal', {});
localVue.component('b-button', {});
localVue.component('router-link', {});

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {},
    show: () => {},
  },
};

describe('Component Tests', () => {
  describe('RequestProxyConfig Management Component', () => {
    let wrapper: Wrapper<RequestProxyConfigClass>;
    let comp: RequestProxyConfigClass;
    let requestProxyConfigServiceStub: SinonStubbedInstance<RequestProxyConfigService>;

    beforeEach(() => {
      requestProxyConfigServiceStub = sinon.createStubInstance<RequestProxyConfigService>(RequestProxyConfigService);
      requestProxyConfigServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<RequestProxyConfigClass>(RequestProxyConfigComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          requestProxyConfigService: () => requestProxyConfigServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      requestProxyConfigServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllRequestProxyConfigs();
      await comp.$nextTick();

      // THEN
      expect(requestProxyConfigServiceStub.retrieve.called).toBeTruthy();
      expect(comp.requestProxyConfigs[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      requestProxyConfigServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeRequestProxyConfig();
      await comp.$nextTick();

      // THEN
      expect(requestProxyConfigServiceStub.delete.called).toBeTruthy();
      expect(requestProxyConfigServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
