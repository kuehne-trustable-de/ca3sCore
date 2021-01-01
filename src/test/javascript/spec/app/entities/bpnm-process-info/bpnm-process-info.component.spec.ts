/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import BPNMProcessInfoComponent from '@/entities/bpnm-process-info/bpnm-process-info.vue';
import BPNMProcessInfoClass from '@/entities/bpnm-process-info/bpnm-process-info.component';
import BPNMProcessInfoService from '@/entities/bpnm-process-info/bpnm-process-info.service';

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
  describe('BPNMProcessInfo Management Component', () => {
    let wrapper: Wrapper<BPNMProcessInfoClass>;
    let comp: BPNMProcessInfoClass;
    let bPNMProcessInfoServiceStub: SinonStubbedInstance<BPNMProcessInfoService>;

    beforeEach(() => {
      bPNMProcessInfoServiceStub = sinon.createStubInstance<BPNMProcessInfoService>(BPNMProcessInfoService);
      bPNMProcessInfoServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<BPNMProcessInfoClass>(BPNMProcessInfoComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          bPNMProcessInfoService: () => bPNMProcessInfoServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      bPNMProcessInfoServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllBPNMProcessInfos();
      await comp.$nextTick();

      // THEN
      expect(bPNMProcessInfoServiceStub.retrieve.called).toBeTruthy();
      expect(comp.bPNMProcessInfos[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      bPNMProcessInfoServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeBPNMProcessInfo();
      await comp.$nextTick();

      // THEN
      expect(bPNMProcessInfoServiceStub.delete.called).toBeTruthy();
      expect(bPNMProcessInfoServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
