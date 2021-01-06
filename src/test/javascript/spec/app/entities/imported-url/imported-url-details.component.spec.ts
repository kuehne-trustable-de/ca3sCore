/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import ImportedURLDetailComponent from '@/entities/imported-url/imported-url-details.vue';
import ImportedURLClass from '@/entities/imported-url/imported-url-details.component';
import ImportedURLService from '@/entities/imported-url/imported-url.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('ImportedURL Management Detail Component', () => {
    let wrapper: Wrapper<ImportedURLClass>;
    let comp: ImportedURLClass;
    let importedURLServiceStub: SinonStubbedInstance<ImportedURLService>;

    beforeEach(() => {
      importedURLServiceStub = sinon.createStubInstance<ImportedURLService>(ImportedURLService);

      wrapper = shallowMount<ImportedURLClass>(ImportedURLDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { importedURLService: () => importedURLServiceStub },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundImportedURL = { id: 123 };
        importedURLServiceStub.find.resolves(foundImportedURL);

        // WHEN
        comp.retrieveImportedURL(123);
        await comp.$nextTick();

        // THEN
        expect(comp.importedURL).toBe(foundImportedURL);
      });
    });
  });
});
