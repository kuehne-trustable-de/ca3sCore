/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import ACMEAccountDetailComponent from '@/entities/acme-account/acme-account-details.vue';
import ACMEAccountClass from '@/entities/acme-account/acme-account-details.component';
import ACMEAccountService from '@/entities/acme-account/acme-account.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('ACMEAccount Management Detail Component', () => {
    let wrapper: Wrapper<ACMEAccountClass>;
    let comp: ACMEAccountClass;
    let aCMEAccountServiceStub: SinonStubbedInstance<ACMEAccountService>;

    beforeEach(() => {
      aCMEAccountServiceStub = sinon.createStubInstance<ACMEAccountService>(ACMEAccountService);

      wrapper = shallowMount<ACMEAccountClass>(ACMEAccountDetailComponent, {
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
        const foundACMEAccount = { id: 123 };
        aCMEAccountServiceStub.find.resolves(foundACMEAccount);

        // WHEN
        comp.retrieveACMEAccount(123);
        await comp.$nextTick();

        // THEN
        expect(comp.aCMEAccount).toBe(foundACMEAccount);
      });
    });
  });
});
