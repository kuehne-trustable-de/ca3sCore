<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.cAConnectorConfig.home.createOrEditLabel" v-text="$t('ca3SApp.cAConnectorConfig.home.createOrEditLabel')">Create or edit a CAConnectorConfig</h2>
                <div>
                    <!--div class="form-group" v-if="cAConnectorConfig.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="cAConnectorConfig.id" readonly />
                    </div-->

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.name')" for="ca-connector-config-name">Name</label>
                        <input type="text" class="form-control" name="name" id="ca-connector-config-name"
                            :class="{'valid': !$v.cAConnectorConfig.name.$invalid, 'invalid': $v.cAConnectorConfig.name.$invalid }" v-model="$v.cAConnectorConfig.name.$model"  required/>
                        <div v-if="$v.cAConnectorConfig.name.$anyDirty && $v.cAConnectorConfig.name.$invalid">
                            <small class="form-text text-danger" v-if="$v.cAConnectorConfig.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                        <div>required: {{$v.cAConnectorConfig.name.required}}, dirty: {{$v.cAConnectorConfig.name.$anyDirty}}, invalid: {{$v.cAConnectorConfig.name.$invalid}}</div>
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.caConnectorType')" for="ca-connector-config-caConnectorType">Ca Connector Type</label>
                        <select class="form-control" name="caConnectorType"
                                :class="{'valid': !$v.cAConnectorConfig.caConnectorType.$invalid, 'invalid': $v.cAConnectorConfig.caConnectorType.$invalid }"
                                v-model="$v.cAConnectorConfig.caConnectorType.$model"
                                id="ca-connector-config-caConnectorType"  required>
                            <option value="INTERNAL" v-bind:label="$t('ca3SApp.CAConnectorType.INTERNAL')">INTERNAL</option>
                            <option value="CMP" v-bind:label="$t('ca3SApp.CAConnectorType.CMP')">CMP</option>
                            <option value="ADCS" v-bind:label="$t('ca3SApp.CAConnectorType.ADCS')">ADCS</option>
                            <option value="ADCS_CERTIFICATE_INVENTORY" v-bind:label="$t('ca3SApp.CAConnectorType.ADCS_CERTIFICATE_INVENTORY')">ADCS_CERTIFICATE_INVENTORY</option>
                            <option value="DIRECTORY" v-bind:label="$t('ca3SApp.CAConnectorType.DIRECTORY')">DIRECTORY</option>
                        </select>

                        <div v-if="$v.cAConnectorConfig.caConnectorType.$anyDirty && $v.cAConnectorConfig.caConnectorType.$invalid">
                            <small class="form-text text-danger" v-if="$v.cAConnectorConfig.caConnectorType.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                        <div>dirty: {{$v.cAConnectorConfig.caConnectorType.$anyDirty}}, invalid: {{$v.cAConnectorConfig.caConnectorType.$invalid}}</div>
                    </div>
                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model !== 'INTERNAL'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.caUrl')" for="ca-connector-config-caUrl">Ca Url</label>
                        <input type="text" class="form-control" name="caUrl" id="ca-connector-config-caUrl"
                            :class="{'valid': !$v.cAConnectorConfig.caUrl.$invalid, 'invalid': $v.cAConnectorConfig.caUrl.$invalid }" v-model="$v.cAConnectorConfig.caUrl.$model" />
                    </div>
                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'ADCS_CERTIFICATE_INVENTORY'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.pollingOffset')" for="ca-connector-config-pollingOffset">Polling Offset</label>
                        <input type="number" class="form-control" name="pollingOffset" id="ca-connector-config-pollingOffset"
                            :class="{'valid': !$v.cAConnectorConfig.pollingOffset.$invalid, 'invalid': $v.cAConnectorConfig.pollingOffset.$invalid }" v-model.number="$v.cAConnectorConfig.pollingOffset.$model" />
                    </div>
                    <div class="form-group" v-if="!($v.cAConnectorConfig.caConnectorType.$model === 'DIRECTORY' || $v.cAConnectorConfig.caConnectorType.$model === 'ADCS_CERTIFICATE_INVENTORY')">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.defaultCA')" for="ca-connector-config-defaultCA">Default CA</label>
                        <input type="checkbox" class="form-check" name="defaultCA" id="ca-connector-config-defaultCA"
                               :class="{'valid': !$v.cAConnectorConfig.defaultCA.$invalid, 'invalid': $v.cAConnectorConfig.defaultCA.$invalid }" v-model="$v.cAConnectorConfig.defaultCA.$model" />
                    </div>
                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'DIRECTORY'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.trustSelfsignedCertificates')" for="ca-connector-config-trustSelfsignedCertificates">Trust Selfsigned Certificates</label>
                        <input type="checkbox" class="form-check" name="trustSelfsignedCertificates" id="ca-connector-config-trustSelfsignedCertificates"
                               :class="{'valid': !$v.cAConnectorConfig.trustSelfsignedCertificates.$invalid, 'invalid': $v.cAConnectorConfig.trustSelfsignedCertificates.$invalid }" v-model="$v.cAConnectorConfig.trustSelfsignedCertificates.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.active')" for="ca-connector-config-active">Active</label>
                        <input type="checkbox" class="form-check" name="active" id="ca-connector-config-active"
                            :class="{'valid': !$v.cAConnectorConfig.active.$invalid, 'invalid': $v.cAConnectorConfig.active.$invalid }" v-model="$v.cAConnectorConfig.active.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.selector')" for="ca-connector-config-selector">Selector</label>
                        <input type="text" class="form-control" name="selector" id="ca-connector-config-selector"
                            :class="{'valid': !$v.cAConnectorConfig.selector.$invalid, 'invalid': $v.cAConnectorConfig.selector.$invalid }" v-model="$v.cAConnectorConfig.selector.$model" />
                    </div>
                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'DIRECTORY' || $v.cAConnectorConfig.caConnectorType.$model === 'ADCS_CERTIFICATE_INVENTORY' ">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.interval')" for="ca-connector-config-interval">Interval</label>
                        <select class="form-control" name="interval" :class="{'valid': !$v.cAConnectorConfig.interval.$invalid, 'invalid': $v.cAConnectorConfig.interval.$invalid }" v-model="$v.cAConnectorConfig.interval.$model" id="ca-connector-config-interval" >
                            <option value="MINUTE" v-bind:label="$t('ca3SApp.Interval.MINUTE')">MINUTE</option>
                            <option value="HOUR" v-bind:label="$t('ca3SApp.Interval.HOUR')">HOUR</option>
                            <option value="DAY" v-bind:label="$t('ca3SApp.Interval.DAY')">DAY</option>
                            <option value="WEEK" v-bind:label="$t('ca3SApp.Interval.WEEK')">WEEK</option>
                            <option value="MONTH" v-bind:label="$t('ca3SApp.Interval.MONTH')">MONTH</option>
                        </select>
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP' ">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.tlsAuthentication')" for="ca-connector-config-tlsAuthentication">tls Authentication Certificate</label>
                        <input type="number" class="form-control" name="tlsAuthentication" id="ca-connector-config-tlsAuthentication"
                               :class="{'valid': !$v.cAConnectorConfig.tlsAuthenticationId.$invalid, 'invalid': $v.cAConnectorConfig.tlsAuthenticationId.$invalid }" v-model="$v.cAConnectorConfig.tlsAuthenticationId.$model" />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP' ">

                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.messageProtectionPassphrase')" for="ca-connector-config-messageProtectionPassphrase">Message Protection 'Passphrase'</label>
                        <input type="checkbox" class="form-check" name="messageProtectionPassphrase" id="ca-connector-config-messageProtectionPassphrase"
                               v-model="cAConnectorConfig.messageProtectionPassphrase" />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model !== 'CMP' || ($v.cAConnectorConfig.caConnectorType.$model === 'CMP' && cAConnectorConfig.messageProtectionPassphrase)">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.plainSecret')" for="ca-connector-config-plainSecret">Passphrase</label>
                        <input type="password" class="form-control" name="plainSecret" id="ca-connector-config-plainSecret"
                               :class="{'valid': !$v.cAConnectorConfig.plainSecret.$invalid, 'invalid': $v.cAConnectorConfig.plainSecret.$invalid }" v-model="$v.cAConnectorConfig.plainSecret.$model" />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP' && !cAConnectorConfig.messageProtectionPassphrase">

                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.messageProtection')" for="ca-connector-config-messageProtection">Message Protection Certificate</label>
                        <input type="number" class="form-control" name="messageProtection" id="ca-connector-config-messageProtection"
                               :class="{'valid': !$v.cAConnectorConfig.messageProtectionId.$invalid, 'invalid': $v.cAConnectorConfig.messageProtectionId.$invalid }" v-model="$v.cAConnectorConfig.messageProtectionId.$model" />
                    </div>

                    <div class="form-group" v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.issuerName')" for="ca-connector-config-issuerName">issuer Name</label>
                        <input type="text" class="form-control" name="issuerName" id="ca-connector-config-issuerName"
                               :class="{'valid': !$v.cAConnectorConfig.issuerName.$invalid, 'invalid': $v.cAConnectorConfig.issuerName.$invalid }" v-model="$v.cAConnectorConfig.issuerName.$model" />
                    </div>
                    <div class="form-group"  v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.multipleMessages')" for="ca-connector-config-multipleMessages">Multiple Messages</label>
                        <input type="checkbox" class="form-check" name="multipleMessages" id="ca-connector-config-multipleMessages"
                               :class="{'valid': !$v.cAConnectorConfig.multipleMessages.$invalid, 'invalid': $v.cAConnectorConfig.multipleMessages.$invalid }" v-model="$v.cAConnectorConfig.multipleMessages.$model" />
                    </div>
                    <div class="form-group"  v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.implicitConfirm')" for="ca-connector-config-implicitConfirm">Implicit Confirm</label>
                        <input type="checkbox" class="form-check" name="implicitConfirm" id="ca-connector-config-implicitConfirm"
                               :class="{'valid': !$v.cAConnectorConfig.implicitConfirm.$invalid, 'invalid': $v.cAConnectorConfig.implicitConfirm.$invalid }" v-model="$v.cAConnectorConfig.implicitConfirm.$model" />
                    </div>

                    <div class="form-group"  v-if="$v.cAConnectorConfig.caConnectorType.$model === 'CMP'">
                        <label class="form-control-label" v-text="$t('ca3SApp.cAConnectorConfig.msgContentType')" for="ca-connector-config-msgContentType">Msg Content Type</label>
                        <input type="text" class="form-control" name="msgContentType" id="ca-connector-config-msgContentType"
                               :class="{'valid': !$v.cAConnectorConfig.msgContentType.$invalid, 'invalid': $v.cAConnectorConfig.msgContentType.$invalid }" v-model="$v.cAConnectorConfig.msgContentType.$model" />
                    </div>

                    <div>
                        <audit-tag :caConnectorId="cAConnectorConfig.id" showLinks="false" :title="$t('ca3SApp.certificate.audit')"></audit-tag>
                    </div>

                </div>
                <div>
                    <!--button type="button" id="test" class="btn btn-secondary" v-on:click="testCaConnectorConfig()">
                        <font-awesome-icon icon="stethoscope"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.test')">Test</span>
                    </button-->
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.cAConnectorConfig.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./ca-connector-config-update.component.ts">
</script>
