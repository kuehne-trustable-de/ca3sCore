/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import NonceComponent from '@/entities/nonce/nonce.vue';
import NonceClass from '@/entities/nonce/nonce.component';
import NonceService from '@/entities/nonce/nonce.service';

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
  describe('Nonce Management Component', () => {
    let wrapper: Wrapper<NonceClass>;
    let comp: NonceClass;
    let nonceServiceStub: SinonStubbedInstance<NonceService>;

    beforeEach(() => {
      nonceServiceStub = sinon.createStubInstance<NonceService>(NonceService);
      nonceServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<NonceClass>(NonceComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          nonceService: () => nonceServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      nonceServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllNonces();
      await comp.$nextTick();

      // THEN
      expect(nonceServiceStub.retrieve.called).toBeTruthy();
      expect(comp.nonces[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      nonceServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeNonce();
      await comp.$nextTick();

      // THEN
      expect(nonceServiceStub.delete.called).toBeTruthy();
      expect(nonceServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
