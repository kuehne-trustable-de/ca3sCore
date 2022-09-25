/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import format from 'date-fns/format';
import parseISO from 'date-fns/parseISO';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeOrderUpdateComponent from '@/entities/acme-order/acme-order-update.vue';
import AcmeOrderClass from '@/entities/acme-order/acme-order-update.component';
import AcmeOrderService from '@/entities/acme-order/acme-order.service';

import AcmeAuthorizationService from '@/entities/acme-authorization/acme-authorization.service';

import AcmeIdentifierService from '@/entities/acme-identifier/acme-identifier.service';

import CSRService from '@/entities/csr/csr.service';

import CertificateService from '@/entities/certificate/certificate.service';

import AcmeAccountService from '@/entities/acme-account/acme-account.service';

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

          acmeAuthorizationService: () => new AcmeAuthorizationService(),

          acmeIdentifierService: () => new AcmeIdentifierService(),

          cSRService: () => new CSRService(),

          certificateService: () => new CertificateService(),

          aCMEAccountService: () => new AcmeAccountService()
        }
      });
      comp = wrapper.vm;
    });

    describe('load', () => {
      it('Should convert date from string', () => {
        // GIVEN
        const date = new Date('2019-10-15T11:42:02Z');

        // WHEN
        const convertedDate = comp.convertDateTimeFromServer(date);

        // THEN
        expect(convertedDate).toEqual(format(date, DATE_TIME_LONG_FORMAT));
      });

      it('Should not convert date if date is not present', () => {
        expect(comp.convertDateTimeFromServer(null)).toBeNull();
      });
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
