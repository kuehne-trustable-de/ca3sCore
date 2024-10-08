import { createLocalVue, Wrapper, shallowMount } from '@vue/test-utils';
import * as config from '@/shared/config/config';
import AlertService from '@/shared/alert/alert.service';

const localVue = createLocalVue();
const store = config.initVueXStore(localVue);

describe('Alert service', () => {
  let alertService: AlertService;

  it('should init service', () => {
    alertService = new AlertService(store);

    expect(store.getters.dismissSecs).toBe(0);
    expect(store.getters.dismissCountDown).toBe(0);
    expect(store.getters.alertType).toBe('');
    expect(store.getters.alertMessage).not.toBeUndefined();
  });

  it('should propagate message on alert', () => {
    alertService = new AlertService(store);
    alertService.showAlert('A specific message', 'danger');

    expect(store.getters.alertType).toBe('danger');
    expect(store.getters.alertMessage).toBe('A specific message');
  });

  it('should propagate message on alert with default type', () => {
    alertService = new AlertService(store);
    alertService.showAlert({ msg: 'A specific message' });

    expect(store.getters.alertType).toBe('info');
    expect(store.getters.alertMessage.msg).toBe('A specific message');
  });

  it('should change countdown', () => {
    alertService = new AlertService(store);
    alertService.countDownChanged(30);

    expect(store.getters.dismissCountDown).toBe(30);
  });
});
