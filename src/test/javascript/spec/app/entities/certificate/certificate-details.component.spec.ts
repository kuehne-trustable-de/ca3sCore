/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import CertificateDetailComponent from '@/entities/certificate/certificate-details.vue';
import CertificateClass from '@/entities/certificate/certificate-details.component';
import CertificateService from '@/entities/certificate/certificate.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('Certificate Management Detail Component', () => {
    let wrapper: Wrapper<CertificateClass>;
    let comp: CertificateClass;
    let certificateServiceStub: SinonStubbedInstance<CertificateService>;

    beforeEach(() => {
      certificateServiceStub = sinon.createStubInstance<CertificateService>(CertificateService);

      wrapper = shallowMount<CertificateClass>(CertificateDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { certificateService: () => certificateServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundCertificate = { id: 123 };
        certificateServiceStub.find.resolves(foundCertificate);

        // WHEN
        comp.retrieveCertificate(123);
        await comp.$nextTick();

        // THEN
        expect(comp.certificate).toBe(foundCertificate);
      });
    });
  });
});
