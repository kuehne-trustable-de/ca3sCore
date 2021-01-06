/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import PipelineAttributeUpdateComponent from '@/entities/pipeline-attribute/pipeline-attribute-update.vue';
import PipelineAttributeClass from '@/entities/pipeline-attribute/pipeline-attribute-update.component';
import PipelineAttributeService from '@/entities/pipeline-attribute/pipeline-attribute.service';

import PipelineService from '@/entities/pipeline/pipeline.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('PipelineAttribute Management Update Component', () => {
    let wrapper: Wrapper<PipelineAttributeClass>;
    let comp: PipelineAttributeClass;
    let pipelineAttributeServiceStub: SinonStubbedInstance<PipelineAttributeService>;

    beforeEach(() => {
      pipelineAttributeServiceStub = sinon.createStubInstance<PipelineAttributeService>(PipelineAttributeService);

      wrapper = shallowMount<PipelineAttributeClass>(PipelineAttributeUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          pipelineAttributeService: () => pipelineAttributeServiceStub,

          pipelineService: () => new PipelineService(),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.pipelineAttribute = entity;
        pipelineAttributeServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(pipelineAttributeServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.pipelineAttribute = entity;
        pipelineAttributeServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(pipelineAttributeServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
