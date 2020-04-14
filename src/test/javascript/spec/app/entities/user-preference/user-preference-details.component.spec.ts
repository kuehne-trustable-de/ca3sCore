/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import UserPreferenceDetailComponent from '@/entities/user-preference/user-preference-details.vue';
import UserPreferenceClass from '@/entities/user-preference/user-preference-details.component';
import UserPreferenceService from '@/entities/user-preference/user-preference.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('UserPreference Management Detail Component', () => {
    let wrapper: Wrapper<UserPreferenceClass>;
    let comp: UserPreferenceClass;
    let userPreferenceServiceStub: SinonStubbedInstance<UserPreferenceService>;

    beforeEach(() => {
      userPreferenceServiceStub = sinon.createStubInstance<UserPreferenceService>(UserPreferenceService);

      wrapper = shallowMount<UserPreferenceClass>(UserPreferenceDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { userPreferenceService: () => userPreferenceServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundUserPreference = { id: 123 };
        userPreferenceServiceStub.find.resolves(foundUserPreference);

        // WHEN
        comp.retrieveUserPreference(123);
        await comp.$nextTick();

        // THEN
        expect(comp.userPreference).toBe(foundUserPreference);
      });
    });
  });
});
