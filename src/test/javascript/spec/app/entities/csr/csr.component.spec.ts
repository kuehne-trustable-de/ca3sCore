/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CSRComponent from '@/entities/csr/csr.vue';
import CSRClass from '@/entities/csr/csr.component';
import CSRService from '@/entities/csr/csr.service';

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
  describe('CSR Management Component', () => {
    let wrapper: Wrapper<CSRClass>;
    let comp: CSRClass;
    let cSRServiceStub: SinonStubbedInstance<CSRService>;

    beforeEach(() => {
      cSRServiceStub = sinon.createStubInstance<CSRService>(CSRService);
      cSRServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<CSRClass>(CSRComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          cSRService: () => cSRServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      cSRServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllCSRs();
      await comp.$nextTick();

      // THEN
      expect(cSRServiceStub.retrieve.called).toBeTruthy();
      expect(comp.cSRS[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      cSRServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeCSR();
      await comp.$nextTick();

      // THEN
      expect(cSRServiceStub.delete.called).toBeTruthy();
      expect(cSRServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
