/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CAConnectorConfigUpdateComponent from '@/entities/ca-connector-config/ca-connector-config-update.vue';
import CAConnectorConfigClass from '@/entities/ca-connector-config/ca-connector-config-update.component';
import CAConnectorConfigService from '@/entities/ca-connector-config/ca-connector-config.service';

import ProtectedContentService from '@/entities/protected-content/protected-content.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('CAConnectorConfig Management Update Component', () => {
    let wrapper: Wrapper<CAConnectorConfigClass>;
    let comp: CAConnectorConfigClass;
    let cAConnectorConfigServiceStub: SinonStubbedInstance<CAConnectorConfigService>;

    beforeEach(() => {
      cAConnectorConfigServiceStub = sinon.createStubInstance<CAConnectorConfigService>(CAConnectorConfigService);

      wrapper = shallowMount<CAConnectorConfigClass>(CAConnectorConfigUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          cAConnectorConfigService: () => cAConnectorConfigServiceStub,

          protectedContentService: () => new ProtectedContentService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.cAConnectorConfig = entity;
        cAConnectorConfigServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(cAConnectorConfigServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.cAConnectorConfig = entity;
        cAConnectorConfigServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(cAConnectorConfigServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
