import { Module } from 'vuex';

export const translationStore: Module<any, any> = {
  state: {
    currentLanguage: localStorage.getItem('currentLanguage') || 'en',
    languages: {
      en: { name: 'English' },
      de: { name: 'Deutsch' },
      ru: { name: 'Русский' },
      pl: { name: 'Polski' },

      multiLanguage: true
      // jhipster-needle-i18n-language-key-pipe - JHipster will add/remove languages in this object
    }
  },
  getters: {
    currentLanguage: state => state.currentLanguage,
    languages: state => state.languages
  },
  mutations: {
    currentLanguage(state, newLanguage) {
      window.console.log('setting currentLanguage to ' + newLanguage);
      state.currentLanguage = newLanguage;
      localStorage.setItem('currentLanguage', newLanguage);
    },
    languages(state, newLanguages) {
      //      window.console.log('setting languages to ' + newLanguages.pl.name);

      state.languages = newLanguages;
      localStorage.setItem('languages', newLanguages);
    }
  }
};
