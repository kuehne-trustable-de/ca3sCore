/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import IdentifierUpdateComponent from '@/entities/identifier/identifier-update.vue';
import IdentifierClass from '@/entities/identifier/identifier-update.component';
import IdentifierService from '@/entities/identifier/identifier.service';

import AcmeOrderService from '@/entities/acme-order/acme-order.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('Identifier Management Update Component', () => {
    let wrapper: Wrapper<IdentifierClass>;
    let comp: IdentifierClass;
    let identifierServiceStub: SinonStubbedInstance<IdentifierService>;

    beforeEach(() => {
      identifierServiceStub = sinon.createStubInstance<IdentifierService>(IdentifierService);

      wrapper = shallowMount<IdentifierClass>(IdentifierUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          identifierService: () => identifierServiceStub,

          acmeOrderService: () => new AcmeOrderService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.identifier = entity;
        identifierServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(identifierServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.identifier = entity;
        identifierServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(identifierServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
