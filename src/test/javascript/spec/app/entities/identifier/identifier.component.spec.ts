/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import IdentifierComponent from '@/entities/identifier/identifier.vue';
import IdentifierClass from '@/entities/identifier/identifier.component';
import IdentifierService from '@/entities/identifier/identifier.service';

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
  describe('Identifier Management Component', () => {
    let wrapper: Wrapper<IdentifierClass>;
    let comp: IdentifierClass;
    let identifierServiceStub: SinonStubbedInstance<IdentifierService>;

    beforeEach(() => {
      identifierServiceStub = sinon.createStubInstance<IdentifierService>(IdentifierService);
      identifierServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<IdentifierClass>(IdentifierComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          identifierService: () => identifierServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      identifierServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllIdentifiers();
      await comp.$nextTick();

      // THEN
      expect(identifierServiceStub.retrieve.called).toBeTruthy();
      expect(comp.identifiers[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      identifierServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeIdentifier();
      await comp.$nextTick();

      // THEN
      expect(identifierServiceStub.delete.called).toBeTruthy();
      expect(identifierServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
