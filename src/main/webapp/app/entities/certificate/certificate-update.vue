<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.certificate.home.createOrEditLabel" v-text="$t('ca3SApp.certificate.home.createOrEditLabel')">Create or edit a Certificate</h2>
                <div>
                    <div class="form-group" v-if="certificate.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="certificate.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.tbsDigest')" for="certificate-tbsDigest">Tbs Digest</label>
                        <input type="text" class="form-control" name="tbsDigest" id="certificate-tbsDigest"
                            :class="{'valid': !$v.certificate.tbsDigest.$invalid, 'invalid': $v.certificate.tbsDigest.$invalid }" v-model="$v.certificate.tbsDigest.$model"  required/>
                        <div v-if="$v.certificate.tbsDigest.$anyDirty && $v.certificate.tbsDigest.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificate.tbsDigest.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.subject')" for="certificate-subject">Subject</label>
                        <input type="text" class="form-control" name="subject" id="certificate-subject"
                            :class="{'valid': !$v.certificate.subject.$invalid, 'invalid': $v.certificate.subject.$invalid }" v-model="$v.certificate.subject.$model"  required/>
                        <div v-if="$v.certificate.subject.$anyDirty && $v.certificate.subject.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificate.subject.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.issuer')" for="certificate-issuer">Issuer</label>
                        <input type="text" class="form-control" name="issuer" id="certificate-issuer"
                            :class="{'valid': !$v.certificate.issuer.$invalid, 'invalid': $v.certificate.issuer.$invalid }" v-model="$v.certificate.issuer.$model"  required/>
                        <div v-if="$v.certificate.issuer.$anyDirty && $v.certificate.issuer.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificate.issuer.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.type')" for="certificate-type">Type</label>
                        <input type="text" class="form-control" name="type" id="certificate-type"
                            :class="{'valid': !$v.certificate.type.$invalid, 'invalid': $v.certificate.type.$invalid }" v-model="$v.certificate.type.$model"  required/>
                        <div v-if="$v.certificate.type.$anyDirty && $v.certificate.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificate.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.description')" for="certificate-description">Description</label>
                        <input type="text" class="form-control" name="description" id="certificate-description"
                            :class="{'valid': !$v.certificate.description.$invalid, 'invalid': $v.certificate.description.$invalid }" v-model="$v.certificate.description.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.fingerprint')" for="certificate-fingerprint">Fingerprint</label>
                        <input type="text" class="form-control" name="fingerprint" id="certificate-fingerprint"
                            :class="{'valid': !$v.certificate.fingerprint.$invalid, 'invalid': $v.certificate.fingerprint.$invalid }" v-model="$v.certificate.fingerprint.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.serial')" for="certificate-serial">Serial</label>
                        <input type="text" class="form-control" name="serial" id="certificate-serial"
                            :class="{'valid': !$v.certificate.serial.$invalid, 'invalid': $v.certificate.serial.$invalid }" v-model="$v.certificate.serial.$model"  required/>
                        <div v-if="$v.certificate.serial.$anyDirty && $v.certificate.serial.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificate.serial.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.validFrom')" for="certificate-validFrom">Valid From</label>
                        <div class="d-flex">
                            <input id="certificate-validFrom" type="datetime-local" class="form-control" name="validFrom" :class="{'valid': !$v.certificate.validFrom.$invalid, 'invalid': $v.certificate.validFrom.$invalid }"
                             required
                            :value="convertDateTimeFromServer($v.certificate.validFrom.$model)"
                            @change="updateInstantField('validFrom', $event)"/>
                        </div>
                        <div v-if="$v.certificate.validFrom.$anyDirty && $v.certificate.validFrom.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificate.validFrom.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.certificate.validFrom.ZonedDateTimelocal" v-text="$t('entity.validation.ZonedDateTimelocal')">
                                This field should be a date and time.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.validTo')" for="certificate-validTo">Valid To</label>
                        <div class="d-flex">
                            <input id="certificate-validTo" type="datetime-local" class="form-control" name="validTo" :class="{'valid': !$v.certificate.validTo.$invalid, 'invalid': $v.certificate.validTo.$invalid }"
                             required
                            :value="convertDateTimeFromServer($v.certificate.validTo.$model)"
                            @change="updateInstantField('validTo', $event)"/>
                        </div>
                        <div v-if="$v.certificate.validTo.$anyDirty && $v.certificate.validTo.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificate.validTo.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.certificate.validTo.ZonedDateTimelocal" v-text="$t('entity.validation.ZonedDateTimelocal')">
                                This field should be a date and time.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.keyAlgorithm')" for="certificate-keyAlgorithm">Key Algorithm</label>
                        <input type="text" class="form-control" name="keyAlgorithm" id="certificate-keyAlgorithm"
                            :class="{'valid': !$v.certificate.keyAlgorithm.$invalid, 'invalid': $v.certificate.keyAlgorithm.$invalid }" v-model="$v.certificate.keyAlgorithm.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.keyLength')" for="certificate-keyLength">Key Length</label>
                        <input type="number" class="form-control" name="keyLength" id="certificate-keyLength"
                            :class="{'valid': !$v.certificate.keyLength.$invalid, 'invalid': $v.certificate.keyLength.$invalid }" v-model.number="$v.certificate.keyLength.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.curveName')" for="certificate-curveName">Curve Name</label>
                        <input type="text" class="form-control" name="curveName" id="certificate-curveName"
                            :class="{'valid': !$v.certificate.curveName.$invalid, 'invalid': $v.certificate.curveName.$invalid }" v-model="$v.certificate.curveName.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.hashingAlgorithm')" for="certificate-hashingAlgorithm">Hashing Algorithm</label>
                        <input type="text" class="form-control" name="hashingAlgorithm" id="certificate-hashingAlgorithm"
                            :class="{'valid': !$v.certificate.hashingAlgorithm.$invalid, 'invalid': $v.certificate.hashingAlgorithm.$invalid }" v-model="$v.certificate.hashingAlgorithm.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.paddingAlgorithm')" for="certificate-paddingAlgorithm">Padding Algorithm</label>
                        <input type="text" class="form-control" name="paddingAlgorithm" id="certificate-paddingAlgorithm"
                            :class="{'valid': !$v.certificate.paddingAlgorithm.$invalid, 'invalid': $v.certificate.paddingAlgorithm.$invalid }" v-model="$v.certificate.paddingAlgorithm.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.signingAlgorithm')" for="certificate-signingAlgorithm">Signing Algorithm</label>
                        <input type="text" class="form-control" name="signingAlgorithm" id="certificate-signingAlgorithm"
                            :class="{'valid': !$v.certificate.signingAlgorithm.$invalid, 'invalid': $v.certificate.signingAlgorithm.$invalid }" v-model="$v.certificate.signingAlgorithm.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.creationExecutionId')" for="certificate-creationExecutionId">Creation Execution Id</label>
                        <input type="text" class="form-control" name="creationExecutionId" id="certificate-creationExecutionId"
                            :class="{'valid': !$v.certificate.creationExecutionId.$invalid, 'invalid': $v.certificate.creationExecutionId.$invalid }" v-model="$v.certificate.creationExecutionId.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.contentAddedAt')" for="certificate-contentAddedAt">Content Added At</label>
                        <div class="d-flex">
                            <input id="certificate-contentAddedAt" type="datetime-local" class="form-control" name="contentAddedAt" :class="{'valid': !$v.certificate.contentAddedAt.$invalid, 'invalid': $v.certificate.contentAddedAt.$invalid }"
                            
                            :value="convertDateTimeFromServer($v.certificate.contentAddedAt.$model)"
                            @change="updateInstantField('contentAddedAt', $event)"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.revokedSince')" for="certificate-revokedSince">Revoked Since</label>
                        <div class="d-flex">
                            <input id="certificate-revokedSince" type="datetime-local" class="form-control" name="revokedSince" :class="{'valid': !$v.certificate.revokedSince.$invalid, 'invalid': $v.certificate.revokedSince.$invalid }"
                            
                            :value="convertDateTimeFromServer($v.certificate.revokedSince.$model)"
                            @change="updateInstantField('revokedSince', $event)"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.revocationReason')" for="certificate-revocationReason">Revocation Reason</label>
                        <input type="text" class="form-control" name="revocationReason" id="certificate-revocationReason"
                            :class="{'valid': !$v.certificate.revocationReason.$invalid, 'invalid': $v.certificate.revocationReason.$invalid }" v-model="$v.certificate.revocationReason.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.revoked')" for="certificate-revoked">Revoked</label>
                        <input type="checkbox" class="form-check" name="revoked" id="certificate-revoked"
                            :class="{'valid': !$v.certificate.revoked.$invalid, 'invalid': $v.certificate.revoked.$invalid }" v-model="$v.certificate.revoked.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.revocationExecutionId')" for="certificate-revocationExecutionId">Revocation Execution Id</label>
                        <input type="text" class="form-control" name="revocationExecutionId" id="certificate-revocationExecutionId"
                            :class="{'valid': !$v.certificate.revocationExecutionId.$invalid, 'invalid': $v.certificate.revocationExecutionId.$invalid }" v-model="$v.certificate.revocationExecutionId.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.endEntity')" for="certificate-endEntity">End Entity</label>
                        <input type="checkbox" class="form-check" name="endEntity" id="certificate-endEntity"
                            :class="{'valid': !$v.certificate.endEntity.$invalid, 'invalid': $v.certificate.endEntity.$invalid }" v-model="$v.certificate.endEntity.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.selfsigned')" for="certificate-selfsigned">Selfsigned</label>
                        <input type="checkbox" class="form-check" name="selfsigned" id="certificate-selfsigned"
                            :class="{'valid': !$v.certificate.selfsigned.$invalid, 'invalid': $v.certificate.selfsigned.$invalid }" v-model="$v.certificate.selfsigned.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.content')" for="certificate-content">Content</label>
                        <textarea class="form-control" name="content" id="certificate-content"
                            :class="{'valid': !$v.certificate.content.$invalid, 'invalid': $v.certificate.content.$invalid }" v-model="$v.certificate.content.$model"  required></textarea>
                        <div v-if="$v.certificate.content.$anyDirty && $v.certificate.content.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificate.content.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.csr')" for="certificate-csr">Csr</label>
                        <select class="form-control" id="certificate-csr" name="csr" v-model="certificate.csr">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="certificate.csr && cSROption.id === certificate.csr.id ? certificate.csr : cSROption" v-for="cSROption in csrs" :key="cSROption.id">{{cSROption.id}}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.issuingCertificate')" for="certificate-issuingCertificate">Issuing Certificate</label>
                        <select class="form-control" id="certificate-issuingCertificate" name="issuingCertificate" v-model="certificate.issuingCertificate">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="certificate.issuingCertificate && certificateOption.id === certificate.issuingCertificate.id ? certificate.issuingCertificate : certificateOption" v-for="certificateOption in certificates" :key="certificateOption.id">{{certificateOption.id}}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.rootCertificate')" for="certificate-rootCertificate">Root Certificate</label>
                        <select class="form-control" id="certificate-rootCertificate" name="rootCertificate" v-model="certificate.rootCertificate">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="certificate.rootCertificate && certificateOption.id === certificate.rootCertificate.id ? certificate.rootCertificate : certificateOption" v-for="certificateOption in certificates" :key="certificateOption.id">{{certificateOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.certificate.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./certificate-update.component.ts">
</script>
