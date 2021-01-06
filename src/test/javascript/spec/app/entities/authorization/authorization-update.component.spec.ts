/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AuthorizationUpdateComponent from '@/entities/authorization/authorization-update.vue';
import AuthorizationClass from '@/entities/authorization/authorization-update.component';
import AuthorizationService from '@/entities/authorization/authorization.service';

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
  describe('Authorization Management Update Component', () => {
    let wrapper: Wrapper<AuthorizationClass>;
    let comp: AuthorizationClass;
    let authorizationServiceStub: SinonStubbedInstance<AuthorizationService>;

    beforeEach(() => {
      authorizationServiceStub = sinon.createStubInstance<AuthorizationService>(AuthorizationService);

      wrapper = shallowMount<AuthorizationClass>(AuthorizationUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          authorizationService: () => authorizationServiceStub,

          acmeChallengeService: () => new AcmeChallengeService(),

          acmeOrderService: () => new AcmeOrderService(),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.authorization = entity;
        authorizationServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(authorizationServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.authorization = entity;
        authorizationServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(authorizationServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
