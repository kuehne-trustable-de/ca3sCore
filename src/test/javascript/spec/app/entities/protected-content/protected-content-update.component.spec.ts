/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import ProtectedContentUpdateComponent from '@/entities/protected-content/protected-content-update.vue';
import ProtectedContentClass from '@/entities/protected-content/protected-content-update.component';
import ProtectedContentService from '@/entities/protected-content/protected-content.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('ProtectedContent Management Update Component', () => {
    let wrapper: Wrapper<ProtectedContentClass>;
    let comp: ProtectedContentClass;
    let protectedContentServiceStub: SinonStubbedInstance<ProtectedContentService>;

    beforeEach(() => {
      protectedContentServiceStub = sinon.createStubInstance<ProtectedContentService>(ProtectedContentService);

      wrapper = shallowMount<ProtectedContentClass>(ProtectedContentUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          protectedContentService: () => protectedContentServiceStub
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.protectedContent = entity;
        protectedContentServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(protectedContentServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.protectedContent = entity;
        protectedContentServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(protectedContentServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
