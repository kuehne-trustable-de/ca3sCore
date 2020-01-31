/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeNonceUpdateComponent from '@/entities/acme-nonce/acme-nonce-update.vue';
import AcmeNonceClass from '@/entities/acme-nonce/acme-nonce-update.component';
import AcmeNonceService from '@/entities/acme-nonce/acme-nonce.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('AcmeNonce Management Update Component', () => {
    let wrapper: Wrapper<AcmeNonceClass>;
    let comp: AcmeNonceClass;
    let acmeNonceServiceStub: SinonStubbedInstance<AcmeNonceService>;

    beforeEach(() => {
      acmeNonceServiceStub = sinon.createStubInstance<AcmeNonceService>(AcmeNonceService);

      wrapper = shallowMount<AcmeNonceClass>(AcmeNonceUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          acmeNonceService: () => acmeNonceServiceStub
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.acmeNonce = entity;
        acmeNonceServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeNonceServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.acmeNonce = entity;
        acmeNonceServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeNonceServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
