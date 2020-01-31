/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeIdentifierUpdateComponent from '@/entities/acme-identifier/acme-identifier-update.vue';
import AcmeIdentifierClass from '@/entities/acme-identifier/acme-identifier-update.component';
import AcmeIdentifierService from '@/entities/acme-identifier/acme-identifier.service';

import AcmeOrderService from '@/entities/acme-order/acme-order.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('AcmeIdentifier Management Update Component', () => {
    let wrapper: Wrapper<AcmeIdentifierClass>;
    let comp: AcmeIdentifierClass;
    let acmeIdentifierServiceStub: SinonStubbedInstance<AcmeIdentifierService>;

    beforeEach(() => {
      acmeIdentifierServiceStub = sinon.createStubInstance<AcmeIdentifierService>(AcmeIdentifierService);

      wrapper = shallowMount<AcmeIdentifierClass>(AcmeIdentifierUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          acmeIdentifierService: () => acmeIdentifierServiceStub,

          acmeOrderService: () => new AcmeOrderService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.acmeIdentifier = entity;
        acmeIdentifierServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeIdentifierServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.acmeIdentifier = entity;
        acmeIdentifierServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeIdentifierServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
