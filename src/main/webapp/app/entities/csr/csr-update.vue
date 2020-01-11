<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.cSR.home.createOrEditLabel" v-text="$t('ca3SApp.cSR.home.createOrEditLabel')">Create or edit a CSR</h2>
                <div>
                    <div class="form-group" v-if="cSR.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="cSR.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.csrBase64')" for="csr-csrBase64">Csr Base 64</label>
                        <textarea class="form-control" name="csrBase64" id="csr-csrBase64"
                            :class="{'valid': !$v.cSR.csrBase64.$invalid, 'invalid': $v.cSR.csrBase64.$invalid }" v-model="$v.cSR.csrBase64.$model"  required></textarea>
                        <div v-if="$v.cSR.csrBase64.$anyDirty && $v.cSR.csrBase64.$invalid">
                            <small class="form-text text-danger" v-if="!$v.cSR.csrBase64.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.requestedOn')" for="csr-requestedOn">Requested On</label>
                        <div class="input-group">
                            <input id="csr-requestedOn" type="date" class="form-control" name="requestedOn"  :class="{'valid': !$v.cSR.requestedOn.$invalid, 'invalid': $v.cSR.requestedOn.$invalid }"
                            v-model="$v.cSR.requestedOn.$model"  required />
                        </div>
                        <div v-if="$v.cSR.requestedOn.$anyDirty && $v.cSR.requestedOn.$invalid">
                            <small class="form-text text-danger" v-if="!$v.cSR.requestedOn.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.status')" for="csr-status">Status</label>
                        <select class="form-control" name="status" :class="{'valid': !$v.cSR.status.$invalid, 'invalid': $v.cSR.status.$invalid }" v-model="$v.cSR.status.$model" id="csr-status"  required>
                            <option value="PROCESSING" v-bind:label="$t('ca3SApp.CsrStatus.PROCESSING')">PROCESSING</option>
                            <option value="ISSUED" v-bind:label="$t('ca3SApp.CsrStatus.ISSUED')">ISSUED</option>
                            <option value="REJECTED" v-bind:label="$t('ca3SApp.CsrStatus.REJECTED')">REJECTED</option>
                            <option value="PENDING" v-bind:label="$t('ca3SApp.CsrStatus.PENDING')">PENDING</option>
                        </select>
                        <div v-if="$v.cSR.status.$anyDirty && $v.cSR.status.$invalid">
                            <small class="form-text text-danger" v-if="!$v.cSR.status.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.processInstanceId')" for="csr-processInstanceId">Process Instance Id</label>
                        <input type="text" class="form-control" name="processInstanceId" id="csr-processInstanceId"
                            :class="{'valid': !$v.cSR.processInstanceId.$invalid, 'invalid': $v.cSR.processInstanceId.$invalid }" v-model="$v.cSR.processInstanceId.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.signingAlgorithm')" for="csr-signingAlgorithm">Signing Algorithm</label>
                        <input type="text" class="form-control" name="signingAlgorithm" id="csr-signingAlgorithm"
                            :class="{'valid': !$v.cSR.signingAlgorithm.$invalid, 'invalid': $v.cSR.signingAlgorithm.$invalid }" v-model="$v.cSR.signingAlgorithm.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.isCSRValid')" for="csr-isCSRValid">Is CSR Valid</label>
                        <input type="checkbox" class="form-check" name="isCSRValid" id="csr-isCSRValid"
                            :class="{'valid': !$v.cSR.isCSRValid.$invalid, 'invalid': $v.cSR.isCSRValid.$invalid }" v-model="$v.cSR.isCSRValid.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.x509KeySpec')" for="csr-x509KeySpec">X 509 Key Spec</label>
                        <input type="text" class="form-control" name="x509KeySpec" id="csr-x509KeySpec"
                            :class="{'valid': !$v.cSR.x509KeySpec.$invalid, 'invalid': $v.cSR.x509KeySpec.$invalid }" v-model="$v.cSR.x509KeySpec.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.publicKeyAlgorithm')" for="csr-publicKeyAlgorithm">Public Key Algorithm</label>
                        <input type="text" class="form-control" name="publicKeyAlgorithm" id="csr-publicKeyAlgorithm"
                            :class="{'valid': !$v.cSR.publicKeyAlgorithm.$invalid, 'invalid': $v.cSR.publicKeyAlgorithm.$invalid }" v-model="$v.cSR.publicKeyAlgorithm.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.publicKeyHash')" for="csr-publicKeyHash">Public Key Hash</label>
                        <input type="text" class="form-control" name="publicKeyHash" id="csr-publicKeyHash"
                            :class="{'valid': !$v.cSR.publicKeyHash.$invalid, 'invalid': $v.cSR.publicKeyHash.$invalid }" v-model="$v.cSR.publicKeyHash.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.subjectPublicKeyInfoBase64')" for="csr-subjectPublicKeyInfoBase64">Subject Public Key Info Base 64</label>
                        <textarea class="form-control" name="subjectPublicKeyInfoBase64" id="csr-subjectPublicKeyInfoBase64"
                            :class="{'valid': !$v.cSR.subjectPublicKeyInfoBase64.$invalid, 'invalid': $v.cSR.subjectPublicKeyInfoBase64.$invalid }" v-model="$v.cSR.subjectPublicKeyInfoBase64.$model"  required></textarea>
                        <div v-if="$v.cSR.subjectPublicKeyInfoBase64.$anyDirty && $v.cSR.subjectPublicKeyInfoBase64.$invalid">
                            <small class="form-text text-danger" v-if="!$v.cSR.subjectPublicKeyInfoBase64.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.cSR.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./csr-update.component.ts">
</script>
