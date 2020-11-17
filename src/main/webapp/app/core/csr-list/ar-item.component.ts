import { Component, Vue } from 'vue-property-decorator';

import { INamedValues } from '@/shared/model/transfer-object.model';

@Component({
  props: ['arAttribute'],
  template:
    '<v-fragment>' + '<dt><span >{{arAttribute.name}}</span></dt>\n' + '<dd><span>{{arAttribute.value[0]}}</span></dd>\n' + '</v-fragment>'
})
export default class ArItem extends Vue {
  public arAttribute: INamedValues = {};
}
