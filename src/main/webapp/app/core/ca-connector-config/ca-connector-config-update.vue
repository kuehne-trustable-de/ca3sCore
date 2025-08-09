<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.cAConnectorConfig.home.createOrEditLabel" v-text="$t('ca3SApp.cAConnectorConfig.home.createOrEditLabel')"></h2>
                <div>
                    <!--div class="form-group" v-if="cAConnectorConfig.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="cAConnectorConfig.id" readonly />
                    </div-->

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.name')" for="ca-connector-config-name"></label>  <help-tag role="Admin" target="ca-connector.name"/>
                        <input type="text" class="form-control" name="name" id="ca-connector-config-name"
                            :class="{'valid': !$v.cAConnectorConfig.name.$invalid, 'invalid': $v.cAConnectorConfig.name.$invalid }" v-model="$v.cAConnectorConfig.name.$model"  required/>
                        <div v-if="$v.cAConnectorConfig.name.$anyDirty && $v.cAConnectorConfig.name.$invalid">
                            <small class="form-text text-danger" v-if="$v.cAConnectorConfig.name.required" v-text="$t('entity.validation.required')"></small>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.caConnectorType')" for="ca-connector-config-caConnectorType"></label>  <help-tag role="Admin" target="ca-connector.type"/>
                        <select class="form-control" name="caConnectorType"
                                :class="{'valid': !$v.cAConnectorConfig.caConnectorType.$invalid, 'invalid': $v.cAConnectorConfig.caConnectorType.$invalid }"
                                v-model="$v.cAConnectorConfig.caConnectorType.$model"
                                id="ca-connector-config-caConnectorType"  required>
                            <option value="INTERNAL" v-bind:label="$t('ca3SApp.CAConnectorType.INTERNAL')"></option>
                            <option value="CMP" v-bind:label="$t('ca3SApp.CAConnectorType.CMP')"></option>
                            <option value="ADCS" v-bind:label="$t('ca3SApp.CAConnectorType.ADCS')"></option>
                            <option value="ADCS_CERTIFICATE_INVENTORY" v-bind:label="$t('ca3SApp.CAConnectorType.ADCS_CERTIFICATE_INVENTORY')"></option>
                            <option value="DIRECTORY" v-bind:label="$t('ca3SApp.CAConnectorType.DIRECTORY')"></option>
                            <!--option value="VAULT" v-bind:label="$t('ca3SApp.CAConnectorType.VAULT')">VAULT</option-->
                            <option value="EJBCA_INVENTORY" v-bind:label="$t('ca3SApp.CAConnectorType.EJBCA_INVENTORY')"></option>
                        </select>

                        <div v-if="$v.cAConnectorConfig.caConnectorType.$anyDirty && $v.cAConnectorConfig.caConnectorType.$invalid">
                            <small class="form-text text-danger" v-if="$v.cAConnectorConfig.caConnectorType.required" v-text="$t('entity.validation.required')">
                            </small>
                        </div>
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model !== 'INTERNAL'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.caUrl')" for="ca-connector-config-caUrl"></label> <help-tag role="Admin" target="ca-connector.ca-url"/>
                        <input type="text" class="form-control" name="caUrl" id="ca-connector-config-caUrl"
                            :class="{'valid': !$v.cAConnectorConfig.caUrl.$invalid, 'invalid': $v.cAConnectorConfig.caUrl.$invalid }" v-model="$v.cAConnectorConfig.caUrl.$model" />
                    </div>
                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'ADCS_CERTIFICATE_INVENTORY'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.pollingOffset')" for="ca-connector-config-pollingOffset"></label> <help-tag role="Admin" target="ca-connector.polling-offset"/>
                        <input type="number" class="form-control" name="pollingOffset" id="ca-connector-config-pollingOffset"
                               :class="{'valid': !$v.cAConnectorConfig.pollingOffset.$invalid, 'invalid': $v.cAConnectorConfig.pollingOffset.$invalid }" v-model.number="$v.cAConnectorConfig.pollingOffset.$model" />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'EJBCA_INVENTORY'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.lastUpdate')" for="ca-connector-config-lastUpdate"></label> <help-tag role="Admin" target="ca-connector.last-update"/>
                        <input type="date" class="form-control" name="lastUpdate" id="ca-connector-config-lastUpdate"
                               :class="{'valid': !$v.cAConnectorConfig.lastUpdate.$invalid, 'invalid': $v.cAConnectorConfig.lastUpdate.$invalid }" v-model.number="$v.cAConnectorConfig.lastUpdate.$model" />
                    </div>

                    <div class="form-group" v-if="!($v.cAConnectorConfig.caConnectorType.$model === 'DIRECTORY' || $v.cAConnectorConfig.caConnectorType.$model === 'ADCS_CERTIFICATE_INVENTORY' || $v.cAConnectorConfig.caConnectorType.$model === 'EJBCA_INVENTORY')">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.defaultCA')" for="ca-connector-config-defaultCA"></label>  <help-tag role="Admin" target="ca-connector.default-ca"/>
                        <input type="checkbox" class="form-check" name="defaultCA" id="ca-connector-config-defaultCA"
                               :class="{'valid': !$v.cAConnectorConfig.defaultCA.$invalid, 'invalid': $v.cAConnectorConfig.defaultCA.$invalid }" v-model="$v.cAConnectorConfig.defaultCA.$model" />
                    </div>
                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'DIRECTORY' || $v.cAConnectorConfig.caConnectorType.$model === 'EJBCA_INVENTORY'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.trustSelfsignedCertificates')" for="ca-connector-config-trustSelfsignedCertificates"></label>  <help-tag role="Admin" target="ca-connector.trust-self-signed-certificates"/>
                        <input type="checkbox" class="form-check" name="trustSelfsignedCertificates" id="ca-connector-config-trustSelfsignedCertificates"
                               :class="{'valid': !$v.cAConnectorConfig.trustSelfsignedCertificates.$invalid, 'invalid': $v.cAConnectorConfig.trustSelfsignedCertificates.$invalid }" v-model="$v.cAConnectorConfig.trustSelfsignedCertificates.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.active')" for="ca-connector-config-active"></label> <help-tag role="Admin" target="ca-connector.active"/>
                        <input type="checkbox" class="form-check" name="active" id="ca-connector-config-active"
                            :class="{'valid': !$v.cAConnectorConfig.active.$invalid, 'invalid': $v.cAConnectorConfig.active.$invalid }" v-model="$v.cAConnectorConfig.active.$model" />
                    </div>


                    <div class="form-group" v-if="($v.cAConnectorConfig.caConnectorType.$model === 'ADCS' || $v.cAConnectorConfig.caConnectorType.$model === 'ADCS_CERTIFICATE_INVENTORY' ) && hasADCSInstanceDetails() ">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.caname')" for="ca-connector-config-caname"></label>
                        <input type="text" class="form-control" name="caname" id="ca-connector-config-caname" v-model="adcsInstanceDetails.caName" disabled />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'ADCS'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.template')" for="ca-connector-config-selector"></label>  <help-tag role="Admin" target="ca-connector.template"/>

                        <input type="text" v-if="!hasADCSInstanceDetails()" class="form-control" name="selector" id="ca-connector-config-selector"
                               :class="{'valid': !$v.cAConnectorConfig.selector.$invalid, 'invalid': $v.cAConnectorConfig.selector.$invalid }" v-model="$v.cAConnectorConfig.selector.$model" />

                        <select v-if="hasADCSInstanceDetails()" class="form-control" name="ca-connector-config-selector" v-model="$v.cAConnectorConfig.selector.$model" id="ca-connector-config-interval" >
                            <option v-for="templateName in adcsInstanceDetails.templates" :key="templateName" :value="templateName">{{templateName}}</option>
                        </select>

                        <button type="button" id="update-templates" class="btn btn-secondary" v-on:click="initADCSTemplates()">
                            <font-awesome-icon icon="refresh"></font-awesome-icon>
                        </button>

                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'VAULT' || $v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.selector')" for="ca-connector-config-selector"></label>  <help-tag role="Admin" target="ca-connector.selector"/>
                        <input type="text" class="form-control" name="selector" id="ca-connector-config-selector"
                               :class="{'valid': !$v.cAConnectorConfig.selector.$invalid, 'invalid': $v.cAConnectorConfig.selector.$invalid }" v-model="$v.cAConnectorConfig.selector.$model" />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'DIRECTORY' || $v.cAConnectorConfig.caConnectorType.$model === 'ADCS_CERTIFICATE_INVENTORY'|| $v.cAConnectorConfig.caConnectorType.$model === 'EJBCA_INVENTORY' ">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.interval')" for="ca-connector-config-interval"></label>  <help-tag role="Admin" target="ca-connector.interval"/>
                        <select class="form-control" name="interval" :class="{'valid': !$v.cAConnectorConfig.interval.$invalid, 'invalid': $v.cAConnectorConfig.interval.$invalid }" v-model="$v.cAConnectorConfig.interval.$model" id="ca-connector-config-interval" >
                            <option value="MINUTE" v-bind:label="$t('ca3SApp.Interval.MINUTE')"></option>
                            <option value="HOUR" v-bind:label="$t('ca3SApp.Interval.HOUR')"></option>
                            <option value="DAY" v-bind:label="$t('ca3SApp.Interval.DAY')"></option>
                            <option value="WEEK" v-bind:label="$t('ca3SApp.Interval.WEEK')"></option>
                            <option value="MONTH" v-bind:label="$t('ca3SApp.Interval.MONTH')"></option>
                        </select>
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP' || $v.cAConnectorConfig.caConnectorType.$model === 'EJBCA_INVENTORY'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.tlsAuthentication')" for="ca-connector-config-tlsAuthentication"></label> <help-tag role="Admin" target="ca-connector.cmp.tls-client-id"/>
                        <input type="number" class="form-control" name="tlsAuthentication" id="ca-connector-config-tlsAuthentication"
                               :class="{'valid': !$v.cAConnectorConfig.tlsAuthenticationId.$invalid, 'invalid': $v.cAConnectorConfig.tlsAuthenticationId.$invalid }" v-model="$v.cAConnectorConfig.tlsAuthenticationId.$model" />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP' ">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.messageProtectionPassphrase')" for="ca-connector-config-messageProtectionPassphrase"></label> <help-tag role="Admin" target="ca-connector.cmp.message-protection-by-passphrase"/>
                        <input type="checkbox" class="form-check" name="messageProtectionPassphrase" id="ca-connector-config-messageProtectionPassphrase"
                               v-model="cAConnectorConfig.messageProtectionPassphrase"/>
                    </div>

                  <div class="form-group"
                       v-if="$v.cAConnectorConfig.caConnectorType.$model === 'ADCS' || $v.cAConnectorConfig.caConnectorType.$model === 'VAULT' || $v.cAConnectorConfig.caConnectorType.$model === 'ADCS_CERTIFICATE_INVENTORY' || ($v.cAConnectorConfig.caConnectorType.$model === 'CMP' && cAConnectorConfig.messageProtectionPassphrase)">

                    <div class="form-group"v-if="isADCSConnectorConfig()">
                      <label class="form-control-label" v-text="$t('ca3SApp.authenticationSelection.kdfType')"
                             for="authentication-selection-kdf-type"></label>
                      <select class="form-control"
                              name="authentication-selection-kdf-type"
                              id="authentication-selection-kdf-type"
                              v-model="$v.cAConnectorConfig.authenticationParameter.kdfType.$model"
                              v-on:input="buildAdcsConfigSnippet()"
                              required>
                        <option value="PBKDF2" v-bind:label="$t('ca3SApp.authenticationSelection.PBKDF2')"></option>
                      </select>
                    </div>

                    <div class="form-group">
                      <label class="form-control-label" v-text="$t('ca3SApp.authenticationSelection.plainSecret')"
                             for="ca-connector-config-plainSecret"></label>
                      <input type="password" class="form-control" name="ca-connector-config-plainSecret"
                             id="ca-connector-config-plainSecret"
                             v-model="$v.cAConnectorConfig.authenticationParameter.plainSecret.$model"
                             :class="{'valid': !$v.cAConnectorConfig.authenticationParameter.plainSecret.$invalid, 'invalid': $v.cAConnectorConfig.authenticationParameter.plainSecret.$invalid }"
                              v-on:input="buildAdcsConfigSnippet()"
                             required />
                      <small class="form-text text-danger" v-if="!$v.cAConnectorConfig.authenticationParameter.plainSecret.minLength"
                             v-text="$t('ca3SApp.authenticationSelection.plainSecret.minLength')"></small>

                    </div>

                    <div class="form-group" v-if="isADCSConnectorConfig() && $v.cAConnectorConfig.authenticationParameter.kdfType.$model === 'PBKDF2'">
                      <label class="form-control-label" v-text="$t('ca3SApp.authenticationSelection.salt')"
                             for="authentication-selection-salt"></label>
                      <input type="text" class="form-control"
                             :class="{'valid': !$v.cAConnectorConfig.authenticationParameter.salt.$invalid, 'invalid': $v.cAConnectorConfig.authenticationParameter.salt.$invalid }"
                             name="authentication-selection-salt" id="authentication-selection-salt"
                             v-model="$v.cAConnectorConfig.authenticationParameter.salt.$model"
                             v-on:input="buildAdcsConfigSnippet()" />
                      <small class="form-text text-danger" v-if="!$v.cAConnectorConfig.authenticationParameter.salt.minLength"
                             v-text="$t('ca3SApp.authenticationSelection.salt.minLength')"></small>
                    </div>

                    <div class="form-group" v-if="isADCSConnectorConfig() && $v.cAConnectorConfig.authenticationParameter.kdfType.$model === 'PBKDF2'">
                      <label class="form-control-label" v-text="$t('ca3SApp.authenticationSelection.cycles')"
                             for="authentication-selection-cycles"></label>
                      <input type="number" class="form-control"
                             :class="{'valid': !$v.cAConnectorConfig.authenticationParameter.cycles.$invalid, 'invalid': $v.cAConnectorConfig.authenticationParameter.cycles.$invalid }"
                             name="authentication-selection-cycles"
                             id="authentication-selection-cycles"
                             v-model="$v.cAConnectorConfig.authenticationParameter.cycles.$model"
                             v-on:input="buildAdcsConfigSnippet()" />
                      <small class="form-text text-danger" v-if="!$v.cAConnectorConfig.authenticationParameter.cycles.minValue"
                             v-text="$t('ca3SApp.authenticationSelection.cycles.minValue')"></small>
                    </div>


                    <div class="form-group" v-if="isADCSConnectorConfig() && $v.cAConnectorConfig.authenticationParameter.kdfType.$model === 'PBKDF2'">
                      <label class="form-control-label" v-text="$t('ca3SApp.authenticationSelection.apiSalt')"
                             for="authentication-selection-api-salt"></label>
                      <input type="text" class="form-control"
                             :class="{'valid': !$v.cAConnectorConfig.authenticationParameter.apiKeySalt.$invalid, 'invalid': $v.cAConnectorConfig.authenticationParameter.apiKeySalt.$invalid }"
                             name="authentication-selection-api-salt" id="authentication-selection-api-salt"
                             v-model="$v.cAConnectorConfig.authenticationParameter.apiKeySalt.$model"
                             v-on:input="buildAdcsConfigSnippet()" />
                      <small class="form-text text-danger" v-if="!$v.cAConnectorConfig.authenticationParameter.apiKeySalt.minLength"
                             v-text="$t('ca3SApp.authenticationSelection.apiSalt.minLength')"></small>
                    </div>

                    <div class="form-group" v-if="isADCSConnectorConfig() && $v.cAConnectorConfig.authenticationParameter.kdfType.$model === 'PBKDF2'">
                      <label class="form-control-label" v-text="$t('ca3SApp.authenticationSelection.apiCycles')"
                             for="authentication-selection-api-cycles"></label>
                      <input type="number" class="form-control"
                             :class="{'valid': !$v.cAConnectorConfig.authenticationParameter.apiKeyCycles.$invalid, 'invalid': $v.cAConnectorConfig.authenticationParameter.apiKeyCycles.$invalid }"
                             name="authentication-selection-api-cycles"
                             id="authentication-selection-api-cycles"
                             v-model="$v.cAConnectorConfig.authenticationParameter.apiKeyCycles.$model"
                            v-on:input="buildAdcsConfigSnippet()" />
                      <small class="form-text text-danger" v-if="!$v.cAConnectorConfig.authenticationParameter.apiKeyCycles.minValue"
                             v-text="$t('ca3SApp.authenticationSelection.apiCycles.minValue')"></small>
                    </div>

                    <div class="form-group" v-if="isADCSConnectorConfig() && $v.cAConnectorConfig.authenticationParameter.kdfType.$model === 'PBKDF2'">
                            <label class="form-control-label" v-text="$t('ca3SApp.authenticationSelection.configText')"
                              for="authentication-selection-configText"></label> <help-tag target="authentication-selection.cmdline"/>
                            <textarea class="form-control cmd0-content"
                              name="authentication-selection-configText"
                              id="authentication-selection-configText"
                              autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly v-model="adcsConfigSnippet" />
                            <CopyClipboardButton contentElementId="authentication-selection-configText"/>
                    </div>

                  </div>

                  <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP' && !cAConnectorConfig.messageProtectionPassphrase">

                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.messageProtection')" for="ca-connector-config-messageProtection"></label>  <help-tag role="Admin" target="ca-connector.cmp.message-protection-passphrase"/>
                        <input type="number" class="form-control" name="messageProtection" id="ca-connector-config-messageProtection"
                               :class="{'valid': !$v.cAConnectorConfig.messageProtectionId.$invalid, 'invalid': $v.cAConnectorConfig.messageProtectionId.$invalid }" v-model="$v.cAConnectorConfig.messageProtectionId.$model" />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.issuerName')" for="ca-connector-config-issuerName"></label>  <help-tag role="Admin" target="ca-connector.cmp.issuer-name"/>
                        <input type="text" class="form-control" name="issuerName" id="ca-connector-config-issuerName"
                               :class="{'valid': !$v.cAConnectorConfig.issuerName.$invalid, 'invalid': $v.cAConnectorConfig.issuerName.$invalid }" v-model="$v.cAConnectorConfig.issuerName.$model" />
                    </div>
                    <div class="form-group"  v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.multipleMessages')" for="ca-connector-config-multipleMessages"></label>  <help-tag role="Admin" target="ca-connector.cmp.multiple-messages"/>
                        <input type="checkbox" class="form-check" name="multipleMessages" id="ca-connector-config-multipleMessages"
                               :class="{'valid': !$v.cAConnectorConfig.multipleMessages.$invalid, 'invalid': $v.cAConnectorConfig.multipleMessages.$invalid }" v-model="$v.cAConnectorConfig.multipleMessages.$model" />
                    </div>
                    <div class="form-group"  v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.implicitConfirm')" for="ca-connector-config-implicitConfirm"></label>  <help-tag role="Admin" target="ca-connector.cmp.implicit-confirm"/>
                        <input type="checkbox" class="form-check" name="implicitConfirm" id="ca-connector-config-implicitConfirm"
                               :class="{'valid': !$v.cAConnectorConfig.implicitConfirm.$invalid, 'invalid': $v.cAConnectorConfig.implicitConfirm.$invalid }" v-model="$v.cAConnectorConfig.implicitConfirm.$model" />
                    </div>

                    <div class="form-group"  v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.msgContentType')" for="ca-connector-config-msgContentType"></label>  <help-tag role="Admin" target="ca-connector.cmp.message-content-type"/>
                        <input type="text" class="form-control" name="msgContentType" id="ca-connector-config-msgContentType"
                               :class="{'valid': !$v.cAConnectorConfig.msgContentType.$invalid, 'invalid': $v.cAConnectorConfig.msgContentType.$invalid }" v-model="$v.cAConnectorConfig.msgContentType.$model" />
                    </div>

                    <div class="form-group"  v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.sni')" for="ca-connector-config-sni"></label>  <help-tag role="Admin" target="ca-connector.cmp.server-name-indication"/>
                        <input type="text" class="form-control" name="sni" id="ca-connector-config-sni"
                               :class="{'valid': !$v.cAConnectorConfig.sni.$invalid, 'invalid': $v.cAConnectorConfig.sni.$invalid }" v-model="$v.cAConnectorConfig.sni.$model" />

                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.disableHostNameVerifier')" for="ca-connector-config-disableHostNameVerifier"></label>  <help-tag role="Admin" target="ca-connector.cmp.disable-host-name-verifier"/>
                        <input type="checkbox" class="form-check" name="disableHostNameVerifier" id="ca-connector-config-disableHostNameVerifier"
                               v-model="cAConnectorConfig.disableHostNameVerifier" />

                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.ignoreResponseMessageVerification')" for="ca-connector-config-ignoreResponseMessageVerification"></label>  <help-tag role="Admin" target="ca-connector.cmp.ignore-response-message-verification"/>
                        <input type="checkbox" class="form-check" name="ca-connector-config-ignoreResponseMessageVerification" id="ca-connector-config-ignoreResponseMessageVerification"
                               v-model="$v.cAConnectorConfig.ignoreResponseMessageVerification.$model" />

                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.fillEmptySubjectWithSAN')" for="ca-connector-config-fillEmptySubjectWithSAN"></label>  <help-tag role="Admin" target="ca-connector.cmp.fill-empty-eubject-with-san"/>
                        <input type="checkbox" class="form-check" name="ca-connector-config-ignoreResponseMessageVerification" id="ca-connector-config-fillEmptySubjectWithSAN"
                               v-model="$v.cAConnectorConfig.fillEmptySubjectWithSAN.$model" />
                    </div>

                    <div v-if="cAConnectorConfig.id">
                        <audit-tag :caConnectorId="cAConnectorConfig.id" showLinks="false" :title="$t('ca3SApp.certificate.audit')"></audit-tag>
                    </div>
                </div>
                <div>
                    <!--button type="button" id="test" class="btn btn-secondary" v-on:click="testCaConnectorConfig()">
                        <font-awesome-icon icon="stethoscope"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.test')">Test</span>
                    </button-->
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')"></span>
                    </button>
                    <button type="submit" id="save-entity"
                            :disabled="!isSaveable() || isSaving"
                            class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span
                        v-text="$t('entity.action.save')"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./ca-connector-config-update.component.ts">
</script>

<style scoped>

.cmd0-content {
    height: 100px;
    width: 70%;
    font-family: monospace;
    font-size: 0.8rem;
}

</style>
