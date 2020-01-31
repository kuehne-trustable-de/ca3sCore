/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeAuthorizationUpdateComponent from '@/entities/acme-authorization/acme-authorization-update.vue';
import AcmeAuthorizationClass from '@/entities/acme-authorization/acme-authorization-update.component';
import AcmeAuthorizationService from '@/entities/acme-authorization/acme-authorization.service';

import AcmeChallengeService from '@/entities/acme-challenge/acme-challenge.service';

import AcmeOrderService from '@/entities/acme-order/acme-order.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('AcmeAuthorization Management Update Component', () => {
    let wrapper: Wrapper<AcmeAuthorizationClass>;
    let comp: AcmeAuthorizationClass;
    let acmeAuthorizationServiceStub: SinonStubbedInstance<AcmeAuthorizationService>;

    beforeEach(() => {
      acmeAuthorizationServiceStub = sinon.createStubInstance<AcmeAuthorizationService>(AcmeAuthorizationService);

      wrapper = shallowMount<AcmeAuthorizationClass>(AcmeAuthorizationUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          acmeAuthorizationService: () => acmeAuthorizationServiceStub,

          acmeChallengeService: () => new AcmeChallengeService(),

          acmeOrderService: () => new AcmeOrderService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.acmeAuthorization = entity;
        acmeAuthorizationServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeAuthorizationServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.acmeAuthorization = entity;
        acmeAuthorizationServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeAuthorizationServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
