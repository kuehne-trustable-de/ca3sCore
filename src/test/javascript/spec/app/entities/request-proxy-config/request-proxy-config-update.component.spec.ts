/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import AlertService from '@/shared/alert/alert.service';
import * as config from '@/shared/config/config';
import RequestProxyConfigUpdateComponent from '@/entities/request-proxy-config/request-proxy-config-update.vue';
import RequestProxyConfigClass from '@/entities/request-proxy-config/request-proxy-config-update.component';
import RequestProxyConfigService from '@/entities/request-proxy-config/request-proxy-config.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});

describe('Component Tests', () => {
  describe('RequestProxyConfig Management Update Component', () => {
    let wrapper: Wrapper<RequestProxyConfigClass>;
    let comp: RequestProxyConfigClass;
    let requestProxyConfigServiceStub: SinonStubbedInstance<RequestProxyConfigService>;

    beforeEach(() => {
      requestProxyConfigServiceStub = sinon.createStubInstance<RequestProxyConfigService>(RequestProxyConfigService);

      wrapper = shallowMount<RequestProxyConfigClass>(RequestProxyConfigUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          alertService: () => new AlertService(store),
          requestProxyConfigService: () => requestProxyConfigServiceStub
        }
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.requestProxyConfig = entity;
        requestProxyConfigServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(requestProxyConfigServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.requestProxyConfig = entity;
        requestProxyConfigServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(requestProxyConfigServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });
  });
});
