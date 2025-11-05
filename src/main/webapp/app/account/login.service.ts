import Vue from 'vue';

const STORAGE_LOGIN_MODE = 'loginMode';

export default class LoginService {
  public openLogin(instance: Vue): void {
    localStorage.setItem(STORAGE_LOGIN_MODE, 'password');
    instance.$emit('bv::show::modal', 'login-page');
  }
  public openLoginSpnego(instance: Vue): void {
    localStorage.setItem(STORAGE_LOGIN_MODE, 'spnego');
    instance.$emit('bv::show::modal', 'login-page');
  }
}
