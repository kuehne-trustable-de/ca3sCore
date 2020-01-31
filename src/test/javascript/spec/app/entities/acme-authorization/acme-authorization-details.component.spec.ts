/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import AcmeAuthorizationDetailComponent from '@/entities/acme-authorization/acme-authorization-details.vue';
import AcmeAuthorizationClass from '@/entities/acme-authorization/acme-authorization-details.component';
import AcmeAuthorizationService from '@/entities/acme-authorization/acme-authorization.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('AcmeAuthorization Management Detail Component', () => {
    let wrapper: Wrapper<AcmeAuthorizationClass>;
    let comp: AcmeAuthorizationClass;
    let acmeAuthorizationServiceStub: SinonStubbedInstance<AcmeAuthorizationService>;

    beforeEach(() => {
      acmeAuthorizationServiceStub = sinon.createStubInstance<AcmeAuthorizationService>(AcmeAuthorizationService);

      wrapper = shallowMount<AcmeAuthorizationClass>(AcmeAuthorizationDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { acmeAuthorizationService: () => acmeAuthorizationServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundAcmeAuthorization = { id: 123 };
        acmeAuthorizationServiceStub.find.resolves(foundAcmeAuthorization);

        // WHEN
        comp.retrieveAcmeAuthorization(123);
        await comp.$nextTick();

        // THEN
        expect(comp.acmeAuthorization).toBe(foundAcmeAuthorization);
      });
    });
  });
});
