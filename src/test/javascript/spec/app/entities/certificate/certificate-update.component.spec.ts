/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CertificateUpdateComponent from '@/entities/certificate/certificate-update.vue';
import CertificateClass from '@/entities/certificate/certificate-update.component';
import CertificateService from '@/entities/certificate/certificate.service';

import CSRService from '@/entities/csr/csr.service';

import CertificateAttributeService from '@/entities/certificate-attribute/certificate-attribute.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('Certificate Management Update Component', () => {
    let wrapper: Wrapper<CertificateClass>;
    let comp: CertificateClass;
    let certificateServiceStub: SinonStubbedInstance<CertificateService>;

    beforeEach(() => {
      certificateServiceStub = sinon.createStubInstance<CertificateService>(CertificateService);

      wrapper = shallowMount<CertificateClass>(CertificateUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          certificateService: () => certificateServiceStub,

          cSRService: () => new CSRService(),

          certificateAttributeService: () => new CertificateAttributeService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.certificate = entity;
        certificateServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(certificateServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.certificate = entity;
        certificateServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(certificateServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
