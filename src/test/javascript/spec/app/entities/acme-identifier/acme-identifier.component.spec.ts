/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeIdentifierComponent from '@/entities/acme-identifier/acme-identifier.vue';
import AcmeIdentifierClass from '@/entities/acme-identifier/acme-identifier.component';
import AcmeIdentifierService from '@/entities/acme-identifier/acme-identifier.service';

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
  describe('AcmeIdentifier Management Component', () => {
    let wrapper: Wrapper<AcmeIdentifierClass>;
    let comp: AcmeIdentifierClass;
    let acmeIdentifierServiceStub: SinonStubbedInstance<AcmeIdentifierService>;

    beforeEach(() => {
      acmeIdentifierServiceStub = sinon.createStubInstance<AcmeIdentifierService>(AcmeIdentifierService);
      acmeIdentifierServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<AcmeIdentifierClass>(AcmeIdentifierComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          acmeIdentifierService: () => acmeIdentifierServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      acmeIdentifierServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllAcmeIdentifiers();
      await comp.$nextTick();

      // THEN
      expect(acmeIdentifierServiceStub.retrieve.called).toBeTruthy();
      expect(comp.acmeIdentifiers[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      acmeIdentifierServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeAcmeIdentifier();
      await comp.$nextTick();

      // THEN
      expect(acmeIdentifierServiceStub.delete.called).toBeTruthy();
      expect(acmeIdentifierServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
