import { Component, Vue, Inject } from 'vue-property-decorator';

import axios from 'axios';

import { required } from 'vuelidate/lib/validators';

import RequestProxyConfigService from '../request-proxy-config/request-proxy-config.service';
import {
  IAuditView,
  ICsrUsage,
  IKeyAlgoLengthOrSpec,
  IPipelineType,
  IRDNRestriction,
  IRequestProxyConfig,
  IPipelineView,
  IARARestriction,
  IAcmeConfigItems,
  ISCEPConfigItems,
  IWebConfigItems,
  IBPMNProcessInfo,
  IRDNCardinalityRestriction,
  IBPMNProcessType,
} from '@/shared/model/transfer-object.model';

import CAConnectorConfigService from '../../entities/ca-connector-config/ca-connector-config.service';
import { ICAConnectorConfig } from '@/shared/model/ca-connector-config.model';
import { IUser } from '@/shared/model/user.model';

import BPNMProcessInfoService from '../../entities/bpnm-process-info/bpnm-process-info.service';

import AlertService from '@/shared/alert/alert.service';
import AlertMixin from '@/shared/alert/alert.mixin';

import PipelineViewService from './pipelineview.service';

import HelpTag from '@/core/help/help-tag.vue';
import AuditTag from '@/core/audit/audit-tag.vue';
import { mixins } from 'vue-class-component';
import UserManagementService from '@/admin/user-management/user-management.service';

const validations: any = {
  pipeline: {
    name: {
      required,
    },
    type: {
      required,
    },
    urlPart: {},
    description: {},
    listOrder: {},
    caConnectorName: { required },
    active: {},
    approvalRequired: {},
    approvalInfo1: {},
    toPendingOnFailedRestrictions: {},
    ipAsSubjectAllowed: {},
    ipAsSANAllowed: {},
    webConfigItems: {
      notifyRAOfficerOnPendingRequest: {},
      additionalEMailRecipients: {},
    },
    scepConfigItems: {
      scepSecretPCId: {},
      scepSecret: {},
      scepSecretValidTo: {},
      recepientCertSerial: {},
      recepientCertSubject: {},
      recepientCertId: {},
      scepRecipientDN: {},
    },
  },
};

@Component({
  validations,
  components: {
    HelpTag,
    AuditTag,
  },
})
// export default class PipelineUpdate extends Vue {
export default class PipelineUpdate extends mixins(AlertMixin) {
  //  @Inject('alertService') private alertService: () => AlertService;
  @Inject('pipelineViewService') private pipelineViewService: () => PipelineViewService;
  public pipeline: IPipelineView = new PipelineView();

  @Inject('requestProxyConfigService') private requestProxyConfigService: () => RequestProxyConfigService;
  @Inject('cAConnectorConfigService') private cAConnectorConfigService: () => CAConnectorConfigService;
  @Inject('bPNMProcessInfoService') private bPNMProcessInfoService: () => BPNMProcessInfoService;
  @Inject('userService') private userManagementService: () => UserManagementService;

  public requestProxyConfigs: IRequestProxyConfig[] = [];
  public cAConnectorConfigs: ICAConnectorConfig[] = [];
  public allCertGenerators: CAConnectorConfigService[] = [];
  public bPNMProcessInfos: IBPMNProcessInfo[] = [];
  public domainRAs: IUser[] = [];

  public isSaving = false;

  public alignARAArraySize(index: number): void {
    window.console.info('in alignARAArraySize(' + index + ')');
    const currentSize = this.pipeline.araRestrictions.length;
    const name = this.pipeline.araRestrictions[index].name || '';

    if (name.trim().length === 0) {
      if (currentSize > 1) {
        // preserve last element
        this.pipeline.araRestrictions.splice(index, 1);
        window.console.info('in alignARAArraySize(' + index + '): dropped empty element');
      }
    } else {
      if (index + 1 === currentSize) {
        this.pipeline.araRestrictions.push({});
        window.console.info('in alignARAArraySize(' + index + '): appended one element');
      }
    }
  }

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.pipelineId) {
        vm.retrievePipeline(to.params.pipelineId, to.params.mode);
      }
      vm.initRelationships();
    });
  }

  public readableUserName(user: IUser): string {
    let result = user.login;
    if (user.firstName && user.firstName.length > 0 && user.lastName && user.lastName.length > 0) {
      result += ' (' + user.lastName + ', ' + user.firstName + ')';
    } else if ((user.firstName && user.firstName.length > 0) || (user.lastName && user.lastName.length > 0)) {
      result += ' (';
      if (user.lastName && user.lastName.length > 0) {
        result += user.lastName;
      }
      if (user.firstName && user.firstName.length > 0) {
        result += user.firstName;
      }
      result += ')';
    }
    return result;
  }

  public save(): void {
    this.isSaving = true;
    if (this.pipeline.id) {
      this.pipelineViewService()
        .update(this.pipeline)
        .then(param => {
          this.isSaving = false;
          //          this.$router.go(-1);
          this.$router.push('/confPipeline');
          const message = this.$t('ca3SApp.pipeline.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        })
        .catch(param => {
          this.alertService().showAlert('error', 'warn');
        });
    } else {
      this.pipelineViewService()
        .create(this.pipeline)
        .then(param => {
          this.isSaving = false;
          //          this.$router.go(-1);
          this.$router.push('/confPipeline');
          const message = this.$t('ca3SApp.pipeline.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        })
        .catch(param => {
          this.alertService().showAlert('error', 'warn');
        });
    }
  }

  public retrievePipeline(pipelineId, mode): void {
    this.pipelineViewService()
      .find(pipelineId)
      .then(res => {
        this.pipeline = res;
        if (mode === 'copy') {
          this.pipeline.name = 'Copy of ' + this.pipeline.name;
          this.pipeline.id = null;
        }
        if (!this.pipeline.acmeConfigItems.allowChallengeDNS) {
          this.pipeline.acmeConfigItems.allowChallengeHTTP01 = true;
          this.pipeline.acmeConfigItems.allowWildcards = false;
        }
        if (this.pipeline.araRestrictions && this.pipeline.araRestrictions.length > 0) {
          window.console.info('pipeline.araRestrictions.length' + this.pipeline.araRestrictions.length);
        } else {
          window.console.info('pipeline.araRestrictions undefined');
          const araRestrictions: IARARestriction[] = new Array();
          this.pipeline.araRestrictions = araRestrictions;
          this.pipeline.araRestrictions.push({});
        }
      });
  }

  public previousState(): void {
    //          this.$router.go(-1);
    this.$router.push('/confPipeline');
  }

  public initRelationships(): void {
    /*
    this.pipelineAttributeService()
      .retrieve()
      .then(res => {
        this.pipelineAttributes = res.data;
      });
*/
    this.requestProxyConfigService()
      .retrieve()
      .then(res => {
        this.requestProxyConfigs = res.data;
      });

    this.cAConnectorConfigService()
      .retrieve()
      .then(res => {
        this.cAConnectorConfigs = res.data;
      });

    this.bPNMProcessInfoService()
      .retrieve()
      .then(res => {
        this.bPNMProcessInfos = res.data;
      });

    this.userManagementService()
      .retrieveUsersByRole('ROLE_RA_DOMAIN')
      .then(res => {
        this.domainRAs = res.data;
      });
  }

  public mounted(): void {
    this.fillData();
  }

  public fillData(): void {
    window.console.info('calling fillData ');
    const self = this;

    axios({
      method: 'get',
      url: 'api/ca-connector-configs/cert-generators',
      responseType: 'stream',
    }).then(function (response) {
      window.console.info('allCertGenerators returns ' + response.data);
      self.allCertGenerators = response.data;
    });
  }

  public getBPNMProcessInfosByType(type: IBPMNProcessType): IBPMNProcessInfo[] {
    const result = this.bPNMProcessInfos.filter(pi => {
      return pi.type === type;
    });
    return result;
  }
}

export class PipelineView implements IPipelineView {
  constructor(
    public id?: number,
    public name?: string,
    public type?: IPipelineType,
    public urlPart?: string,
    public description?: string,
    public listOrder?: number,
    public approvalRequired?: boolean,
    public active?: boolean,
    public caConnectorName?: string,
    public processInfoNameCreate?: string,
    public processInfoNameRevoke?: string,
    public processInfoNotify?: string,
    public restriction_C?: IRDNRestriction,
    public restriction_CN?: IRDNRestriction,
    public restriction_L?: IRDNRestriction,
    public restriction_O?: IRDNRestriction,
    public restriction_OU?: IRDNRestriction,
    public restriction_S?: IRDNRestriction,
    public restriction_E?: IRDNRestriction,
    public restriction_SAN?: IRDNRestriction,
    public rdnRestrictions?: IRDNRestriction[],
    public araRestrictions?: IARARestriction[],
    public domainRaOfficerList?: string[],
    public toPendingOnFailedRestrictions?: boolean,
    public ipAsSubjectAllowed?: boolean,
    public ipAsSANAllowed?: boolean,
    public acmeConfigItems?: IAcmeConfigItems,
    public scepConfigItems?: ISCEPConfigItems,
    public webConfigItems?: IWebConfigItems,
    public auditViewArr?: IAuditView[],
    public csrUsage?: ICsrUsage,
    public requestProxyConfigIds?: number[]
  ) {
    this.toPendingOnFailedRestrictions = this.toPendingOnFailedRestrictions || false;
    this.approvalRequired = this.approvalRequired || false;
    this.ipAsSANAllowed = this.ipAsSANAllowed || false;
    this.ipAsSubjectAllowed = this.ipAsSubjectAllowed || false;
    this.active = this.active || false;

    this.acmeConfigItems = new AcmeConfigItems();
    this.scepConfigItems = new SCEPConfigItems();
    this.webConfigItems = new WebConfigItems();

    this.restriction_C = new RDNRestriction();
    this.restriction_CN = new RDNRestriction();
    this.restriction_L = new RDNRestriction();
    this.restriction_O = new RDNRestriction();
    this.restriction_OU = new RDNRestriction();
    this.restriction_S = new RDNRestriction();
    this.restriction_E = new RDNRestriction();
    this.restriction_SAN = new RDNRestriction();
  }
}

export class AcmeConfigItems implements IAcmeConfigItems {
  constructor(
    public allowChallengeHTTP01?: boolean,
    public allowChallengeAlpn?: boolean,
    public allowChallengeDNS?: boolean,
    public allowWildcards?: boolean,
    public checkCAA?: boolean,
    public caNameCAA?: string,
    public processInfoNameAccountValidation?: string,
    public processInfoNameOrderValidation?: string,
    public processInfoNameChallengeValidation?: string
  ) {
    this.allowChallengeHTTP01 = this.allowChallengeHTTP01 || false;
    this.allowChallengeAlpn = this.allowChallengeAlpn || false;
    this.allowChallengeDNS = this.allowChallengeDNS || false;
    this.allowWildcards = this.allowWildcards || false;
    this.checkCAA = this.checkCAA || false;
  }
}

export class SCEPConfigItems implements ISCEPConfigItems {
  constructor(
    public capabilityRenewal?: boolean,
    public capabilityPostPKIOperation?: boolean,
    public recepientCertSubject?: string,
    public recepientCertSerial?: string,
    public recepientCertId?: number,
    public scepSecretPCId?: string,
    public scepSecret?: string,
    public scepSecretValidTo?: Date,
    public keyAlgoLength?: IKeyAlgoLengthOrSpec,
    public scepRecipientDN?: string,
    public caConnectorRecipientName?: string
  ) {
    this.capabilityRenewal = this.capabilityRenewal || false;
    this.capabilityPostPKIOperation = this.capabilityPostPKIOperation || false;
  }
}

export class WebConfigItems implements IWebConfigItems {
  constructor(public additionalEMailRecipients?: string, public notifyRAOfficerOnPendingRequest?: boolean) {
    this.notifyRAOfficerOnPendingRequest = this.notifyRAOfficerOnPendingRequest || false;
  }
}

export class RDNRestriction implements IRDNRestriction {
  constructor(
    public rdnName?: string,
    public cardinalityRestriction?: IRDNCardinalityRestriction,
    public contentTemplate?: string,
    public regEx?: string,
    public regExMatch?: boolean
  ) {
    this.regExMatch = this.regExMatch || false;
    this.cardinalityRestriction = this.cardinalityRestriction || 'ZERO_OR_ONE';
  }
}
