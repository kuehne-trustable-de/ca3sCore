/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import ProtectedContentDetailComponent from '@/entities/protected-content/protected-content-details.vue';
import ProtectedContentClass from '@/entities/protected-content/protected-content-details.component';
import ProtectedContentService from '@/entities/protected-content/protected-content.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('ProtectedContent Management Detail Component', () => {
    let wrapper: Wrapper<ProtectedContentClass>;
    let comp: ProtectedContentClass;
    let protectedContentServiceStub: SinonStubbedInstance<ProtectedContentService>;

    beforeEach(() => {
      protectedContentServiceStub = sinon.createStubInstance<ProtectedContentService>(ProtectedContentService);

      wrapper = shallowMount<ProtectedContentClass>(ProtectedContentDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { protectedContentService: () => protectedContentServiceStub }
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundProtectedContent = { id: 123 };
        protectedContentServiceStub.find.resolves(foundProtectedContent);

        // WHEN
        comp.retrieveProtectedContent(123);
        await comp.$nextTick();

        // THEN
        expect(comp.protectedContent).toBe(foundProtectedContent);
      });
    });
  });
});
