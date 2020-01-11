/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import RDNDetailComponent from '@/entities/rdn/rdn-details.vue';
import RDNClass from '@/entities/rdn/rdn-details.component';
import RDNService from '@/entities/rdn/rdn.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('RDN Management Detail Component', () => {
    let wrapper: Wrapper<RDNClass>;
    let comp: RDNClass;
    let rDNServiceStub: SinonStubbedInstance<RDNService>;

    beforeEach(() => {
      rDNServiceStub = sinon.createStubInstance<RDNService>(RDNService);

      wrapper = shallowMount<RDNClass>(RDNDetailComponent, { store, i18n, localVue, provide: { rDNService: () => rDNServiceStub } });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundRDN = { id: 123 };
        rDNServiceStub.find.resolves(foundRDN);

        // WHEN
        comp.retrieveRDN(123);
        await comp.$nextTick();

        // THEN
        expect(comp.rDN).toBe(foundRDN);
      });
    });
  });
});
