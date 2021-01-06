/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import PipelineUpdateComponent from '@/entities/pipeline/pipeline-update.vue';
import PipelineClass from '@/entities/pipeline/pipeline-update.component';
import PipelineService from '@/entities/pipeline/pipeline.service';

import PipelineAttributeService from '@/entities/pipeline-attribute/pipeline-attribute.service';

import CAConnectorConfigService from '@/entities/ca-connector-config/ca-connector-config.service';

import BPNMProcessInfoService from '@/entities/bpnm-process-info/bpnm-process-info.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('Pipeline Management Update Component', () => {
    let wrapper: Wrapper<PipelineClass>;
    let comp: PipelineClass;
    let pipelineServiceStub: SinonStubbedInstance<PipelineService>;

    beforeEach(() => {
      pipelineServiceStub = sinon.createStubInstance<PipelineService>(PipelineService);

      wrapper = shallowMount<PipelineClass>(PipelineUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          pipelineService: () => pipelineServiceStub,

          pipelineAttributeService: () => new PipelineAttributeService(),

          cAConnectorConfigService: () => new CAConnectorConfigService(),

          bPNMProcessInfoService: () => new BPNMProcessInfoService(),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.pipeline = entity;
        pipelineServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(pipelineServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.pipeline = entity;
        pipelineServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(pipelineServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
