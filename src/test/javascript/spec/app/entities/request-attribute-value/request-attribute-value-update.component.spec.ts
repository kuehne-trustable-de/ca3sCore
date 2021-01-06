/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RequestAttributeValueUpdateComponent from '@/entities/request-attribute-value/request-attribute-value-update.vue';
import RequestAttributeValueClass from '@/entities/request-attribute-value/request-attribute-value-update.component';
import RequestAttributeValueService from '@/entities/request-attribute-value/request-attribute-value.service';

import RequestAttributeService from '@/entities/request-attribute/request-attribute.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('RequestAttributeValue Management Update Component', () => {
    let wrapper: Wrapper<RequestAttributeValueClass>;
    let comp: RequestAttributeValueClass;
    let requestAttributeValueServiceStub: SinonStubbedInstance<RequestAttributeValueService>;

    beforeEach(() => {
      requestAttributeValueServiceStub = sinon.createStubInstance<RequestAttributeValueService>(RequestAttributeValueService);

      wrapper = shallowMount<RequestAttributeValueClass>(RequestAttributeValueUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          requestAttributeValueService: () => requestAttributeValueServiceStub,

          requestAttributeService: () => new RequestAttributeService(),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.requestAttributeValue = entity;
        requestAttributeValueServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(requestAttributeValueServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.requestAttributeValue = entity;
        requestAttributeValueServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(requestAttributeValueServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
