/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import RequestAttributeValueDetailComponent from '@/entities/request-attribute-value/request-attribute-value-details.vue';
import RequestAttributeValueClass from '@/entities/request-attribute-value/request-attribute-value-details.component';
import RequestAttributeValueService from '@/entities/request-attribute-value/request-attribute-value.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('RequestAttributeValue Management Detail Component', () => {
    let wrapper: Wrapper<RequestAttributeValueClass>;
    let comp: RequestAttributeValueClass;
    let requestAttributeValueServiceStub: SinonStubbedInstance<RequestAttributeValueService>;

    beforeEach(() => {
      requestAttributeValueServiceStub = sinon.createStubInstance<RequestAttributeValueService>(RequestAttributeValueService);

      wrapper = shallowMount<RequestAttributeValueClass>(RequestAttributeValueDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { requestAttributeValueService: () => requestAttributeValueServiceStub },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundRequestAttributeValue = { id: 123 };
        requestAttributeValueServiceStub.find.resolves(foundRequestAttributeValue);

        // WHEN
        comp.retrieveRequestAttributeValue(123);
        await comp.$nextTick();

        // THEN
        expect(comp.requestAttributeValue).toBe(foundRequestAttributeValue);
      });
    });
  });
});
