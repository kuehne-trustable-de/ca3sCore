<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.pipeline.home.createOrEditLabel" v-text="$t('ca3SApp.pipeline.home.editLabel', {'id': pipeline.id})"></h2>

                <b-alert :show="dismissCountDown"
                         dismissible
                         :variant="alertType"
                         @dismissed="dismissCountDown=0"
                         @dismiss-count-down="countDownChanged">
                    {{alertMessage}}
                </b-alert>
                <br/>

                <div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.name')" for="pipeline-name"></label>  <help-tag role="Admin" target="pipeline.name"/>
                        <input type="text" class="form-control" name="name" id="pipeline-name"
                            :class="{'valid': !$v.pipeline.name.$invalid, 'invalid': $v.pipeline.name.$invalid }" v-model="$v.pipeline.name.$model" required/>
                        <div v-if="$v.pipeline.name.$anyDirty && $v.pipeline.name.$invalid">
                            <small class="form-text text-danger" v-if="$v.pipeline.name.required" v-text="$t('entity.validation.required')"></small>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.active')" for="pipeline-active"></label>  <help-tag role="Admin" target="pipeline.active"/>
                        <input type="checkbox" class="form-check-inline" name="active" id="pipeline-active"
                               v-model="$v.pipeline.active.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.approvalRequired')" for="pipeline-approvalRequired"></label>  <help-tag role="Admin" target="pipeline.approval-required"/>
                        <input type="checkbox" class="form-check-inline" name="approvalRequired" id="pipeline-approvalRequired"
                               :class="{'valid': !$v.pipeline.approvalRequired.$invalid, 'invalid': $v.pipeline.approvalRequired.$invalid }" v-model="$v.pipeline.approvalRequired.$model" />
                    </div>


                  <div class="form-group" v-if="$v.pipeline.type.$model === 'WEB'">
                    <label class="form-control-label" v-text="$t('ca3SApp.pipeline.issuesSecondFactorClientCert')" for="pipeline-issuesSecondFactorClientCert"></label>  <help-tag role="Admin" target="pipeline.issuesSecondFactorClientCert"/>
                    <input type="checkbox" class="form-check-inline" name="pipeline-issuesSecondFactorClientCert" id="pipeline-issuesSecondFactorClientCert"
                           v-model="$v.pipeline.webConfigItems.issuesSecondFactorClientCert.$model" />
                  </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.type')" for="pipeline-type"></label>  <help-tag role="Admin" target="pipeline.type"/>
                        <select class="form-control" name="type" :class="{'valid': !$v.pipeline.type.$invalid, 'invalid': $v.pipeline.type.$invalid }" v-model="$v.pipeline.type.$model" id="pipeline-type"  required>
                            <option value="ACME" v-bind:label="$t('ca3SApp.PipelineType.ACME')">ACME</option>
                            <option value="SCEP" v-bind:label="$t('ca3SApp.PipelineType.SCEP')">SCEP</option>
                            <option value="WEB" v-bind:label="$t('ca3SApp.PipelineType.WEB')">WEB</option>
                        </select>
                        <div v-if="$v.pipeline.type.$anyDirty && $v.pipeline.type.$invalid">
                            <small class="form-text text-danger" v-if="$v.pipeline.type.required" v-text="$t('entity.validation.required')"></small>
                        </div>
                    </div>
                    <div class="form-group" v-if="$v.pipeline.type.$model !== 'WEB'">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.urlPart')" for="pipeline-urlPart"></label>  <help-tag role="Admin" target="pipeline.url-part"/>
                        <input type="text" class="form-control" name="urlPart" id="pipeline-urlPart"
                            :class="{'valid': !$v.pipeline.urlPart.$invalid, 'invalid': $v.pipeline.urlPart.$invalid }" v-model="$v.pipeline.urlPart.$model" />

                        <label v-if="$v.pipeline.type.$model === 'ACME'" class="form-check-inline" name="pipeline-acme-url" id="pipeline-acme-url">ACME-URL: https://SERVER/acme/{{encodeURIComponent($v.pipeline.urlPart.$model)}}/directory</label>
                        <label v-if="$v.pipeline.type.$model === 'SCEP'" class="form-check-inline" name="pipeline-scep-url" id="pipeline-scep-url">SCEP-URL: http://SERVER/scep/{{encodeURIComponent($v.pipeline.urlPart.$model)}}</label>

                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.description')" for="pipeline-description"></label>  <help-tag role="Admin" target="pipeline.description"/>
                        <textarea type="text" class="form-control" name="description" id="pipeline-description"
                            :class="{'valid': !$v.pipeline.description.$invalid, 'invalid': $v.pipeline.description.$invalid }" v-model="$v.pipeline.description.$model" />
                    </div>

                    <div class="form-group" v-if="$v.pipeline.type.$model === 'WEB'">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.listOrder')" for="pipeline-listOrder"></label>  <help-tag role="Admin" target="pipeline.list-order"/>
                        <input type="number" class="form-control" name="listOrder" id="pipeline-listOrder"
                               :class="{'valid': !$v.pipeline.listOrder.$invalid, 'invalid': $v.pipeline.listOrder.$invalid }" v-model="$v.pipeline.listOrder.$model" />
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.caConnector')" for="pipeline-caConnector"></label>  <help-tag role="Admin" target="pipeline.ca-connector"/> <a href="" >{{pipeline.caConnectorName}} </a>
                        <select class="form-control" id="pipeline-caConnector" name="caConnector"
                                :class="{'valid': !$v.pipeline.caConnectorName.$invalid, 'invalid': $v.pipeline.caConnectorName.$invalid }"
                                v-model="pipeline.caConnectorName">
                          <option v-bind:value="null"></option>
                          <!--option v-bind:value="pipeline.caConnectorName && cAConnectorConfigOption.name === pipeline.caConnectorName ? pipeline.caConnectorName : cAConnectorConfigOption.name" -->
                          <option v-bind:value="cAConnectorConfigOption.name"
                                  v-for="cAConnectorConfigOption in allCertGenerators"
                                  :key="cAConnectorConfigOption.id">{{cAConnectorConfigOption.name}}</option>
                        </select>
                        <div v-if="$v.pipeline.caConnectorName.$anyDirty && $v.pipeline.caConnectorName.$invalid">
                            <small class="form-text text-danger" v-if="$v.pipeline.caConnectorName.required" v-text="$t('entity.validation.required')"></small>
                        </div>
                    </div>

                    <div class="form-group" v-if="$v.pipeline.type.$model === 'SCEP'">

                        <div>
                            <label class="form-control-label" v-text="$t('ca3SApp.pipeline.scepRenewalAllowed')" for="pipeline-scepRenewalAllowed"></label>
                            <input type="checkbox" class="form-check-inline" name="pipeline-scepRenewalAllowed" id="pipeline-scepRenewalAllowed" v-model="$v.pipeline.scepConfigItems.capabilityRenewal.$model" />
                        </div>

                        <div v-if="$v.pipeline.scepConfigItems.capabilityRenewal.$model">
                            <label v-if="$v.pipeline.scepConfigItems.capabilityRenewal.$model" class="form-control-label" v-text="$t('ca3SApp.pipeline.periodDaysRenewal')" for="pipeline-periodDaysRenewal"></label>
                            <input v-if="$v.pipeline.scepConfigItems.capabilityRenewal.$model" type="text" class="form-control" name="pipeline-periodDaysRenewal" id="pipeline-periodDaysRenewal"
                                   :class="{'valid': !$v.pipeline.scepConfigItems.periodDaysRenewal.$invalid, 'invalid': $v.pipeline.scepConfigItems.periodDaysRenewal.$invalid }"
                                   v-model="$v.pipeline.scepConfigItems.periodDaysRenewal.$model" />

                            <label v-if="$v.pipeline.scepConfigItems.capabilityRenewal.$model" class="form-control-label" v-text="$t('ca3SApp.pipeline.percentageOfValidtyBeforeRenewal')" for="pipeline-percentageOfValidtyBeforeRenewal"></label>
                            <input v-if="$v.pipeline.scepConfigItems.capabilityRenewal.$model" type="text" class="form-control" name="pipeline-percentageOfValidtyBeforeRenewal" id="pipeline-percentageOfValidtyBeforeRenewal"
                                   :class="{'valid': !$v.pipeline.scepConfigItems.percentageOfValidtyBeforeRenewal.$invalid, 'invalid': $v.pipeline.scepConfigItems.percentageOfValidtyBeforeRenewal.$invalid }"
                                   v-model="$v.pipeline.scepConfigItems.percentageOfValidtyBeforeRenewal.$model" />
                        </div>


                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.scepSecret')" for="pipeline-scepSecret"></label>  <help-tag role="Admin" target="pipeline.scep.secret"/>
                        <input type="text" class="form-control" name="scepSecret" id="pipeline-scepSecret"
                               :class="{'valid': !$v.pipeline.scepConfigItems.scepSecret.$invalid, 'invalid': $v.pipeline.scepConfigItems.scepSecret.$invalid }" v-model="$v.pipeline.scepConfigItems.scepSecret.$model" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.scepSecretValidTo')" for="pipeline-scepSecretValidTo"></label>  <help-tag role="Admin" target="pipeline.scep.secret-valid-to"/>
                        <datetime type="datetime" input-class="form-control" name="scepSecretValidTo" input-id="pipeline-scepSecretValidTo" id="pipeline-scepSecretValidTo"
                                  v-model="$v.pipeline.scepConfigItems.scepSecretValidTo.$model"/>

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.scepRecipientDN')" for="pipeline-scepRecipientDN"></label>  <help-tag role="Admin" target="pipeline.scep.recipient-dn"/>
                        <input type="text" class="form-control" name="scepRecipientDN" id="pipeline-scepRecipientDN" v-model="$v.pipeline.scepConfigItems.scepRecipientDN.$model" />

                        <div class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.pipeline.caConnectorScepRecipientName')" for="pipeline-caConnectorScepRecipientName"></label>  <help-tag role="Admin" target="pipeline.scep.ca-connector-recipient"/>
                            <select class="form-control" id="pipeline-caConnectorScepRecipientName" name="caConnector" v-model="pipeline.scepConfigItems.caConnectorRecipientName">
                                <option v-bind:value="pipeline.scepConfigItems.caConnectorRecipientName && cAConnectorConfigOption.name === pipeline.scepConfigItems.caConnectorRecipientName ? pipeline.scepConfigItems.caConnectorRecipientName : cAConnectorConfigOption.name" v-for="cAConnectorConfigOption in allCertGenerators" :key="cAConnectorConfigOption.id">{{cAConnectorConfigOption.name}}</option>
                            </select>
                        </div>

                        <label v-if="$v.pipeline.scepConfigItems.recepientCertId.$model"
                               class="form-control-label"
                               v-text="$t('ca3SApp.pipeline.scepRecipientCertificate')" for="pipeline-scepRecipientCertificate"></label>

                        <div v-if="$v.pipeline.scepConfigItems.recepientCertId.$model">
                            <router-link name="pipeline-scepRecipientCertificate"
                                         id="pipeline-scepRecipientCertificate"
                                         :to="{name: 'CertInfo', params: {certificateId: $v.pipeline.scepConfigItems.recepientCertId.$model}}">{{$v.pipeline.scepConfigItems.recepientCertSubject.$model}}</router-link>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.key.unique')" for="pipeline-key-unique"></label>  <help-tag role="Admin" target="pipeline.key.unique"/>
                        <select class="form-control" id="pipeline-key-unique" name="pipeline-key-unique" v-model="pipeline.keyUniqueness">
                            <option v-text="$t('ca3SApp.pipeline.key.reuse.unique')" value="KEY_UNIQUE"></option>
                            <option v-text="$t('ca3SApp.pipeline.key.reuse.domainReuse')" value="DOMAIN_REUSE"></option>
                            <option v-text="$t('ca3SApp.pipeline.key.reuse.reuse')" value="KEY_REUSE"></option>
                            <option v-if="$v.pipeline.type.$model === 'ACME'" v-text="$t('ca3SApp.pipeline.key.reuse.domainReuseWithWarn')" value="DOMAIN_REUSE_WARN_ONLY"></option>
                            <option v-if="$v.pipeline.type.$model === 'ACME'" v-text="$t('ca3SApp.pipeline.key.reuse.reuseWithWarn')" value="KEY_REUSE_WARN_ONLY"></option>
                        </select>
                    </div>

                    <table class="table">
                        <thead class="thead-light">
                        <tr>
                            <th></th>
                            <th>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.cardinality')"></label>
                                <help-tag role="Admin" target="pipeline.dn-cardinality"/>
                            </th>
                            <th>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')"></label>
                                <help-tag role="Admin" target="pipeline.template"/>
                            </th>
                            <th>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')"></label>
                                <help-tag role="Admin" target="pipeline.regex"/>
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        <tr>
                            <td>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.cn')" for="pipeline-cn-cardinality"></label>
                            </td>
                            <td>
                                <select class="form-control" id="pipeline-cn-cardinality" name="pipeline-cn-cardinality" v-model="pipeline.restriction_CN.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE_OR_SAN">ONE_OR_SAN</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" class="form-control" name="pipeline-cn-template" id="pipeline-cn-template" v-model="pipeline.restriction_CN.contentTemplate" />
                            </td>
                            <td>
                                <input type="checkbox" class="form-check-inline" name="pipeline-cn-regExMatch" id="pipeline-cn-regExMatch" v-model="pipeline.restriction_CN.regExMatch" />
                                <input type="text" class="form-control" name="pipeline-cn-regex" id="pipeline-cn-regex" v-model="pipeline.restriction_CN.regEx" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.c')" for="pipeline-c-cardinality"></label>
                            </td>
                            <td>
                                <select class="form-control" id="pipeline-c-cardinality" name="pipeline-c-cardinality" v-model="pipeline.restriction_C.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" class="form-control" name="pipeline-c-template" id="pipeline-c-template" v-model="pipeline.restriction_C.contentTemplate" />
                            </td>
                            <td>
                                <input type="checkbox" class="form-check-inline" name="pipeline-c-regExMatch" id="pipeline-c-regExMatch" v-model="pipeline.restriction_C.regExMatch" />
                                <input type="text" class="form-control" name="pipeline-c-regex" id="pipeline-c-regex" v-model="pipeline.restriction_C.regEx" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.o')" for="pipeline-o-cardinality"></label>
                            </td>
                            <td>
                                <select class="form-control" id="pipeline-o-cardinality" name="pipeline-o-cardinality" v-model="pipeline.restriction_O.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" class="form-control" name="pipeline-o-template" id="pipeline-o-template" v-model="pipeline.restriction_O.contentTemplate" />
                            </td>
                            <td>
                                <input type="checkbox" class="form-check-inline" name="pipeline-o-regExMatch" id="pipeline-o-regExMatch" v-model="pipeline.restriction_O.regExMatch" />
                                <input type="text" class="form-control" name="pipeline-o-regex" id="pipeline-o-regex" v-model="pipeline.restriction_O.regEx" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ou')" for="pipeline-ou-cardinality"></label>
                            </td>
                            <td>
                                <select class="form-control" id="pipeline-ou-cardinality" name="pipeline-ou-cardinality" v-model="pipeline.restriction_OU.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" class="form-control" name="pipeline-cn-template" id="pipeline-cn-template" v-model="pipeline.restriction_CN.contentTemplate" />
                            </td>
                            <td>
                                <input type="checkbox" class="form-check-inline" name="pipeline-ou-regExMatch" id="pipeline-ou-regExMatch" v-model="pipeline.restriction_OU.regExMatch" />
                                <input type="text" class="form-control" name="pipeline-ou-regex" id="pipeline-ou-regex" v-model="pipeline.restriction_OU.regEx" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.s')" for="pipeline-s-cardinality"></label>
                            </td>
                            <td>
                                <select class="form-control" id="pipeline-s-cardinality" name="pipeline-s-cardinality" v-model="pipeline.restriction_S.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" class="form-control" name="pipeline-s-template" id="pipeline-s-template" v-model="pipeline.restriction_S.contentTemplate" />
                            </td>
                            <td>
                                <input type="checkbox" class="form-check-inline" name="pipeline-s-regExMatch" id="pipeline-s-regExMatch" v-model="pipeline.restriction_S.regExMatch" />
                                <input type="text" class="form-control" name="pipeline-s-regex" id="pipeline-s-regex" v-model="pipeline.restriction_S.regEx" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.l')" for="pipeline-l-cardinality"></label>
                            </td>
                            <td>
                                <select class="form-control" id="pipeline-l-cardinality" name="pipeline-l-cardinality" v-model="pipeline.restriction_L.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" class="form-control" name="pipeline-l-template" id="pipeline-l-template" v-model="pipeline.restriction_L.contentTemplate" />
                            </td>
                            <td>
                                <input type="checkbox" class="form-check-inline" name="pipeline-l-regExMatch" id="pipeline-l-regExMatch" v-model="pipeline.restriction_L.regExMatch" />
                                <input type="text" class="form-control" name="pipeline-l-regex" id="pipeline-l-regex" v-model="pipeline.restriction_L.regEx" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.e')" for="pipeline-e-cardinality"></label>
                            </td>
                            <td>
                                <select class="form-control" id="pipeline-e-cardinality" name="pipeline-e-cardinality" v-model="pipeline.restriction_E.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" class="form-control" name="pipeline-e-template" id="pipeline-e-template" v-model="pipeline.restriction_E.contentTemplate" />
                            </td>
                            <td>
                                <input type="checkbox" class="form-check-inline" name="pipeline-e-regExMatch" id="pipeline-e-regExMatch" v-model="pipeline.restriction_E.regExMatch" />
                                <input type="text" class="form-control" name="pipeline-e-regex" id="pipeline-e-regex" v-model="pipeline.restriction_E.regEx" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.san')" for="pipeline-san-cardinality"></label>  <help-tag role="Admin" target="pipeline.san.restrictions"/>
                            </td>
                            <td>
                                <select class="form-control" id="pipeline-san-cardinality" name="pipeline-san-cardinality" v-model="pipeline.restriction_SAN.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" class="form-control" name="pipeline-san-template" id="pipeline-san-template" v-model="pipeline.restriction_SAN.contentTemplate" />
                            </td>
                            <td>
                                <input type="checkbox" class="form-check-inline" name="pipeline-san-regExMatch" id="pipeline-san-regExMatch" v-model="pipeline.restriction_SAN.regExMatch" />
                                <input type="text" class="form-control" name="pipeline-san-regex" id="pipeline-san-regex" v-model="pipeline.restriction_SAN.regEx" />
                            </td>
                        </tr>
                        </tbody>
                    </table>

                    <div v-if="$v.pipeline.type.$model === 'ACME'" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.acme.contact.email.regex')" for="pipeline-contact-email-regex"></label>  <help-tag role="Admin" target="pipeline.acmeAccountEmailRegex"/>
                        <input type="text" class="form-check-inline" name="pipeline-contact-email-regex" id="pipeline-contact-email-regex" v-model="pipeline.acmeConfigItems.contactEMailRegEx" />
                        <input type="text" class="form-check-inline" name="pipeline-contact-email-reject-regex" id="pipeline-contact-email-reject-regex" v-model="pipeline.acmeConfigItems.contactEMailRejectRegEx" />
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ipAsSubjectAllowed')" for="pipeline-ipAsSubjectAllowed"></label>  <help-tag role="Admin" target="pipeline.ip-as-subject"/>
                        <input type="checkbox" class="form-check-inline" name="ipAsSubjectAllowed" id="pipeline-ipAsSubjectAllowed"
                               :class="{'valid': !$v.pipeline.ipAsSubjectAllowed.$invalid, 'invalid': $v.pipeline.ipAsSubjectAllowed.$invalid }" v-model="$v.pipeline.ipAsSubjectAllowed.$model" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ipAsSanAllowed')" for="pipeline-ipAsSanAllowed"></label>  <help-tag role="Admin" target="pipeline.ip-as-san"/>
                        <input type="checkbox" class="form-check-inline" name="ipAsSanAllowed" id="pipeline-ipAsSanAllowed"
                               :class="{'valid': !$v.pipeline.ipAsSANAllowed.$invalid, 'invalid': $v.pipeline.ipAsSANAllowed.$invalid }" v-model="$v.pipeline.ipAsSANAllowed.$model" />
                    </div>

                    <!-- Additional Request Attributes -->
                    <table class="table">
                        <thead class="thead-light">
                            <tr>
                                <th>
                                    <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ara.name')"
                                           for="pipeline-ara-name"></label>
                                    <help-tag role="Admin" target="pipeline.ara.restrictions"/>
                                </th>
                                <th>
                                    <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')"
                                           for="pipeline-ara-template"></label>
                                </th>
                                <th>
                                    <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')"
                                           for="pipeline-ara-regExMatch"></label>
                                </th>
                                <th>
                                    <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ara.content.type')"
                                           for="pipeline-ara-content-type"></label>
                                </th>
                                <th>
                                    <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ara.comment')"
                                           for="pipeline-ara-comment"></label>
                                </th>
                                <th>
                                    <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ara.required')"
                                           for="pipeline-ara-required"></label>
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in pipeline.araRestrictions" :key="index">
                                <td>
                                    <input type="text" class="form-control-inline" name="pipeline-ara-name"
                                           id="pipeline-ara-name" v-model="pipeline.araRestrictions[index].name"
                                           v-on:input="alignARAArraySize(index)"/>
                                </td>
                                <td>
                                    <input type="text" class="form-control-inline" name="pipeline-ara-template"
                                           id="pipeline-ara-template"
                                           v-model="pipeline.araRestrictions[index].contentTemplate"/>
                                </td>
                                <td>
                                    <input type="checkbox" class="form-check-inline" name="pipeline-ara-regExMatch"
                                           id="pipeline-ara-regExMatch"
                                           v-model="pipeline.araRestrictions[index].regExMatch"/>
                                    <input type="text" class="form-control-inline" name="pipeline-ara-regex"
                                           id="pipeline-ara-regex" v-model="pipeline.araRestrictions[index].regEx"/>
                                </td>
                                <td>
                                    <select class="form-control-inline" id="pipeline-ara-content-type"
                                            name="pipeline-ara-content-type"
                                            v-model="pipeline.araRestrictions[index].contentType">
                                        <option value="NO_TYPE"></option>
                                        <option value="EMAIL_ADDRESS">EMAIL_ADDRESS</option>
                                    </select>
                                </td>
                                <td>
                                    <input type="text" class="form-check-inline" name="pipeline-ara-comment"
                                           id="pipeline-ara-comment" v-model="pipeline.araRestrictions[index].comment"/>
                                </td>
                                <td>
                                    <input type="checkbox" class="form-check-inline" name="pipeline-ara-required"
                                           id="pipeline-ara-required" v-model="pipeline.araRestrictions[index].required"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <div class="form-group" v-if="$v.pipeline.type.$model === 'WEB' || $v.pipeline.type.$model === 'ACME'">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.tosAgreed')" for="pipeline-tosAgreed"></label>  <help-tag role="Admin" target="pipeline.tosAgreed"/>
                        <input type="checkbox" class="form-check-inline" name="pipeline-tosAgreementRequired" id="pipeline-tosAgreementRequired" v-model="pipeline.tosAgreementRequired" />
                        <input v-if="pipeline.tosAgreementRequired"
                               type="text" class="form-control" name="tosAgreementLink" id="pipeline-tosAgreementLink" v-model="$v.pipeline.tosAgreementLink.$model" />
                    </div>


                    <div v-if="$v.pipeline.type.$model === 'ACME' || $v.pipeline.type.$model === 'SCEP'" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.toPendingOnFailedRestrictions')" for="^pipeline-toPendingOnFailedRestrictions"></label>  <help-tag role="Admin" target="pipeline.pending-on-failure"/>
                        <input type="checkbox" class="form-check-inline" name="toPendingOnFailedRestrictions" id="pipeline-toPendingOnFailedRestrictions" v-model="pipeline.toPendingOnFailedRestrictions" />
                    </div>


<!--
                    <div class="form-group" v-if="$v.pipeline.approvalRequired.$model">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.approvalInfo')" for="pipeline-approvalInfo1">Approval Info</label>

                        <input type="text" class="form-control" name="urlPart" id="pipeline-approvalInfo1"
                            :class="{'valid': !$v.pipeline.approvalInfo1.$invalid, 'invalid': $v.pipeline.approvalInfo1.$invalid }" v-model="$v.pipeline.approvalInfo1.$model" />
                    </div>
-->

                    <div v-if="$v.pipeline.type.$model === 'ACME'" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.allowChallengeHTTP01')" for="pipeline-allowChallengeHTTP01"></label>  <help-tag role="Admin" target="pipeline.acme.allow-challenge-http01"/>
                        <input type="checkbox" class="form-check-inline" name="allowChallengeHTTP01" id="pipeline-allowChallengeHTTP01" v-model="pipeline.acmeConfigItems.allowChallengeHTTP01" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.allowChallengeAlpn')" for="pipeline-allowChallengeAlpn"></label>  <help-tag role="Admin" target="pipeline.acme.allow-challenge-alpn"/>
                        <input type="checkbox" class="form-check-inline" name="allowChallengeAlpn" id="pipeline-allowChallengeAlpn" v-model="pipeline.acmeConfigItems.allowChallengeAlpn" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.allowChallengeDNS')" for="pipeline-allowChallengeDNS"></label>  <help-tag role="Admin" target="pipeline.acme.allow-challenge-dns"/>
                        <input type="checkbox" class="form-check-inline" name="allowChallengeDNS" id="pipeline-allowChallengeDNS" v-model="pipeline.acmeConfigItems.allowChallengeDNS" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.allowWildcards')" for="pipeline-allowWildcards"></label>  <help-tag role="Admin" target="pipeline.acme.allow-wildcards"/>
                        <input type="checkbox" class="form-check-inline" :disabled="pipeline.acmeConfigItems.allowChallengeDNS === false" name="allowWildcards" id="pipeline-allowWildcards" v-model="pipeline.acmeConfigItems.allowWildcards" />

                    </div>
                    <div v-if="$v.pipeline.type.$model === 'ACME'" class="form-inline">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.eabRequired')" for="pipeline-eabRequired"></label>  <help-tag role="Admin" target="pipeline.acme.eabRequired"/>
                        <input type="checkbox" class="form-check-inline" name="pipeline-eabRequired" id="pipeline-eabRequired" v-model="pipeline.acmeConfigItems.externalAccountRequired" />
                    </div>

                    <div v-if="$v.pipeline.type.$model === 'ACME'" class="form-inline">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.checkCAA')" for="pipeline-checkCAA"></label>  <help-tag role="Admin" target="pipeline.acme.check-caa"/>
                        <input type="checkbox" class="form-check-inline" name="pipeline-checkCAA" id="pipeline-checkCAA" v-model="pipeline.acmeConfigItems.checkCAA" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.caNameCAA')" for="pipeline-caNameCAA"></label>  <help-tag role="Admin" target="pipeline.acme.ca-name-caa"/>
                        <input type="text" class="form-control" name="caNameCAA" id="pipeline-caNameCAA" v-model="pipeline.acmeConfigItems.caNameCAA" />
                    </div>

                       <div v-if="($v.pipeline.type.$model === 'ACME') && (requestProxyConfigs.length > 0 )" class="form-inline">

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.requestProxies')"
                               for="pipeline-requestProxyIds"></label>  <help-tag role="Admin" target="pipeline.acme.request-proxies"/>
                        <select class="form-control" id="pipeline-requestProxyIds" name="pipeline-requestProxyIds"
                                multiple
                                v-model="pipeline.requestProxyConfigIds">
                            <option v-for="item in requestProxyConfigs" :key="item.id" :value="item.id">{{item.name}}</option>
                        </select>
                    </div>

                    <div class="container" v-if="$v.pipeline.type.$model === 'WEB'">
                        <div class="row" >
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.csr.usage')" for="pipeline-csrUsage"></label>  <help-tag role="Admin" target="pipeline.csr-usage"/>
                            </div>
                            <div class="col">
                                <select class="form-control" id="pipeline-csrUsage" name="pipeline-csrUsage" v-model="pipeline.csrUsage">
                                    <option value="TLS_SERVER">TLS Server</option>
                                    <option value="TLS_CLIENT">TLS Client</option>
                                    <option value="DOC_SIGNING">Document Signing</option>
                                    <option value="CODE_SIGNING">Code Signing</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="container" v-if="pipeline.domainRaOfficerList && domainRAs && domainRAs.length > 0">
                        <div class="row" >
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.domainRAs')" for="pipeline-domainRAs"></label>  <help-tag role="Admin" target="pipeline.domain-ra"/>
                            </div>
                            <div class="col">
                                <select class="form-control" multiple id="pipeline-domainRAs" name="pipeline-domainRAs" v-model="pipeline.domainRaOfficerList">
                                    <option v-bind:value="domainRA.id" v-for="domainRA in domainRAs" :key="domainRA.id">{{readableUserName(domainRA)}}</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="form-group" v-if="$v.pipeline.type.$model === 'WEB'">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.notifyRAOnCertificateLifecycleEvents')" for="pipeline-notifyRAOnCertificateLifecycleEvents"></label>  <help-tag role="Admin" target="pipeline.notify-ra-on-pending-request"/>
                        <input type="checkbox" class="form-check-inline" name="notifyRAOnCertificateLifecycleEvents" id="pipeline-notifyRAOnCertificateLifecycleEvents" v-model="$v.pipeline.webConfigItems.notifyRAOfficerOnPendingRequest.$model" />
                    </div>

                    <div class="form-group" v-if="$v.pipeline.type.$model === 'WEB' && pipeline.domainRaOfficerList && (pipeline.domainRaOfficerList.length > 0) ">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.notifyDomainRAOnCertificateLifecycleEvents')" for="pipeline-notifyDomainRAOnCertificateLifecycleEvents"></label>
                        <input type="checkbox" class="form-check-inline" name="notifyDomainRAOnCertificateLifecycleEvents" id="pipeline-notifyDomainRAOnCertificateLifecycleEvents" v-model="$v.pipeline.webConfigItems.notifyDomainRAOfficerOnPendingRequest.$model" />
                    </div>


                    <div class="container" v-if="$v.pipeline.type.$model === 'WEB'">
                        <div class="row" >
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.additionalEmailRecipients')" for="pipeline-additionalEmailRecipients"></label>  <help-tag role="Admin" target="pipeline.additional-email-recipients"/>
                            </div>
                            <div class="col">
                                <input type="text" class="form-control" name="additionalEmailRecipients" id="pipeline-additionalEmailRecipients" v-model="$v.pipeline.webConfigItems.additionalEMailRecipients.$model" />
                            </div>
                        </div>
                    </div>

                    <div class="form-group" v-if="getBPNMProcessInfosByType('CERTIFICATE_CREATION').length > 0">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.processInfoCreate')" for="pipeline-processInfo"></label>  <help-tag role="Admin" target="pipeline.process.create"/>
                        <select class="form-control" id="pipeline-processInfo" name="processInfo" v-model="pipeline.processInfoNameCreate">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.processInfo && bPNMProcessInfoOption.id === pipeline.processInfo.id ? pipeline.processInfo.name : bPNMProcessInfoOption.name" v-for="bPNMProcessInfoOption in getBPNMProcessInfosByType('CERTIFICATE_CREATION')" :key="bPNMProcessInfoOption.id">{{bPNMProcessInfoOption.name}}</option>
                        </select>
                    </div>

                    <div class="form-group" v-if="getBPNMProcessInfosByType('CERTIFICATE_REVOCATION').length > 0">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.processInfoRevoke')" for="pipeline-processInfo"></label>  <help-tag role="Admin" target="pipeline.process.revoke"/>
                        <select class="form-control" id="pipeline-processInfo" name="processInfo" v-model="pipeline.processInfoNameRevoke">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.processInfo && bPNMProcessInfoOption.id === pipeline.processInfo.id ? pipeline.processInfo.name : bPNMProcessInfoOption.name" v-for="bPNMProcessInfoOption in getBPNMProcessInfosByType('CERTIFICATE_REVOCATION')" :key="bPNMProcessInfoOption.id">{{bPNMProcessInfoOption.name}}</option>
                        </select>
                    </div>

                    <div class="form-group" v-if="$v.pipeline.type.$model === 'WEB' && getBPNMProcessInfosByType('REQUEST_AUTHORIZATION').length > 0">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.processInfoRequestAuthorization')" for="pipeline-processInfoRequestAuthorization"></label>  <help-tag role="Admin" target="pipeline.process.requestAuthorization"/>
                        <select class="form-control" id="pipeline-processInfoRequestAuthorization" name="processInfoRequestAuthorization" v-model="pipeline.webConfigItems.processInfoNameRequestAuthorization">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.processInfo && bPNMProcessInfoOption.id === pipeline.processInfo.id ? pipeline.processInfo.name : bPNMProcessInfoOption.name" v-for="bPNMProcessInfoOption in getBPNMProcessInfosByType('REQUEST_AUTHORIZATION')" :key="bPNMProcessInfoOption.id">{{bPNMProcessInfoOption.name}}</option>
                        </select>
                    </div>

                    <div class="form-group" v-if="getBPNMProcessInfosByType('CERTIFICATE_NOTIFY').length > 0">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.processInfoNotify')" for="pipeline-processInfoNotify"></label>  <help-tag role="Admin" target="pipeline.process.notify"/>
                        <select class="form-control" id="pipeline-processInfoNotify" name="processInfoNotify" v-model="pipeline.processInfoNameNotify">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.processInfo && bPNMProcessInfoOption.id === pipeline.processInfo.id ? pipeline.processInfo.name : bPNMProcessInfoOption.name" v-for="bPNMProcessInfoOption in getBPNMProcessInfosByType('CERTIFICATE_NOTIFY')" :key="bPNMProcessInfoOption.id">{{bPNMProcessInfoOption.name}}</option>
                        </select>
                    </div>

                    <div class="form-group" v-if="$v.pipeline.type.$model === 'ACME' && getBPNMProcessInfosByType('ACME_ACCOUNT_AUTHORIZATION').length > 0">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.processInfoAccountAuthorization')" for="pipeline-processInfoAccountAuthorization"></label>  <help-tag role="Admin" target="pipeline.process.account-authorization"/>
                        <select class="form-control" id="pipeline-processInfoNotify" name="processInfoNotify" v-model="pipeline.acmeConfigItems.processInfoNameAccountAuthorization">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.processInfo && bPNMProcessInfoOption.id === pipeline.processInfo.id ? pipeline.processInfo.name : bPNMProcessInfoOption.name" v-for="bPNMProcessInfoOption in getBPNMProcessInfosByType('ACME_ACCOUNT_AUTHORIZATION')" :key="bPNMProcessInfoOption.id">{{bPNMProcessInfoOption.name}}</option>
                        </select>
                    </div>

                    <div v-if="$v.pipeline.type.$model === 'ACME'" class="form-inline">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.isNotifyContactsOnError')" for="pipeline-isNotifyContactsOnError"></label>  <help-tag role="Admin" target="pipeline.acme.notify-contacts-on-error"/>
                        <input type="checkbox" class="form-check-inline" name="checkCAA" id="pipeline-isNotifyContactsOnError" v-model="pipeline.acmeConfigItems.notifyContactsOnError" />
                    </div>

                    <div class="form-group" v-if="$v.pipeline.type.$model === 'WEB' && pipeline.selectedTenantList">
                        <label v-text="$t('ca3SApp.pipeline.tenants')"></label> <help-tag role="Admin" target="pipeline.tenants"/>
                        <select class="form-control" multiple name="selectedTenants" v-model="pipeline.selectedTenantList">
                            <option v-for="tenant of tenants" :value="tenant" :key="tenant.id">{{ tenant.longname }}</option>
                        </select>
                    </div>
                </div>
                <p></p>
                <div v-if="$v.pipeline.type.$model === 'ACME'">
                  <div v-if="networkCollapsed">
                    <button type="button" class="addRemoveSelector" v-on:click="setNetworkCollapsed(false)">
                      <font-awesome-icon icon="plus"></font-awesome-icon>
                    </button>
                    <b v-text="$t('ca3SApp.pipeline.network')"></b>
                    <p />
                  </div>
                  <div v-if="!networkCollapsed">
                    <button type="button" class="addRemoveSelector" v-on:click="setNetworkCollapsed(true)">
                      <font-awesome-icon icon="minus"></font-awesome-icon>
                    </button>
                    <b v-text="$t('ca3SApp.pipeline.network')"></b>
                  </div>

                  <div v-if="!networkCollapsed">
                    <div class="form-group">
                      <label class="form-control-label" v-text="$t('ca3SApp.pipeline.network.accept')"
                             for="pipeline-network-accept"></label>
                      <div v-for="(item, index) in $v.pipeline.networkAcceptArr.$model" :key="index">
                        <input type="text" class="form-control" name="pipeline-network-accept"
                               id="pipeline-network-accept"
                               v-model="$v.pipeline.networkAcceptArr.$model[index]"
                               v-on:input="alignNetworkAcceptArraySize(index)" />
                        <small v-if="$v.pipeline.networkAcceptArr.$each[index].$invalid"
                               class="form-text text-danger"
                               v-text="$t('entity.validation.subnetRequired')"></small>
                      </div>
                    </div>
                  </div>
                  <div v-if="!networkCollapsed">
                    <div class="form-group">
                      <label class="form-control-label" v-text="$t('ca3SApp.pipeline.network.reject')"
                             for="pipeline-network-reject"></label>
                      <div v-for="(item, index) in $v.pipeline.networkRejectArr.$model" :key="index">
                        <input type="text" class="form-control" name="pipeline-network-reject"
                               id="pipeline-network-reject"
                               v-model="$v.pipeline.networkRejectArr.$model[index]"
                               v-on:input="alignNetworkRejectArraySize(index)" />
                        <small v-if="$v.pipeline.networkRejectArr.$each[index].$invalid"
                               class="form-text text-danger"
                               v-text="$t('entity.validation.subnetRequired')"></small>
                      </div>
                    </div>
                  </div>
                </div>
              <p></p>
                <div v-if="pipeline.id">
                    <audit-tag :pipelineId="pipeline.id" showLinks="false" :title="$t('ca3SApp.certificate.audit')"></audit-tag>
                </div>

                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')"></span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.pipeline.$invalid" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./pipeline-update.component.ts">
</script>

<style>
.container {
	padding-left: 0;
	margin-left: 0;
}
.row {
	padding-top: 5px;
	padding-bottom: 5px;
}
</style>
