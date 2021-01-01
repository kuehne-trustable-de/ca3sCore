/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RDNComponent from '@/entities/rdn/rdn.vue';
import RDNClass from '@/entities/rdn/rdn.component';
import RDNService from '@/entities/rdn/rdn.service';

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
  describe('RDN Management Component', () => {
    let wrapper: Wrapper<RDNClass>;
    let comp: RDNClass;
    let rDNServiceStub: SinonStubbedInstance<RDNService>;

    beforeEach(() => {
      rDNServiceStub = sinon.createStubInstance<RDNService>(RDNService);
      rDNServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<RDNClass>(RDNComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          rDNService: () => rDNServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      rDNServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllRDNs();
      await comp.$nextTick();

      // THEN
      expect(rDNServiceStub.retrieve.called).toBeTruthy();
      expect(comp.rDNS[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      rDNServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeRDN();
      await comp.$nextTick();

      // THEN
      expect(rDNServiceStub.delete.called).toBeTruthy();
      expect(rDNServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
