/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';

import * as config from '@/shared/config/config';
import AcmeChallengeDetailComponent from '@/entities/acme-challenge/acme-challenge-details.vue';
import AcmeChallengeClass from '@/entities/acme-challenge/acme-challenge-details.component';
import AcmeChallengeService from '@/entities/acme-challenge/acme-challenge.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('AcmeChallenge Management Detail Component', () => {
    let wrapper: Wrapper<AcmeChallengeClass>;
    let comp: AcmeChallengeClass;
    let acmeChallengeServiceStub: SinonStubbedInstance<AcmeChallengeService>;

    beforeEach(() => {
      acmeChallengeServiceStub = sinon.createStubInstance<AcmeChallengeService>(AcmeChallengeService);

      wrapper = shallowMount<AcmeChallengeClass>(AcmeChallengeDetailComponent, {
        store,
        i18n,
        localVue,
        provide: { acmeChallengeService: () => acmeChallengeServiceStub },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundAcmeChallenge = { id: 123 };
        acmeChallengeServiceStub.find.resolves(foundAcmeChallenge);

        // WHEN
        comp.retrieveAcmeChallenge(123);
        await comp.$nextTick();

        // THEN
        expect(comp.acmeChallenge).toBe(foundAcmeChallenge);
      });
    });
  });
});
