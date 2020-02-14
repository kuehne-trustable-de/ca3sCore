/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import format from 'date-fns/format';
import parseISO from 'date-fns/parseISO';
import { DATE_TIME_LONG_FORMAT } from '@/shared/date/filters';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CSRUpdateComponent from '@/entities/csr/csr-update.vue';
import CSRClass from '@/entities/csr/csr-update.component';
import CSRService from '@/entities/csr/csr.service';

import RDNService from '@/entities/rdn/rdn.service';

import RequestAttributeService from '@/entities/request-attribute/request-attribute.service';

import CsrAttributeService from '@/entities/csr-attribute/csr-attribute.service';

import PipelineService from '@/entities/pipeline/pipeline.service';

import CertificateService from '@/entities/certificate/certificate.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('CSR Management Update Component', () => {
    let wrapper: Wrapper<CSRClass>;
    let comp: CSRClass;
    let cSRServiceStub: SinonStubbedInstance<CSRService>;

    beforeEach(() => {
      cSRServiceStub = sinon.createStubInstance<CSRService>(CSRService);

      wrapper = shallowMount<CSRClass>(CSRUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          cSRService: () => cSRServiceStub,

          rDNService: () => new RDNService(),

          requestAttributeService: () => new RequestAttributeService(),

          csrAttributeService: () => new CsrAttributeService(),

          pipelineService: () => new PipelineService(),

          certificateService: () => new CertificateService()
        }
      });
      comp = wrapper.vm;
    });

    describe('load', () => {
      it('Should convert date from string', () => {
        // GIVEN
        const date = new Date('2019-10-15T11:42:02Z');

        // WHEN
        const convertedDate = comp.convertDateTimeFromServer(date);

        // THEN
        expect(convertedDate).toEqual(format(date, DATE_TIME_LONG_FORMAT));
      });

      it('Should not convert date if date is not present', () => {
        expect(comp.convertDateTimeFromServer(null)).toBeNull();
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.cSR = entity;
        cSRServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(cSRServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.cSR = entity;
        cSRServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(cSRServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
