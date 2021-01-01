/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import PipelineAttributeComponent from '@/entities/pipeline-attribute/pipeline-attribute.vue';
import PipelineAttributeClass from '@/entities/pipeline-attribute/pipeline-attribute.component';
import PipelineAttributeService from '@/entities/pipeline-attribute/pipeline-attribute.service';

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
    show: () => {}
  }
};

describe('Component Tests', () => {
  describe('PipelineAttribute Management Component', () => {
    let wrapper: Wrapper<PipelineAttributeClass>;
    let comp: PipelineAttributeClass;
    let pipelineAttributeServiceStub: SinonStubbedInstance<PipelineAttributeService>;

    beforeEach(() => {
      pipelineAttributeServiceStub = sinon.createStubInstance<PipelineAttributeService>(PipelineAttributeService);
      pipelineAttributeServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<PipelineAttributeClass>(PipelineAttributeComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          pipelineAttributeService: () => pipelineAttributeServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      pipelineAttributeServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllPipelineAttributes();
      await comp.$nextTick();

      // THEN
      expect(pipelineAttributeServiceStub.retrieve.called).toBeTruthy();
      expect(comp.pipelineAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      pipelineAttributeServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removePipelineAttribute();
      await comp.$nextTick();

      // THEN
      expect(pipelineAttributeServiceStub.delete.called).toBeTruthy();
      expect(pipelineAttributeServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
