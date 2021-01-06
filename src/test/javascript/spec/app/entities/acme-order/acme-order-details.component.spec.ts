/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import AcmeOrderDetailComponent from '@/entities/acme-order/acme-order-details.vue';
import AcmeOrderClass from '@/entities/acme-order/acme-order-details.component';
import AcmeOrderService from '@/entities/acme-order/acme-order.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('AcmeOrder Management Detail Component', () => {
    let wrapper: Wrapper<AcmeOrderClass>;
    let comp: AcmeOrderClass;
    let acmeOrderServiceStub: SinonStubbedInstance<AcmeOrderService>;

    beforeEach(() => {
      acmeOrderServiceStub = sinon.createStubInstance<AcmeOrderService>(AcmeOrderService);

      wrapper = shallowMount<AcmeOrderClass>(AcmeOrderDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { acmeOrderService: () => acmeOrderServiceStub },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundAcmeOrder = { id: 123 };
        acmeOrderServiceStub.find.resolves(foundAcmeOrder);

        // WHEN
        comp.retrieveAcmeOrder(123);
        await comp.$nextTick();

        // THEN
        expect(comp.acmeOrder).toBe(foundAcmeOrder);
      });
    });
  });
});
