/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import CSRDetailComponent from '@/entities/csr/csr-details.vue';
import CSRClass from '@/entities/csr/csr-details.component';
import CSRService from '@/entities/csr/csr.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('CSR Management Detail Component', () => {
    let wrapper: Wrapper<CSRClass>;
    let comp: CSRClass;
    let cSRServiceStub: SinonStubbedInstance<CSRService>;

    beforeEach(() => {
      cSRServiceStub = sinon.createStubInstance<CSRService>(CSRService);

      wrapper = shallowMount<CSRClass>(CSRDetailComponent, { store, i18n, localVue, provide: { cSRService: () => cSRServiceStub } });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundCSR = { id: 123 };
        cSRServiceStub.find.resolves(foundCSR);

        // WHEN
        comp.retrieveCSR(123);
        await comp.$nextTick();

        // THEN
        expect(comp.cSR).toBe(foundCSR);
      });
    });
  });
});
