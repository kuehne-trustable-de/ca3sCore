/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import AuthorizationDetailComponent from '@/entities/authorization/authorization-details.vue';
import AuthorizationClass from '@/entities/authorization/authorization-details.component';
import AuthorizationService from '@/entities/authorization/authorization.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('Authorization Management Detail Component', () => {
    let wrapper: Wrapper<AuthorizationClass>;
    let comp: AuthorizationClass;
    let authorizationServiceStub: SinonStubbedInstance<AuthorizationService>;

    beforeEach(() => {
      authorizationServiceStub = sinon.createStubInstance<AuthorizationService>(AuthorizationService);

      wrapper = shallowMount<AuthorizationClass>(AuthorizationDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { authorizationService: () => authorizationServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundAuthorization = { id: 123 };
        authorizationServiceStub.find.resolves(foundAuthorization);

        // WHEN
        comp.retrieveAuthorization(123);
        await comp.$nextTick();

        // THEN
        expect(comp.authorization).toBe(foundAuthorization);
      });
    });
  });
});
