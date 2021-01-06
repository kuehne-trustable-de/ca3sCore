/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RDNAttributeUpdateComponent from '@/entities/rdn-attribute/rdn-attribute-update.vue';
import RDNAttributeClass from '@/entities/rdn-attribute/rdn-attribute-update.component';
import RDNAttributeService from '@/entities/rdn-attribute/rdn-attribute.service';

import RDNService from '@/entities/rdn/rdn.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('RDNAttribute Management Update Component', () => {
    let wrapper: Wrapper<RDNAttributeClass>;
    let comp: RDNAttributeClass;
    let rDNAttributeServiceStub: SinonStubbedInstance<RDNAttributeService>;

    beforeEach(() => {
      rDNAttributeServiceStub = sinon.createStubInstance<RDNAttributeService>(RDNAttributeService);

      wrapper = shallowMount<RDNAttributeClass>(RDNAttributeUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          rDNAttributeService: () => rDNAttributeServiceStub,

          rDNService: () => new RDNService(),
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.rDNAttribute = entity;
        rDNAttributeServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(rDNAttributeServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.rDNAttribute = entity;
        rDNAttributeServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(rDNAttributeServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
