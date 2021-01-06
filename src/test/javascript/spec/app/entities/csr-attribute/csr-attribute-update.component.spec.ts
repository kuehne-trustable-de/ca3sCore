/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CsrAttributeUpdateComponent from '@/entities/csr-attribute/csr-attribute-update.vue';
import CsrAttributeClass from '@/entities/csr-attribute/csr-attribute-update.component';
import CsrAttributeService from '@/entities/csr-attribute/csr-attribute.service';

import CSRService from '@/entities/csr/csr.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('CsrAttribute Management Update Component', () => {
    let wrapper: Wrapper<CsrAttributeClass>;
    let comp: CsrAttributeClass;
    let csrAttributeServiceStub: SinonStubbedInstance<CsrAttributeService>;

    beforeEach(() => {
      csrAttributeServiceStub = sinon.createStubInstance<CsrAttributeService>(CsrAttributeService);

      wrapper = shallowMount<CsrAttributeClass>(CsrAttributeUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          csrAttributeService: () => csrAttributeServiceStub,

          cSRService: () => new CSRService(),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.csrAttribute = entity;
        csrAttributeServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(csrAttributeServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.csrAttribute = entity;
        csrAttributeServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(csrAttributeServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
