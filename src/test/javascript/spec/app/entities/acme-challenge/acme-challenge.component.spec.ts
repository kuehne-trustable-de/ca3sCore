/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeChallengeComponent from '@/entities/acme-challenge/acme-challenge.vue';
import AcmeChallengeClass from '@/entities/acme-challenge/acme-challenge.component';
import AcmeChallengeService from '@/entities/acme-challenge/acme-challenge.service';

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
  describe('AcmeChallenge Management Component', () => {
    let wrapper: Wrapper<AcmeChallengeClass>;
    let comp: AcmeChallengeClass;
    let acmeChallengeServiceStub: SinonStubbedInstance<AcmeChallengeService>;

    beforeEach(() => {
      acmeChallengeServiceStub = sinon.createStubInstance<AcmeChallengeService>(AcmeChallengeService);
      acmeChallengeServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<AcmeChallengeClass>(AcmeChallengeComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          acmeChallengeService: () => acmeChallengeServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      acmeChallengeServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllAcmeChallenges();
      await comp.$nextTick();

      // THEN
      expect(acmeChallengeServiceStub.retrieve.called).toBeTruthy();
      expect(comp.acmeChallenges[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      acmeChallengeServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeAcmeChallenge();
      await comp.$nextTick();

      // THEN
      expect(acmeChallengeServiceStub.delete.called).toBeTruthy();
      expect(acmeChallengeServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
