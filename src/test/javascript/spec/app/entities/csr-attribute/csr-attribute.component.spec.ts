/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CsrAttributeComponent from '@/entities/csr-attribute/csr-attribute.vue';
import CsrAttributeClass from '@/entities/csr-attribute/csr-attribute.component';
import CsrAttributeService from '@/entities/csr-attribute/csr-attribute.service';

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
  describe('CsrAttribute Management Component', () => {
    let wrapper: Wrapper<CsrAttributeClass>;
    let comp: CsrAttributeClass;
    let csrAttributeServiceStub: SinonStubbedInstance<CsrAttributeService>;

    beforeEach(() => {
      csrAttributeServiceStub = sinon.createStubInstance<CsrAttributeService>(CsrAttributeService);
      csrAttributeServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<CsrAttributeClass>(CsrAttributeComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          csrAttributeService: () => csrAttributeServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      csrAttributeServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllCsrAttributes();
      await comp.$nextTick();

      // THEN
      expect(csrAttributeServiceStub.retrieve.called).toBeTruthy();
      expect(comp.csrAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      csrAttributeServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeCsrAttribute();
      await comp.$nextTick();

      // THEN
      expect(csrAttributeServiceStub.delete.called).toBeTruthy();
      expect(csrAttributeServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
