/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import CertificateAttributeComponent from '@/entities/certificate-attribute/certificate-attribute.vue';
import CertificateAttributeClass from '@/entities/certificate-attribute/certificate-attribute.component';
import CertificateAttributeService from '@/entities/certificate-attribute/certificate-attribute.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('b-alert', {});
localVue.component('b-badge', {});
localVue.directive('b-modal', {});
localVue.component('b-button', {});
localVue.component('router-link', {});

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {},
    show: () => {}
  }
};

describe('Component Tests', () => {
  describe('CertificateAttribute Management Component', () => {
    let wrapper: Wrapper<CertificateAttributeClass>;
    let comp: CertificateAttributeClass;
    let certificateAttributeServiceStub: SinonStubbedInstance<CertificateAttributeService>;

    beforeEach(() => {
      certificateAttributeServiceStub = sinon.createStubInstance<CertificateAttributeService>(CertificateAttributeService);
      certificateAttributeServiceStub.retrieve.resolves({ headers: {} });

      wrapper = shallowMount<CertificateAttributeClass>(CertificateAttributeComponent, {
        store,
        i18n,
        localVue,
        stubs: { bModal: bModalStub as any },
        provide: {
          alertService: () => new AlertService(store),
          certificateAttributeService: () => certificateAttributeServiceStub
        }
      });
      comp = wrapper.vm;
    });

    it('should be a Vue instance', () => {
      expect(wrapper.isVueInstance()).toBeTruthy();
    });

    it('Should call load all on init', async () => {
      // GIVEN
      certificateAttributeServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

      // WHEN
      comp.retrieveAllCertificateAttributes();
      await comp.$nextTick();

      // THEN
      expect(certificateAttributeServiceStub.retrieve.called).toBeTruthy();
      expect(comp.certificateAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
    it('Should call delete service on confirmDelete', async () => {
      // GIVEN
      certificateAttributeServiceStub.delete.resolves({});

      // WHEN
      comp.prepareRemove({ id: 123 });
      comp.removeCertificateAttribute();
      await comp.$nextTick();

      // THEN
      expect(certificateAttributeServiceStub.delete.called).toBeTruthy();
      expect(certificateAttributeServiceStub.retrieve.callCount).toEqual(2);
    });
  });
});
