/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import CAConnectorConfigDetailComponent from '@/entities/ca-connector-config/ca-connector-config-details.vue';
import CAConnectorConfigClass from '@/entities/ca-connector-config/ca-connector-config-details.component';
import CAConnectorConfigService from '@/entities/ca-connector-config/ca-connector-config.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('CAConnectorConfig Management Detail Component', () => {
    let wrapper: Wrapper<CAConnectorConfigClass>;
    let comp: CAConnectorConfigClass;
    let cAConnectorConfigServiceStub: SinonStubbedInstance<CAConnectorConfigService>;

    beforeEach(() => {
      cAConnectorConfigServiceStub = sinon.createStubInstance<CAConnectorConfigService>(CAConnectorConfigService);

      wrapper = shallowMount<CAConnectorConfigClass>(CAConnectorConfigDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { cAConnectorConfigService: () => cAConnectorConfigServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundCAConnectorConfig = { id: 123 };
        cAConnectorConfigServiceStub.find.resolves(foundCAConnectorConfig);

        // WHEN
        comp.retrieveCAConnectorConfig(123);
        await comp.$nextTick();

        // THEN
        expect(comp.cAConnectorConfig).toBe(foundCAConnectorConfig);
      });
    });
  });
});
