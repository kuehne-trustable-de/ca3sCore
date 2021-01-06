import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import * as sinon from 'sinon';
import { Observable, Observer } from 'rxjs';

import * as config from '@/shared/config/config';
import JhiTracker from '@/admin/tracker/tracker.vue';
import JhiTrackerClass from '@/admin/tracker/tracker.component';
import TrackerService from '@/admin/tracker/tracker.service';

const localVue = createLocalVue();
const mockedAxios: any = axios;
let trackerServiceStub: any;
let listenerObserver: Observer<any>;

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', FontAwesomeIcon);

jest.mock('axios', () => ({
  get: jest.fn(),
}));

describe('JhiTracker', () => {
  let wrapper: Wrapper<JhiTrackerClass>;
  let jhiTracker: JhiTrackerClass;

  beforeEach(() => {
    mockedAxios.get.mockReturnValue(Promise.resolve({ data: {} }));
    trackerServiceStub = sinon.createStubInstance<TrackerService>(TrackerService);
    trackerServiceStub.receive = sinon.stub().callsFake(() => new Observable(observer => (listenerObserver = observer)));
    wrapper = shallowMount<JhiTrackerClass>(JhiTracker, {
      store,
      i18n,
      localVue,
      provide: {
        trackerService: () => trackerServiceStub,
      },
    });
    jhiTracker = wrapper.vm;
  });

  it('should subscribe', () => {
    expect(trackerServiceStub.subscribe.called).toBeTruthy();
  });

  it('should unsubscribe at destroy', () => {
    // WHEN
    wrapper.destroy();

    // THEN
    expect(trackerServiceStub.unsubscribe.called).toBeTruthy();
  });

  it('should add new activity', () => {
    // GIVEN
    const activity1 = { page: 'login', sessionId: '123' };
    jhiTracker.activities = [activity1];

    // WHEN
    const activity2 = { page: 'login', sessionId: '456' };
    listenerObserver.next(activity2);

    // THEN
    expect(jhiTracker.activities).toEqual([activity1, activity2]);
  });

  it('should not add logout activity', () => {
    // WHEN
    listenerObserver.next({ page: 'logout', sessionId: '123' });

    // THEN
    expect(jhiTracker.activities).toEqual([]);
  });

  it('should update user activity', () => {
    // GIVEN
    jhiTracker.activities = [{ page: 'login', sessionId: '123' }];

    // WHEN
    const activity = { page: 'login', sessionId: '123' };
    listenerObserver.next(activity);

    // THEN
    expect(jhiTracker.activities).toEqual([activity]);
  });

  it('should remove user activity', () => {
    // GIVEN
    jhiTracker.activities = [{ page: 'login', sessionId: '123' }];

    // WHEN
    listenerObserver.next({ page: 'logout', sessionId: '123' });

    // THEN
    expect(jhiTracker.activities).toEqual([]);
  });
});
