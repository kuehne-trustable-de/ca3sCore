/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import ACMEAccountUpdateComponent from '@/entities/acme-account/acme-account-update.vue';
import ACMEAccountClass from '@/entities/acme-account/acme-account-update.component';
import ACMEAccountService from '@/entities/acme-account/acme-account.service';

import AcmeContactService from '@/entities/acme-contact/acme-contact.service';

import AcmeOrderService from '@/entities/acme-order/acme-order.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('ACMEAccount Management Update Component', () => {
    let wrapper: Wrapper<ACMEAccountClass>;
    let comp: ACMEAccountClass;
    let aCMEAccountServiceStub: SinonStubbedInstance<ACMEAccountService>;

    beforeEach(() => {
      aCMEAccountServiceStub = sinon.createStubInstance<ACMEAccountService>(ACMEAccountService);

      wrapper = shallowMount<ACMEAccountClass>(ACMEAccountUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          aCMEAccountService: () => aCMEAccountServiceStub,

          acmeContactService: () => new AcmeContactService(),

          acmeOrderService: () => new AcmeOrderService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.aCMEAccount = entity;
        aCMEAccountServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(aCMEAccountServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.aCMEAccount = entity;
        aCMEAccountServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(aCMEAccountServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
