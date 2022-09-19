/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeAccountComponent from '@/entities/acme-account/acme-account.vue';
import AcmeAccountClass from '@/entities/acme-account/acme-account.component';
import AcmeAccountService from '@/entities/acme-account/acme-account.service';

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
  describe('AcmeAccount Management Component', () => {
    let wrapper: Wrapper<AcmeAccountClass>;
    let comp: AcmeAccountClass;
    let aCMEAccountServiceStub: SinonStubbedInstance<AcmeAccountService>;

    beforeEach(() => {
      aCMEAccountServiceStub = sinon.createStubInstance<AcmeAccountService>(AcmeAccountService);
      aCMEAccountServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<AcmeAccountClass>(AcmeAccountComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          aCMEAccountService: () => aCMEAccountServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      aCMEAccountServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllAcmeAccounts();
      await comp.$nextTick();

      // THEN
      expect(aCMEAccountServiceStub.retrieve.called).toBeTruthy();
      expect(comp.aCMEAccounts[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      aCMEAccountServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeAcmeAccount();
      await comp.$nextTick();

      // THEN
      expect(aCMEAccountServiceStub.delete.called).toBeTruthy();
      expect(aCMEAccountServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
