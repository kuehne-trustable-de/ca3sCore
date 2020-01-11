/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import PipelineDetailComponent from '@/entities/pipeline/pipeline-details.vue';
import PipelineClass from '@/entities/pipeline/pipeline-details.component';
import PipelineService from '@/entities/pipeline/pipeline.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('Pipeline Management Detail Component', () => {
    let wrapper: Wrapper<PipelineClass>;
    let comp: PipelineClass;
    let pipelineServiceStub: SinonStubbedInstance<PipelineService>;

    beforeEach(() => {
      pipelineServiceStub = sinon.createStubInstance<PipelineService>(PipelineService);

      wrapper = shallowMount<PipelineClass>(PipelineDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { pipelineService: () => pipelineServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundPipeline = { id: 123 };
        pipelineServiceStub.find.resolves(foundPipeline);

        // WHEN
        comp.retrievePipeline(123);
        await comp.$nextTick();

        // THEN
        expect(comp.pipeline).toBe(foundPipeline);
      });
    });
  });
});
