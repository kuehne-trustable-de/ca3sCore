/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import AcmeContactDetailComponent from '@/entities/acme-contact/acme-contact-details.vue';
import AcmeContactClass from '@/entities/acme-contact/acme-contact-details.component';
import AcmeContactService from '@/entities/acme-contact/acme-contact.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('AcmeContact Management Detail Component', () => {
    let wrapper: Wrapper<AcmeContactClass>;
    let comp: AcmeContactClass;
    let acmeContactServiceStub: SinonStubbedInstance<AcmeContactService>;

    beforeEach(() => {
      acmeContactServiceStub = sinon.createStubInstance<AcmeContactService>(AcmeContactService);

      wrapper = shallowMount<AcmeContactClass>(AcmeContactDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { acmeContactService: () => acmeContactServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundAcmeContact = { id: 123 };
        acmeContactServiceStub.find.resolves(foundAcmeContact);

        // WHEN
        comp.retrieveAcmeContact(123);
        await comp.$nextTick();

        // THEN
        expect(comp.acmeContact).toBe(foundAcmeContact);
      });
    });
  });
});
