import axios from 'axios';
import VueI18n from 'vue-i18n';
import { Store } from 'vuex';

export default class TranslationService {
  private store: Store<{}>;
  private i18n: VueI18n;

  constructor(store: Store<{}>, i18n: VueI18n) {
    this.store = store;
    this.i18n = i18n;
  }

  public async refreshLanguages() {
    this.store.commit('languages', {
      de: { name: 'Deutsch' }
      ru: { name: 'Русский' }
      pl: { name: 'Polski' }
    });

    return axios.get('api/languages');

    /*
    const self = this;
    axios.get('api/languages').then(res => {
      if (res.data) {
        let newLanguages = new Object();
        for( let lang of res.data.languageArr){
          window.console.log('adding available language "' + lang + '" ...');
          if( lang === 'en'){
            newLanguages['en'] = { name: 'English' };
          }else if( lang === 'de'){
            newLanguages['de'] = { name: 'Deutsch' };
          }else if( lang === 'ru'){
            newLanguages['ru'] = { name: 'Русский' };
          }else if( lang === 'pl'){
            newLanguages['pl'] = { name: 'Polski' };
          }else{
            window.console.warn('unexpected language "' + lang + '" found');
          }
        }
        newLanguages['multiLanguage'] = res.data.languageArr.length > 1;

        self.store.commit('languages', newLanguages);
      }
    });

 */
  }

  public refreshTranslation(newLanguage: string) {
    let currentLanguage = this.store.getters.currentLanguage;
    currentLanguage = newLanguage ? newLanguage : 'en';
    if (this.i18n && !this.i18n.messages[currentLanguage]) {
      this.i18n.setLocaleMessage(currentLanguage, {});
      axios.get('i18n/' + currentLanguage + '.json').then(res => {
        if (res.data) {
          this.i18n.setLocaleMessage(currentLanguage, res.data);
          this.i18n.locale = currentLanguage;
          this.store.commit('currentLanguage', currentLanguage);
        }
      });
    } else if (this.i18n) {
      this.i18n.locale = currentLanguage;
      this.store.commit('currentLanguage', currentLanguage);
    }
  }
}
