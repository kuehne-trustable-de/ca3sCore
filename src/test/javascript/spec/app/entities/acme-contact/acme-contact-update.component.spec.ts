/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import AcmeContactUpdateComponent from '@/entities/acme-contact/acme-contact-update.vue';
import AcmeContactClass from '@/entities/acme-contact/acme-contact-update.component';
import AcmeContactService from '@/entities/acme-contact/acme-contact.service';

import AcmeAccountService from '@/entities/acme-account/acme-account.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('AcmeContact Management Update Component', () => {
    let wrapper: Wrapper<AcmeContactClass>;
    let comp: AcmeContactClass;
    let acmeContactServiceStub: SinonStubbedInstance<AcmeContactService>;

    beforeEach(() => {
      acmeContactServiceStub = sinon.createStubInstance<AcmeContactService>(AcmeContactService);

      wrapper = shallowMount<AcmeContactClass>(AcmeContactUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          acmeContactService: () => acmeContactServiceStub,

          aCMEAccountService: () => new AcmeAccountService()
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.acmeContact = entity;
        acmeContactServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeContactServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.acmeContact = entity;
        acmeContactServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(acmeContactServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
