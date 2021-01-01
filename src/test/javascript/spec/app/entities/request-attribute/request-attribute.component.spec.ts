/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RequestAttributeComponent from '@/entities/request-attribute/request-attribute.vue';
import RequestAttributeClass from '@/entities/request-attribute/request-attribute.component';
import RequestAttributeService from '@/entities/request-attribute/request-attribute.service';

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
  describe('RequestAttribute Management Component', () => {
    let wrapper: Wrapper<RequestAttributeClass>;
    let comp: RequestAttributeClass;
    let requestAttributeServiceStub: SinonStubbedInstance<RequestAttributeService>;

    beforeEach(() => {
      requestAttributeServiceStub = sinon.createStubInstance<RequestAttributeService>(RequestAttributeService);
      requestAttributeServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<RequestAttributeClass>(RequestAttributeComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          requestAttributeService: () => requestAttributeServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      requestAttributeServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllRequestAttributes();
      await comp.$nextTick();

      // THEN
      expect(requestAttributeServiceStub.retrieve.called).toBeTruthy();
      expect(comp.requestAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      requestAttributeServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeRequestAttribute();
      await comp.$nextTick();

      // THEN
      expect(requestAttributeServiceStub.delete.called).toBeTruthy();
      expect(requestAttributeServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
