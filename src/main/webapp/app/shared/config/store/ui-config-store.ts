import { Module } from 'vuex';
import { IUIConfigView } from '@/shared/model/transfer-object.model';

export const uiConfigStore: Module<any, any> = {
  state: {
    config: ''
  },

  getters: {
    config: state => state.config
  },
  mutations: {
    updateCV(state, cv: IUIConfigView) {
      state.config = cv;
    }
  }
};
