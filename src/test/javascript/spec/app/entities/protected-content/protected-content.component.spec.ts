/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import ProtectedContentComponent from '@/entities/protected-content/protected-content.vue';
import ProtectedContentClass from '@/entities/protected-content/protected-content.component';
import ProtectedContentService from '@/entities/protected-content/protected-content.service';

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
  describe('ProtectedContent Management Component', () => {
    let wrapper: Wrapper<ProtectedContentClass>;
    let comp: ProtectedContentClass;
    let protectedContentServiceStub: SinonStubbedInstance<ProtectedContentService>;

    beforeEach(() => {
      protectedContentServiceStub = sinon.createStubInstance<ProtectedContentService>(ProtectedContentService);
      protectedContentServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<ProtectedContentClass>(ProtectedContentComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          protectedContentService: () => protectedContentServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      protectedContentServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllProtectedContents();
      await comp.$nextTick();

      // THEN
      expect(protectedContentServiceStub.retrieve.called).toBeTruthy();
      expect(comp.protectedContents[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      protectedContentServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeProtectedContent();
      await comp.$nextTick();

      // THEN
      expect(protectedContentServiceStub.delete.called).toBeTruthy();
      expect(protectedContentServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
