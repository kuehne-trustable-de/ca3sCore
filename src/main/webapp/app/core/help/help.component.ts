import { Component, Inject } from 'vue-property-decorator';
import { mixins } from 'vue-class-component';
import TranslationService from '@/locale/translation.service';

import AlertService from '@/shared/alert/alert.service';
import AlertMixin from '@/shared/alert/alert.mixin';

@Component
export default class Help extends mixins(AlertMixin) {
  @Inject('alertService') alertService: () => AlertService;

  @Inject('translationService') private translationService: () => TranslationService;

  public helpId = 'start';

  public changeLanguage(newLanguage: string): void {
    this.translationService().refreshTranslation(newLanguage);
  }

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.id) {
        vm.helpId = to.params.id;
      }
      if (to.params.lang) {
        this.changeLanguage(to.params.lang);
      }
    });
  }
}
