/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RDNAttributeComponent from '@/entities/rdn-attribute/rdn-attribute.vue';
import RDNAttributeClass from '@/entities/rdn-attribute/rdn-attribute.component';
import RDNAttributeService from '@/entities/rdn-attribute/rdn-attribute.service';

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
  describe('RDNAttribute Management Component', () => {
    let wrapper: Wrapper<RDNAttributeClass>;
    let comp: RDNAttributeClass;
    let rDNAttributeServiceStub: SinonStubbedInstance<RDNAttributeService>;

    beforeEach(() => {
      rDNAttributeServiceStub = sinon.createStubInstance<RDNAttributeService>(RDNAttributeService);
      rDNAttributeServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<RDNAttributeClass>(RDNAttributeComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          rDNAttributeService: () => rDNAttributeServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      rDNAttributeServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllRDNAttributes();
      await comp.$nextTick();

      // THEN
      expect(rDNAttributeServiceStub.retrieve.called).toBeTruthy();
      expect(comp.rDNAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      rDNAttributeServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeRDNAttribute();
      await comp.$nextTick();

      // THEN
      expect(rDNAttributeServiceStub.delete.called).toBeTruthy();
      expect(rDNAttributeServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
