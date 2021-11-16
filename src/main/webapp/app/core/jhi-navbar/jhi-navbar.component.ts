import { Component, Inject, Vue } from 'vue-property-decorator';
import { VERSION } from '@/constants';
import LoginService from '@/account/login.service';
import AccountService from '@/account/account.service';
import TranslationService from '@/locale/translation.service';

@Component
export default class JhiNavbar extends Vue {
  @Inject('loginService')
  private loginService: () => LoginService;
  @Inject('translationService') private translationService: () => TranslationService;

  @Inject('accountService') private accountService: () => AccountService;
  public version = VERSION ? 'v' + VERSION : '';
  private currentLanguage = this.$store.getters.currentLanguage;
  public languages: any = this.$store.getters.languages;

  public beforeRouteEnter(to, from, next) {
    next(vm => {
      console.log('JhiNavbar beforeRouteEnter : ' + to.params.showNavBar);
      if (to.params.showNavBar) {
      }
    });
  }

  async created() {
    const res = await this.translationService().refreshLanguages();

    if (res.data) {
      this.languages = new Object();
      for (let lang of res.data.languageArr) {
        window.console.log('adding available language "' + lang + '" ...');
        if (lang === 'en') {
          this.languages['en'] = { name: 'English' };
        } else if (lang === 'de') {
          this.languages['de'] = { name: 'Deutsch' };
        } else if (lang === 'pl') {
          this.languages['pl'] = { name: 'Polski' };
        } else {
          window.console.warn('unexpected language "' + lang + '" found');
        }
      }
      this.languages['multiLanguage'] = res.data.languageArr.length > 1;
    }

    this.translationService().refreshTranslation(this.currentLanguage);
  }

  public subIsActive(input) {
    const paths = Array.isArray(input) ? input : [input];
    return paths.some(path => {
      return this.$route.path.indexOf(path) === 0; // current path starts with this path string
    });
  }

  public changeLanguage(newLanguage: string): void {
    this.translationService().refreshTranslation(newLanguage);
  }

  public isActiveLanguage(key: string): boolean {
    return key === this.$store.getters.currentLanguage;
  }

  public logout(): void {
    localStorage.removeItem('jhi-authenticationToken');
    sessionStorage.removeItem('jhi-authenticationToken');
    this.$store.commit('logout');
    this.$router.push('/');
  }

  public openLogin(): void {
    this.loginService().openLogin((<any>this).$root);
  }

  public get authenticated(): boolean {
    return this.$store.getters.authenticated;
  }

  public get username(): string {
    return this.$store.getters.account ? this.$store.getters.account.login : '';
  }

  public get roles(): string {
    let roles = '';
    if (this.$store.getters.account) {
      for (const role of this.$store.getters.account.authorities) {
        if (roles.length > 0) {
          roles += ', ';
        }
        roles += this.$t(role);
      }
    }
    return roles;
  }

  public hasAnyAuthority(authorities: any): boolean {
    return this.accountService().hasAnyAuthority(authorities);
  }

  public get swaggerEnabled(): boolean {
    return this.$store.getters.activeProfiles.indexOf('swagger') > -1;
  }

  public get inProduction(): boolean {
    return this.$store.getters.activeProfiles.indexOf('prod') > -1;
  }
}
