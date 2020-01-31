/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import AcmeNonceDetailComponent from '@/entities/acme-nonce/acme-nonce-details.vue';
import AcmeNonceClass from '@/entities/acme-nonce/acme-nonce-details.component';
import AcmeNonceService from '@/entities/acme-nonce/acme-nonce.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('AcmeNonce Management Detail Component', () => {
    let wrapper: Wrapper<AcmeNonceClass>;
    let comp: AcmeNonceClass;
    let acmeNonceServiceStub: SinonStubbedInstance<AcmeNonceService>;

    beforeEach(() => {
      acmeNonceServiceStub = sinon.createStubInstance<AcmeNonceService>(AcmeNonceService);

      wrapper = shallowMount<AcmeNonceClass>(AcmeNonceDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { acmeNonceService: () => acmeNonceServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundAcmeNonce = { id: 123 };
        acmeNonceServiceStub.find.resolves(foundAcmeNonce);

        // WHEN
        comp.retrieveAcmeNonce(123);
        await comp.$nextTick();

        // THEN
        expect(comp.acmeNonce).toBe(foundAcmeNonce);
      });
    });
  });
});
