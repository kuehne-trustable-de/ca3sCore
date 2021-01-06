/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import PipelineComponent from '@/entities/pipeline/pipeline.vue';
import PipelineClass from '@/entities/pipeline/pipeline.component';
import PipelineService from '@/entities/pipeline/pipeline.service';

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
  describe('Pipeline Management Component', () => {
    let wrapper: Wrapper<PipelineClass>;
    let comp: PipelineClass;
    let pipelineServiceStub: SinonStubbedInstance<PipelineService>;

    beforeEach(() => {
      pipelineServiceStub = sinon.createStubInstance<PipelineService>(PipelineService);
      pipelineServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<PipelineClass>(PipelineComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          pipelineService: () => pipelineServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      pipelineServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllPipelines();
      await comp.$nextTick();

      // THEN
      expect(pipelineServiceStub.retrieve.called).toBeTruthy();
      expect(comp.pipelines[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      pipelineServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removePipeline();
      await comp.$nextTick();

      // THEN
      expect(pipelineServiceStub.delete.called).toBeTruthy();
      expect(pipelineServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
