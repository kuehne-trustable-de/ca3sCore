/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RequestAttributeValueComponent from '@/entities/request-attribute-value/request-attribute-value.vue';
import RequestAttributeValueClass from '@/entities/request-attribute-value/request-attribute-value.component';
import RequestAttributeValueService from '@/entities/request-attribute-value/request-attribute-value.service';

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
    show: () => {},
  },
};

describe('Component Tests', () => {
  describe('RequestAttributeValue Management Component', () => {
    let wrapper: Wrapper<RequestAttributeValueClass>;
    let comp: RequestAttributeValueClass;
    let requestAttributeValueServiceStub: SinonStubbedInstance<RequestAttributeValueService>;

    beforeEach(() => {
      requestAttributeValueServiceStub = sinon.createStubInstance<RequestAttributeValueService>(RequestAttributeValueService);
      requestAttributeValueServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<RequestAttributeValueClass>(RequestAttributeValueComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          requestAttributeValueService: () => requestAttributeValueServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      requestAttributeValueServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllRequestAttributeValues();
      await comp.$nextTick();

      // THEN
      expect(requestAttributeValueServiceStub.retrieve.called).toBeTruthy();
      expect(comp.requestAttributeValues[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      requestAttributeValueServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeRequestAttributeValue();
      await comp.$nextTick();

      // THEN
      expect(requestAttributeValueServiceStub.delete.called).toBeTruthy();
      expect(requestAttributeValueServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
