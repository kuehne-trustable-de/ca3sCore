/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CertificateAttributeUpdateComponent from '@/entities/certificate-attribute/certificate-attribute-update.vue';
import CertificateAttributeClass from '@/entities/certificate-attribute/certificate-attribute-update.component';
import CertificateAttributeService from '@/entities/certificate-attribute/certificate-attribute.service';

import CertificateService from '@/entities/certificate/certificate.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('CertificateAttribute Management Update Component', () => {
    let wrapper: Wrapper<CertificateAttributeClass>;
    let comp: CertificateAttributeClass;
    let certificateAttributeServiceStub: SinonStubbedInstance<CertificateAttributeService>;

    beforeEach(() => {
      certificateAttributeServiceStub = sinon.createStubInstance<CertificateAttributeService>(CertificateAttributeService);

      wrapper = shallowMount<CertificateAttributeClass>(CertificateAttributeUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          certificateAttributeService: () => certificateAttributeServiceStub,

          certificateService: () => new CertificateService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.certificateAttribute = entity;
        certificateAttributeServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(certificateAttributeServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.certificateAttribute = entity;
        certificateAttributeServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(certificateAttributeServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
