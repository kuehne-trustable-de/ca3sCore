/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeContactComponent from '@/entities/acme-contact/acme-contact.vue';
import AcmeContactClass from '@/entities/acme-contact/acme-contact.component';
import AcmeContactService from '@/entities/acme-contact/acme-contact.service';

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
  describe('AcmeContact Management Component', () => {
    let wrapper: Wrapper<AcmeContactClass>;
    let comp: AcmeContactClass;
    let acmeContactServiceStub: SinonStubbedInstance<AcmeContactService>;

    beforeEach(() => {
      acmeContactServiceStub = sinon.createStubInstance<AcmeContactService>(AcmeContactService);
      acmeContactServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<AcmeContactClass>(AcmeContactComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          acmeContactService: () => acmeContactServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      acmeContactServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllAcmeContacts();
      await comp.$nextTick();

      // THEN
      expect(acmeContactServiceStub.retrieve.called).toBeTruthy();
      expect(comp.acmeContacts[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      acmeContactServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeAcmeContact();
      await comp.$nextTick();

      // THEN
      expect(acmeContactServiceStub.delete.called).toBeTruthy();
      expect(acmeContactServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
