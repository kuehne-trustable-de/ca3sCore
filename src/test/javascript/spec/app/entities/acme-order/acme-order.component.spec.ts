/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeOrderComponent from '@/entities/acme-order/acme-order.vue';
import AcmeOrderClass from '@/entities/acme-order/acme-order.component';
import AcmeOrderService from '@/entities/acme-order/acme-order.service';

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
  describe('AcmeOrder Management Component', () => {
    let wrapper: Wrapper<AcmeOrderClass>;
    let comp: AcmeOrderClass;
    let acmeOrderServiceStub: SinonStubbedInstance<AcmeOrderService>;

    beforeEach(() => {
      acmeOrderServiceStub = sinon.createStubInstance<AcmeOrderService>(AcmeOrderService);
      acmeOrderServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<AcmeOrderClass>(AcmeOrderComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          acmeOrderService: () => acmeOrderServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      acmeOrderServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllAcmeOrders();
      await comp.$nextTick();

      // THEN
      expect(acmeOrderServiceStub.retrieve.called).toBeTruthy();
      expect(comp.acmeOrders[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      acmeOrderServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeAcmeOrder();
      await comp.$nextTick();

      // THEN
      expect(acmeOrderServiceStub.delete.called).toBeTruthy();
      expect(acmeOrderServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
