/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import UserPreferenceUpdateComponent from '@/entities/user-preference/user-preference-update.vue';
import UserPreferenceClass from '@/entities/user-preference/user-preference-update.component';
import UserPreferenceService from '@/entities/user-preference/user-preference.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('UserPreference Management Update Component', () => {
    let wrapper: Wrapper<UserPreferenceClass>;
    let comp: UserPreferenceClass;
    let userPreferenceServiceStub: SinonStubbedInstance<UserPreferenceService>;

    beforeEach(() => {
      userPreferenceServiceStub = sinon.createStubInstance<UserPreferenceService>(UserPreferenceService);

      wrapper = shallowMount<UserPreferenceClass>(UserPreferenceUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          userPreferenceService: () => userPreferenceServiceStub
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.userPreference = entity;
        userPreferenceServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(userPreferenceServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.userPreference = entity;
        userPreferenceServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(userPreferenceServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
