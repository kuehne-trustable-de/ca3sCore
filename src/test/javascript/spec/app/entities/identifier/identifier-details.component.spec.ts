/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import IdentifierDetailComponent from '@/entities/identifier/identifier-details.vue';
import IdentifierClass from '@/entities/identifier/identifier-details.component';
import IdentifierService from '@/entities/identifier/identifier.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('Identifier Management Detail Component', () => {
    let wrapper: Wrapper<IdentifierClass>;
    let comp: IdentifierClass;
    let identifierServiceStub: SinonStubbedInstance<IdentifierService>;

    beforeEach(() => {
      identifierServiceStub = sinon.createStubInstance<IdentifierService>(IdentifierService);

      wrapper = shallowMount<IdentifierClass>(IdentifierDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { identifierService: () => identifierServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundIdentifier = { id: 123 };
        identifierServiceStub.find.resolves(foundIdentifier);

        // WHEN
        comp.retrieveIdentifier(123);
        await comp.$nextTick();

        // THEN
        expect(comp.identifier).toBe(foundIdentifier);
      });
    });
  });
});
