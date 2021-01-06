/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import BPNMProcessInfoDetailComponent from '@/entities/bpnm-process-info/bpnm-process-info-details.vue';
import BPNMProcessInfoClass from '@/entities/bpnm-process-info/bpnm-process-info-details.component';
import BPNMProcessInfoService from '@/entities/bpnm-process-info/bpnm-process-info.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('BPNMProcessInfo Management Detail Component', () => {
    let wrapper: Wrapper<BPNMProcessInfoClass>;
    let comp: BPNMProcessInfoClass;
    let bPNMProcessInfoServiceStub: SinonStubbedInstance<BPNMProcessInfoService>;

    beforeEach(() => {
      bPNMProcessInfoServiceStub = sinon.createStubInstance<BPNMProcessInfoService>(BPNMProcessInfoService);

      wrapper = shallowMount<BPNMProcessInfoClass>(BPNMProcessInfoDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { bPNMProcessInfoService: () => bPNMProcessInfoServiceStub },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundBPNMProcessInfo = { id: 123 };
        bPNMProcessInfoServiceStub.find.resolves(foundBPNMProcessInfo);

        // WHEN
        comp.retrieveBPNMProcessInfo(123);
        await comp.$nextTick();

        // THEN
        expect(comp.bPNMProcessInfo).toBe(foundBPNMProcessInfo);
      });
    });
  });
});
