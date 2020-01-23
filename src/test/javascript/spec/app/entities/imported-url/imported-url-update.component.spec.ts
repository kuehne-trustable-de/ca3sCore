/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import ImportedURLUpdateComponent from '@/entities/imported-url/imported-url-update.vue';
import ImportedURLClass from '@/entities/imported-url/imported-url-update.component';
import ImportedURLService from '@/entities/imported-url/imported-url.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('ImportedURL Management Update Component', () => {
    let wrapper: Wrapper<ImportedURLClass>;
    let comp: ImportedURLClass;
    let importedURLServiceStub: SinonStubbedInstance<ImportedURLService>;

    beforeEach(() => {
      importedURLServiceStub = sinon.createStubInstance<ImportedURLService>(ImportedURLService);

      wrapper = shallowMount<ImportedURLClass>(ImportedURLUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          importedURLService: () => importedURLServiceStub
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.importedURL = entity;
        importedURLServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(importedURLServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.importedURL = entity;
        importedURLServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(importedURLServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
