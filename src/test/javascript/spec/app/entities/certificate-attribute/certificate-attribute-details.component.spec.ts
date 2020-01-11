/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import CertificateAttributeDetailComponent from '@/entities/certificate-attribute/certificate-attribute-details.vue';
import CertificateAttributeClass from '@/entities/certificate-attribute/certificate-attribute-details.component';
import CertificateAttributeService from '@/entities/certificate-attribute/certificate-attribute.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('CertificateAttribute Management Detail Component', () => {
    let wrapper: Wrapper<CertificateAttributeClass>;
    let comp: CertificateAttributeClass;
    let certificateAttributeServiceStub: SinonStubbedInstance<CertificateAttributeService>;

    beforeEach(() => {
      certificateAttributeServiceStub = sinon.createStubInstance<CertificateAttributeService>(CertificateAttributeService);

      wrapper = shallowMount<CertificateAttributeClass>(CertificateAttributeDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { certificateAttributeService: () => certificateAttributeServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundCertificateAttribute = { id: 123 };
        certificateAttributeServiceStub.find.resolves(foundCertificateAttribute);

        // WHEN
        comp.retrieveCertificateAttribute(123);
        await comp.$nextTick();

        // THEN
        expect(comp.certificateAttribute).toBe(foundCertificateAttribute);
      });
    });
  });
});
