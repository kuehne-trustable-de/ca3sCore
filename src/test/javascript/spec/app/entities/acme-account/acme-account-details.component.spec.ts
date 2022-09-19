/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import AcmeAccountDetailComponent from '@/entities/acme-account/acme-account-details.vue';
import AcmeAccountClass from '@/entities/acme-account/acme-account-details.component';
import AcmeAccountService from '@/entities/acme-account/acme-account.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('AcmeAccount Management Detail Component', () => {
    let wrapper: Wrapper<AcmeAccountClass>;
    let comp: AcmeAccountClass;
    let aCMEAccountServiceStub: SinonStubbedInstance<AcmeAccountService>;

    beforeEach(() => {
      aCMEAccountServiceStub = sinon.createStubInstance<AcmeAccountService>(AcmeAccountService);

      wrapper = shallowMount<AcmeAccountClass>(AcmeAccountDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { aCMEAccountService: () => aCMEAccountServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundAcmeAccount = { id: 123 };
        aCMEAccountServiceStub.find.resolves(foundAcmeAccount);

        // WHEN
        comp.retrieveAcmeAccount(123);
        await comp.$nextTick();

        // THEN
        expect(comp.aCMEAccount).toBe(foundAcmeAccount);
      });
    });
  });
});
