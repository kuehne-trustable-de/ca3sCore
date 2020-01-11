/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import NonceDetailComponent from '@/entities/nonce/nonce-details.vue';
import NonceClass from '@/entities/nonce/nonce-details.component';
import NonceService from '@/entities/nonce/nonce.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('Nonce Management Detail Component', () => {
    let wrapper: Wrapper<NonceClass>;
    let comp: NonceClass;
    let nonceServiceStub: SinonStubbedInstance<NonceService>;

    beforeEach(() => {
      nonceServiceStub = sinon.createStubInstance<NonceService>(NonceService);

      wrapper = shallowMount<NonceClass>(NonceDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { nonceService: () => nonceServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundNonce = { id: 123 };
        nonceServiceStub.find.resolves(foundNonce);

        // WHEN
        comp.retrieveNonce(123);
        await comp.$nextTick();

        // THEN
        expect(comp.nonce).toBe(foundNonce);
      });
    });
  });
});
