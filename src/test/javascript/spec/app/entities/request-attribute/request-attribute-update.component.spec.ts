/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RequestAttributeUpdateComponent from '@/entities/request-attribute/request-attribute-update.vue';
import RequestAttributeClass from '@/entities/request-attribute/request-attribute-update.component';
import RequestAttributeService from '@/entities/request-attribute/request-attribute.service';

import RequestAttributeValueService from '@/entities/request-attribute-value/request-attribute-value.service';

import CSRService from '@/entities/csr/csr.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('RequestAttribute Management Update Component', () => {
    let wrapper: Wrapper<RequestAttributeClass>;
    let comp: RequestAttributeClass;
    let requestAttributeServiceStub: SinonStubbedInstance<RequestAttributeService>;

    beforeEach(() => {
      requestAttributeServiceStub = sinon.createStubInstance<RequestAttributeService>(RequestAttributeService);

      wrapper = shallowMount<RequestAttributeClass>(RequestAttributeUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          requestAttributeService: () => requestAttributeServiceStub,

          requestAttributeValueService: () => new RequestAttributeValueService(),

          cSRService: () => new CSRService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.requestAttribute = entity;
        requestAttributeServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(requestAttributeServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.requestAttribute = entity;
        requestAttributeServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(requestAttributeServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
