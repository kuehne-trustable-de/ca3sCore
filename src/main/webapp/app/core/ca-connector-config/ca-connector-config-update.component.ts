import { Component, Inject } from 'vue-property-decorator';

import axios from 'axios';

import { numeric, required, minLength, maxLength, minValue, maxValue } from 'vuelidate/lib/validators';

import CopyClipboardButton from '@/shared/clipboard/clipboard.vue';
import AlertService from '@/shared/alert/alert.service';
import CAConnectorConfigViewService from '@/entities/ca-connector-config/ca-connector-config-view.service';

import {
  ICAStatus,
  ICaConnectorConfigView,
  ICAConnectorType,
  IInterval,
  INamedValue,
  IADCSInstanceDetailsView,
  IAuthenticationParameter,
  IKDFType,
} from '@/shared/model/transfer-object.model';

import { mixins } from 'vue-class-component';
import JhiDataUtils from '@/shared/data/data-utils.service';

import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';

const validations: any = {
  cAConnectorConfig: {
    name: {
      required,
    },
    caConnectorType: {
      required,
    },
    caUrl: {},
    pollingOffset: {},
    lastUpdate: {},
    defaultCA: {},
    trustSelfsignedCertificates: {},
    active: {},
    selector: {},
    messageProtectionPassphrase: {},
    interval: {},
    tlsAuthenticationId: {},
    messageProtectionId: {},
    issuerName: {},
    multipleMessages: {},
    implicitConfirm: {},
    msgContentType: {},
    sni: {},
    ignoreResponseMessageVerification: {},
    fillEmptySubjectWithSAN: {},
    authenticationParameter: {
      kdfType: {
        required,
      },
      plainSecret: {
        required,
        minLength: minLength(6),
      },
      salt: {
        required,
        minLength: minLength(6),
      },
      cycles: {
        required,
        minValue: minValue(100000),
      },
      apiKeySalt: {
        minLength: minLength(6),
      },
      apiKeyCycles: {
        minValue: minValue(100000),
      },
    },
  },
};

@Component({
  validations,
  components: {
    HelpTag,
    CopyClipboardButton,
    AuditTag,
  },
})
export default class CAConnectorConfigUpdate extends mixins(JhiDataUtils) {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('cAConnectorConfigViewService') private cAConnectorConfigViewService: () => CAConnectorConfigViewService;
  public cAConnectorConfig: ICaConnectorConfigView = new CAConnectorConfigView();
  public caStatus: ICAStatus = 'Unknown';

  public adcsInstanceDetails: IADCSInstanceDetailsView = {};

  public isSaving = false;

  public adcsConfigSnippet = '';

  public authenticationParameter: IAuthenticationParameter = {
    kdfType: 'PBKDF2',
    plainSecret: 's3cr3t',
    cycles: 88888,
    salt: 'pepper',
    apiKeyCycles: 99999,
    apiKeySalt: 'apiPepper',
  };

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.cAConnectorConfigId) {
        vm.retrieveCAConnectorConfig(to.params.cAConnectorConfigId, to.params.mode);
      }
      vm.initRelationships();
    });
  }

  public isSaveable(): boolean {
    return !this.$v.cAConnectorConfig.$invalid;
  }

  public save(): void {
    this.isSaving = true;
    if (this.cAConnectorConfig.id) {
      this.cAConnectorConfigViewService()
        .update(this.cAConnectorConfig)
        .then(param => {
          this.isSaving = false;
          this.$router.push('/confCaConnector');
          const message = this.$t('ca3SApp.cAConnectorConfig.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.cAConnectorConfigViewService()
        .create(this.cAConnectorConfig)
        .then(param => {
          this.isSaving = false;
          this.$router.push('/confCaConnector');
          const message = this.$t('ca3SApp.cAConnectorConfig.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveCAConnectorConfig(cAConnectorConfigId, mode): void {
    this.cAConnectorConfigViewService()
      .find(cAConnectorConfigId)
      .then(res => {
        this.cAConnectorConfig = res;

        if (mode === 'copy') {
          this.cAConnectorConfig.name = 'Copy of ' + this.cAConnectorConfig.name;
          this.cAConnectorConfig.id = null;
          this.cAConnectorConfig.authenticationParameter.plainSecret = null;
        }

        if (this.cAConnectorConfig.caConnectorType === 'ADCS') {
          this.initADCSTemplates();
        }
      });
  }

  public isADCSConnectorConfig(): boolean {
    return this.cAConnectorConfig.caConnectorType === 'ADCS' || this.cAConnectorConfig.caConnectorType === 'ADCS_CERTIFICATE_INVENTORY';
  }

  public initADCSTemplates(): void {
    window.console.info('calling ca-connector-configViews/adcs/templates ');
    const self = this;

    self.adcsInstanceDetails = {};

    axios({
      method: 'post',
      url: 'api/ca-connector-configViews/adcs/templates',
      data: this.cAConnectorConfig,
      responseType: 'stream',
    }).then(function (response) {
      window.console.info('ca-connector-configViews/adcs/templates returns ' + response.data);

      self.adcsInstanceDetails = response.data;
    });
  }

  public hasADCSInstanceDetails(): boolean {
    return this.adcsInstanceDetails && this.adcsInstanceDetails.templates && this.adcsInstanceDetails.templates.length > 0;
  }

  public previousState(): void {
    this.$router.push('/confCaConnector');
  }

  public initRelationships(): void {}

  public testCaConnectorConfig(): void {
    window.console.info('calling checkCaConnectorConfig ');
    const self = this;

    axios({
      method: 'post',
      url: 'api/ca-connector-configs/getStatus',
      data: self.cAConnectorConfig,
      responseType: 'stream',
    }).then(function (response) {
      window.console.info('testCaConnectorConfig returns ' + response.data);
      self.caStatus = response.data;
    });
  }

  public buildAdcsConfigSnippet() {
    this.adcsConfigSnippet =
      'adcs-proxy:\n' +
      '  connection:\n' +
      '    secret: ' +
      this.cAConnectorConfig.authenticationParameter.plainSecret +
      '\n' +
      '    salt: ' +
      this.cAConnectorConfig.authenticationParameter.salt +
      '\n' +
      '    iterations: ' +
      this.cAConnectorConfig.authenticationParameter.cycles +
      '\n' +
      '    api-key-salt: ' +
      this.cAConnectorConfig.authenticationParameter.apiKeySalt +
      '\n' +
      '    api-key-iterations: ' +
      this.cAConnectorConfig.authenticationParameter.apiKeyCycles +
      '\n' +
      '    pbeAlgo: PBKDF2WithHmacSHA256';
  }
}

export class AuthenticationParameter implements IAuthenticationParameter {
  constructor(
    public kdfType?: IKDFType,
    public plainSecret?: string,
    public secretValidTo?: Date,
    public salt?: string,
    public cycles?: number,
    public apiKeySalt?: string,
    public apiKeyCycles?: number
  ) {
    this.kdfType = this.kdfType || 'PBKDF2';
    this.plainSecret = this.plainSecret || 's3cr3t';
    this.secretValidTo = this.secretValidTo || new Date();
    this.salt = this.salt || 'pepper';
    this.cycles = this.cycles || 100000;
    this.apiKeySalt = this.apiKeySalt || 'apiPepper';
    this.apiKeyCycles = this.apiKeyCycles || 100000;
  }
}

export class CAConnectorConfigView implements ICaConnectorConfigView {
  constructor(
    public id?: number,
    public name?: string,
    public caConnectorType?: ICAConnectorType,
    public caUrl?: string,
    public msgContentType?: string,
    public sni?: string,
    public pollingOffset?: number,
    public lastUpdate?: Date,
    public defaultCA?: boolean,
    public trustSelfsignedCertificates?: boolean,
    public active?: boolean,
    public selector?: string,
    public interval?: IInterval,
    public messageProtectionPassphrase?: boolean,
    public plainSecret?: string,
    public secretValidTo?: Date,
    public tlsAuthenticationId?: number,
    public messageProtectionId?: number,
    public issuerName?: string,
    public aTaVArr?: INamedValue[],
    public multipleMessages?: boolean,
    public implicitConfirm?: boolean,
    public authenticationParameter?: IAuthenticationParameter
  ) {
    this.defaultCA = this.defaultCA || false;
    this.trustSelfsignedCertificates = this.trustSelfsignedCertificates || false;
    this.messageProtectionPassphrase = this.messageProtectionPassphrase || false;
    this.active = this.active || false;
    this.interval = this.interval || 'DAY';
    this.authenticationParameter = this.authenticationParameter || new AuthenticationParameter();
  }
}
