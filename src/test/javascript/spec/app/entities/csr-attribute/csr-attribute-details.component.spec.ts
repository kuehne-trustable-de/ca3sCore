/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import CsrAttributeDetailComponent from '@/entities/csr-attribute/csr-attribute-details.vue';
import CsrAttributeClass from '@/entities/csr-attribute/csr-attribute-details.component';
import CsrAttributeService from '@/entities/csr-attribute/csr-attribute.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('CsrAttribute Management Detail Component', () => {
    let wrapper: Wrapper<CsrAttributeClass>;
    let comp: CsrAttributeClass;
    let csrAttributeServiceStub: SinonStubbedInstance<CsrAttributeService>;

    beforeEach(() => {
      csrAttributeServiceStub = sinon.createStubInstance<CsrAttributeService>(CsrAttributeService);

      wrapper = shallowMount<CsrAttributeClass>(CsrAttributeDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { csrAttributeService: () => csrAttributeServiceStub },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundCsrAttribute = { id: 123 };
        csrAttributeServiceStub.find.resolves(foundCsrAttribute);

        // WHEN
        comp.retrieveCsrAttribute(123);
        await comp.$nextTick();

        // THEN
        expect(comp.csrAttribute).toBe(foundCsrAttribute);
      });
    });
  });
});
