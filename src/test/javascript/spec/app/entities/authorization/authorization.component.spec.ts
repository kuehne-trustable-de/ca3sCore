/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AuthorizationComponent from '@/entities/authorization/authorization.vue';
import AuthorizationClass from '@/entities/authorization/authorization.component';
import AuthorizationService from '@/entities/authorization/authorization.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('b-alert', {});
localVue.component('b-badge', {});
localVue.directive('b-modal', {});
localVue.component('b-button', {});
localVue.component('router-link', {});

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {}
  }
};

describe('Component Tests', () => {
  describe('Authorization Management Component', () => {
    let wrapper: Wrapper<AuthorizationClass>;
    let comp: AuthorizationClass;
    let authorizationServiceStub: SinonStubbedInstance<AuthorizationService>;

    beforeEach(() => {
      authorizationServiceStub = sinon.createStubInstance<AuthorizationService>(AuthorizationService);
      authorizationServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<AuthorizationClass>(AuthorizationComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          authorizationService: () => authorizationServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      authorizationServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllAuthorizations();
      await comp.$nextTick();

      // THEN
      expect(authorizationServiceStub.retrieve.called).toBeTruthy();
      expect(comp.authorizations[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      authorizationServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeAuthorization();
      await comp.$nextTick();

      // THEN
      expect(authorizationServiceStub.delete.called).toBeTruthy();
      expect(authorizationServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
