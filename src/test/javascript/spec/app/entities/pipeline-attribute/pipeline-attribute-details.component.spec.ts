/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import PipelineAttributeDetailComponent from '@/entities/pipeline-attribute/pipeline-attribute-details.vue';
import PipelineAttributeClass from '@/entities/pipeline-attribute/pipeline-attribute-details.component';
import PipelineAttributeService from '@/entities/pipeline-attribute/pipeline-attribute.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('PipelineAttribute Management Detail Component', () => {
    let wrapper: Wrapper<PipelineAttributeClass>;
    let comp: PipelineAttributeClass;
    let pipelineAttributeServiceStub: SinonStubbedInstance<PipelineAttributeService>;

    beforeEach(() => {
      pipelineAttributeServiceStub = sinon.createStubInstance<PipelineAttributeService>(PipelineAttributeService);

      wrapper = shallowMount<PipelineAttributeClass>(PipelineAttributeDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { pipelineAttributeService: () => pipelineAttributeServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundPipelineAttribute = { id: 123 };
        pipelineAttributeServiceStub.find.resolves(foundPipelineAttribute);

        // WHEN
        comp.retrievePipelineAttribute(123);
        await comp.$nextTick();

        // THEN
        expect(comp.pipelineAttribute).toBe(foundPipelineAttribute);
      });
    });
  });
});
