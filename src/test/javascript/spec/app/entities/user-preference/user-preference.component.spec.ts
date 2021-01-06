/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import UserPreferenceComponent from '@/entities/user-preference/user-preference.vue';
import UserPreferenceClass from '@/entities/user-preference/user-preference.component';
import UserPreferenceService from '@/entities/user-preference/user-preference.service';

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
  describe('UserPreference Management Component', () => {
    let wrapper: Wrapper<UserPreferenceClass>;
    let comp: UserPreferenceClass;
    let userPreferenceServiceStub: SinonStubbedInstance<UserPreferenceService>;

    beforeEach(() => {
      userPreferenceServiceStub = sinon.createStubInstance<UserPreferenceService>(UserPreferenceService);
      userPreferenceServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<UserPreferenceClass>(UserPreferenceComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          userPreferenceService: () => userPreferenceServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    it('Should call load all on init', async () => {
      // GIVEN
      userPreferenceServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllUserPreferences();
      await comp.$nextTick();

      // THEN
      expect(userPreferenceServiceStub.retrieve.called).toBeTruthy();
      expect(comp.userPreferences[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      userPreferenceServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeUserPreference();
      await comp.$nextTick();

      // THEN
      expect(userPreferenceServiceStub.delete.called).toBeTruthy();
      expect(userPreferenceServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
