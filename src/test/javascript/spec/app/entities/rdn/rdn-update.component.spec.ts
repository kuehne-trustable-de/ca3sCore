/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RDNUpdateComponent from '@/entities/rdn/rdn-update.vue';
import RDNClass from '@/entities/rdn/rdn-update.component';
import RDNService from '@/entities/rdn/rdn.service';

import RDNAttributeService from '@/entities/rdn-attribute/rdn-attribute.service';

import CSRService from '@/entities/csr/csr.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('RDN Management Update Component', () => {
    let wrapper: Wrapper<RDNClass>;
    let comp: RDNClass;
    let rDNServiceStub: SinonStubbedInstance<RDNService>;

    beforeEach(() => {
      rDNServiceStub = sinon.createStubInstance<RDNService>(RDNService);

      wrapper = shallowMount<RDNClass>(RDNUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          rDNService: () => rDNServiceStub,

          rDNAttributeService: () => new RDNAttributeService(),

          cSRService: () => new CSRService(),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.rDN = entity;
        rDNServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(rDNServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.rDN = entity;
        rDNServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(rDNServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
