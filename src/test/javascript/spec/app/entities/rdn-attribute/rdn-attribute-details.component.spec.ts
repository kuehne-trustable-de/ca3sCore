/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import RDNAttributeDetailComponent from '@/entities/rdn-attribute/rdn-attribute-details.vue';
import RDNAttributeClass from '@/entities/rdn-attribute/rdn-attribute-details.component';
import RDNAttributeService from '@/entities/rdn-attribute/rdn-attribute.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('RDNAttribute Management Detail Component', () => {
    let wrapper: Wrapper<RDNAttributeClass>;
    let comp: RDNAttributeClass;
    let rDNAttributeServiceStub: SinonStubbedInstance<RDNAttributeService>;

    beforeEach(() => {
      rDNAttributeServiceStub = sinon.createStubInstance<RDNAttributeService>(RDNAttributeService);

      wrapper = shallowMount<RDNAttributeClass>(RDNAttributeDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { rDNAttributeService: () => rDNAttributeServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundRDNAttribute = { id: 123 };
        rDNAttributeServiceStub.find.resolves(foundRDNAttribute);

        // WHEN
        comp.retrieveRDNAttribute(123);
        await comp.$nextTick();

        // THEN
        expect(comp.rDNAttribute).toBe(foundRDNAttribute);
      });
    });
  });
});
