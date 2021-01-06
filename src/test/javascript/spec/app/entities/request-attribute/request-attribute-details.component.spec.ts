/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import RequestAttributeDetailComponent from '@/entities/request-attribute/request-attribute-details.vue';
import RequestAttributeClass from '@/entities/request-attribute/request-attribute-details.component';
import RequestAttributeService from '@/entities/request-attribute/request-attribute.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('RequestAttribute Management Detail Component', () => {
    let wrapper: Wrapper<RequestAttributeClass>;
    let comp: RequestAttributeClass;
    let requestAttributeServiceStub: SinonStubbedInstance<RequestAttributeService>;

    beforeEach(() => {
      requestAttributeServiceStub = sinon.createStubInstance<RequestAttributeService>(RequestAttributeService);

      wrapper = shallowMount<RequestAttributeClass>(RequestAttributeDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { requestAttributeService: () => requestAttributeServiceStub },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundRequestAttribute = { id: 123 };
        requestAttributeServiceStub.find.resolves(foundRequestAttribute);

        // WHEN
        comp.retrieveRequestAttribute(123);
        await comp.$nextTick();

        // THEN
        expect(comp.requestAttribute).toBe(foundRequestAttribute);
      });
    });
  });
});
