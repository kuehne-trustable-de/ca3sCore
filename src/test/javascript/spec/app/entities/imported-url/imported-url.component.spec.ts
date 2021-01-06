/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import ImportedURLComponent from '@/entities/imported-url/imported-url.vue';
import ImportedURLClass from '@/entities/imported-url/imported-url.component';
import ImportedURLService from '@/entities/imported-url/imported-url.service';

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
  describe('ImportedURL Management Component', () => {
    let wrapper: Wrapper<ImportedURLClass>;
    let comp: ImportedURLClass;
    let importedURLServiceStub: SinonStubbedInstance<ImportedURLService>;

    beforeEach(() => {
      importedURLServiceStub = sinon.createStubInstance<ImportedURLService>(ImportedURLService);
      importedURLServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<ImportedURLClass>(ImportedURLComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          importedURLService: () => importedURLServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      importedURLServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllImportedURLs();
      await comp.$nextTick();

      // THEN
      expect(importedURLServiceStub.retrieve.called).toBeTruthy();
      expect(comp.importedURLS[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      importedURLServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeImportedURL();
      await comp.$nextTick();

      // THEN
      expect(importedURLServiceStub.delete.called).toBeTruthy();
      expect(importedURLServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
