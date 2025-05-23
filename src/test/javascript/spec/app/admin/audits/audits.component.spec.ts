import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

import * as config from '@/shared/config/config';
import Audits from '@/admin/audits/audits.vue';
import AuditsClass from '@/admin/audits/audits.component';
import AuditsService from '@/admin/audits/audits.service';

const localVue = createLocalVue();
const mockedAxios: any = axios;

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', FontAwesomeIcon);

jest.mock('axios', () => ({
  get: jest.fn(),
  put: jest.fn(),
}));

describe('Audits Component', () => {
  let wrapper: Wrapper<AuditsClass>;
  let audits: AuditsClass;

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.get.mockReturnValue(Promise.resolve({ headers: {} }));
    wrapper = shallowMount<AuditsClass>(Audits, {
      store,
      i18n,
      localVue,
      stubs: {
        bPagination: true,
        jhiItemCount: true,
      },
      provide: {
        auditsService: () => new AuditsService(),
      },
    });
    audits = wrapper.vm;
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  describe('today function ', () => {
    it('should set toDate to current date', () => {
      audits.today();
      expect(audits.toDate).toBe(getDate());
    });
  });

  describe('previousMonth function ', () => {
    it('should set fromDate to current date', () => {
      audits.previousMonth();
      expect(audits.fromDate).toBe(getDate(false));
    });
  });

  describe('By default, on init', () => {
    it('should set all default values correctly', async () => {
      audits.init();
      await audits.$nextTick();

      expect(audits.predicate).toBe('timestamp');
      expect(audits.toDate).toBe(getDate());
      expect(audits.fromDate).toBe(getDate(false));
      expect(audits.itemsPerPage).toBe(20);
      expect(audits.page).toBe(1);
      expect(audits.reverse).toBeFalsy();
    });
  });

  describe('OnInit', () => {
    it('Should call load all on init', async () => {
      // GIVEN
      mockedAxios.get.mockReturnValue(Promise.resolve({ headers: {}, data: ['test'] }));
      const today = getDate();
      const fromDate = getDate(false);
      // WHEN
      audits.init();
      await audits.$nextTick();

      // THEN
      expect(mockedAxios.get).toHaveBeenCalledWith(
        `api/audits?fromDate=${fromDate}&toDate=${today}&sort=auditEventDate,desc&sort=id&page=0&size=20`
      );
      expect(audits.audits.length).toEqual(1);
    });
  });
});

function build2DigitsDatePart(datePart) {
  return `0${datePart}`.slice(-2);
}

function getDate(isToday = true) {
  let date = new Date();
  if (isToday) {
    // Today + 1 day - needed if the current day must be included
    date.setDate(date.getDate() + 1);
  } else {
    // get last month
    if (date.getMonth() === 0) {
      date = new Date(date.getFullYear() - 1, 11, date.getDate());
    } else {
      date = new Date(date.getFullYear(), date.getMonth() - 1, date.getDate());
    }
  }
  const monthString = build2DigitsDatePart(date.getMonth() + 1);
  const dateString = build2DigitsDatePart(date.getDate());
  return `${date.getFullYear()}-${monthString}-${dateString}`;
}
