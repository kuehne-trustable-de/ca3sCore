/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CAConnectorConfigComponent from '@/entities/ca-connector-config/ca-connector-config.vue';
import CAConnectorConfigClass from '@/entities/ca-connector-config/ca-connector-config.component';
import CAConnectorConfigService from '@/entities/ca-connector-config/ca-connector-config.service';

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
  describe('CAConnectorConfig Management Component', () => {
    let wrapper: Wrapper<CAConnectorConfigClass>;
    let comp: CAConnectorConfigClass;
    let cAConnectorConfigServiceStub: SinonStubbedInstance<CAConnectorConfigService>;

    beforeEach(() => {
      cAConnectorConfigServiceStub = sinon.createStubInstance<CAConnectorConfigService>(CAConnectorConfigService);
      cAConnectorConfigServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<CAConnectorConfigClass>(CAConnectorConfigComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          cAConnectorConfigService: () => cAConnectorConfigServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    it('Should call load all on init', async () => {
      // GIVEN
      cAConnectorConfigServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllCAConnectorConfigs();
      await comp.$nextTick();

      // THEN
      expect(cAConnectorConfigServiceStub.retrieve.called).toBeTruthy();
      expect(comp.cAConnectorConfigs[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      cAConnectorConfigServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeCAConnectorConfig();
      await comp.$nextTick();

      // THEN
      expect(cAConnectorConfigServiceStub.delete.called).toBeTruthy();
      expect(cAConnectorConfigServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
