/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeOrderUpdateComponent from '@/entities/acme-order/acme-order-update.vue';
import AcmeOrderClass from '@/entities/acme-order/acme-order-update.component';
import AcmeOrderService from '@/entities/acme-order/acme-order.service';

import AuthorizationService from '@/entities/authorization/authorization.service';

import IdentifierService from '@/entities/identifier/identifier.service';

import CSRService from '@/entities/csr/csr.service';

import CertificateService from '@/entities/certificate/certificate.service';

import ACMEAccountService from '@/entities/acme-account/acme-account.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('AcmeOrder Management Update Component', () => {
    let wrapper: Wrapper<AcmeOrderClass>;
    let comp: AcmeOrderClass;
    let acmeOrderServiceStub: SinonStubbedInstance<AcmeOrderService>;

    beforeEach(() => {
      acmeOrderServiceStub = sinon.createStubInstance<AcmeOrderService>(AcmeOrderService);

      wrapper = shallowMount<AcmeOrderClass>(AcmeOrderUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          acmeOrderService: () => acmeOrderServiceStub,

          authorizationService: () => new AuthorizationService(),

          identifierService: () => new IdentifierService(),

          cSRService: () => new CSRService(),

          certificateService: () => new CertificateService(),

          aCMEAccountService: () => new ACMEAccountService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.acmeOrder = entity;
        acmeOrderServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeOrderServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.acmeOrder = entity;
        acmeOrderServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeOrderServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
