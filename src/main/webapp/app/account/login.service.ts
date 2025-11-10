import Vue from 'vue';

const STORAGE_LOGIN_MODE = 'loginMode';
const STORAGE_LOGIN_DOMAIN = 'loginDomain';

export default class LoginService {
  public openLogin(instance: Vue): void {
    localStorage.setItem(STORAGE_LOGIN_MODE, 'password');
    localStorage.setItem(STORAGE_LOGIN_DOMAIN, '');
    instance.$emit('bv::show::modal', 'login-page');
  }
  public openLdapLogin(instance: Vue, domain: string): void {
    localStorage.setItem(STORAGE_LOGIN_MODE, 'ldap');
    localStorage.setItem(STORAGE_LOGIN_DOMAIN, domain);
    instance.$emit('bv::show::modal', 'login-page');
  }
  public openLoginSpnego(instance: Vue): void {
    localStorage.setItem(STORAGE_LOGIN_MODE, 'spnego');
    instance.$emit('bv::show::modal', 'login-page');
    localStorage.setItem(STORAGE_LOGIN_DOMAIN, '');
  }
}
