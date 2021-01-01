/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeAuthorizationComponent from '@/entities/acme-authorization/acme-authorization.vue';
import AcmeAuthorizationClass from '@/entities/acme-authorization/acme-authorization.component';
import AcmeAuthorizationService from '@/entities/acme-authorization/acme-authorization.service';

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
    hide: () => {},
    show: () => {}
  }
};

describe('Component Tests', () => {
  describe('AcmeAuthorization Management Component', () => {
    let wrapper: Wrapper<AcmeAuthorizationClass>;
    let comp: AcmeAuthorizationClass;
    let acmeAuthorizationServiceStub: SinonStubbedInstance<AcmeAuthorizationService>;

    beforeEach(() => {
      acmeAuthorizationServiceStub = sinon.createStubInstance<AcmeAuthorizationService>(AcmeAuthorizationService);
      acmeAuthorizationServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<AcmeAuthorizationClass>(AcmeAuthorizationComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          acmeAuthorizationService: () => acmeAuthorizationServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      acmeAuthorizationServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllAcmeAuthorizations();
      await comp.$nextTick();

      // THEN
      expect(acmeAuthorizationServiceStub.retrieve.called).toBeTruthy();
      expect(comp.acmeAuthorizations[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      acmeAuthorizationServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeAcmeAuthorization();
      await comp.$nextTick();

      // THEN
      expect(acmeAuthorizationServiceStub.delete.called).toBeTruthy();
      expect(acmeAuthorizationServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
