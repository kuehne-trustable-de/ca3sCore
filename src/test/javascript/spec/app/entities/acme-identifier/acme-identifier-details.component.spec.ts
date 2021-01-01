/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import AcmeIdentifierDetailComponent from '@/entities/acme-identifier/acme-identifier-details.vue';
import AcmeIdentifierClass from '@/entities/acme-identifier/acme-identifier-details.component';
import AcmeIdentifierService from '@/entities/acme-identifier/acme-identifier.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('AcmeIdentifier Management Detail Component', () => {
    let wrapper: Wrapper<AcmeIdentifierClass>;
    let comp: AcmeIdentifierClass;
    let acmeIdentifierServiceStub: SinonStubbedInstance<AcmeIdentifierService>;

    beforeEach(() => {
      acmeIdentifierServiceStub = sinon.createStubInstance<AcmeIdentifierService>(AcmeIdentifierService);

      wrapper = shallowMount<AcmeIdentifierClass>(AcmeIdentifierDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { acmeIdentifierService: () => acmeIdentifierServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundAcmeIdentifier = { id: 123 };
        acmeIdentifierServiceStub.find.resolves(foundAcmeIdentifier);

        // WHEN
        comp.retrieveAcmeIdentifier(123);
        await comp.$nextTick();

        // THEN
        expect(comp.acmeIdentifier).toBe(foundAcmeIdentifier);
      });
    });
  });
});
