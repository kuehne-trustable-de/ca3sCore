/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeNonceComponent from '@/entities/acme-nonce/acme-nonce.vue';
import AcmeNonceClass from '@/entities/acme-nonce/acme-nonce.component';
import AcmeNonceService from '@/entities/acme-nonce/acme-nonce.service';

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
  describe('AcmeNonce Management Component', () => {
    let wrapper: Wrapper<AcmeNonceClass>;
    let comp: AcmeNonceClass;
    let acmeNonceServiceStub: SinonStubbedInstance<AcmeNonceService>;

    beforeEach(() => {
      acmeNonceServiceStub = sinon.createStubInstance<AcmeNonceService>(AcmeNonceService);
      acmeNonceServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<AcmeNonceClass>(AcmeNonceComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          acmeNonceService: () => acmeNonceServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      acmeNonceServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllAcmeNonces();
      await comp.$nextTick();

      // THEN
      expect(acmeNonceServiceStub.retrieve.called).toBeTruthy();
      expect(comp.acmeNonces[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      acmeNonceServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeAcmeNonce();
      await comp.$nextTick();

      // THEN
      expect(acmeNonceServiceStub.delete.called).toBeTruthy();
      expect(acmeNonceServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
