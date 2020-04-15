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
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.subject')" for="csr-subject">Subject</label>
                        <input type="text" class="form-control" name="subject" id="csr-subject"
                            :class="{'valid': !$v.cSR.subject.$invalid, 'invalid': $v.cSR.subject.$invalid }" v-model="$v.cSR.subject.$model"  required/>
                        <div v-if="$v.cSR.subject.$anyDirty && $v.cSR.subject.$invalid">
                            <small class="form-text text-danger" v-if="!$v.cSR.subject.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.sans')" for="csr-sans">Sans</label>
                        <input type="text" class="form-control" name="sans" id="csr-sans"
                            :class="{'valid': !$v.cSR.sans.$invalid, 'invalid': $v.cSR.sans.$invalid }" v-model="$v.cSR.sans.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.requestedOn')" for="csr-requestedOn">Requested On</label>
                        <div class="d-flex">
                            <input id="csr-requestedOn" type="datetime-local" class="form-control" name="requestedOn" :class="{'valid': !$v.cSR.requestedOn.$invalid, 'invalid': $v.cSR.requestedOn.$invalid }"
                             required
                            :value="convertDateTimeFromServer($v.cSR.requestedOn.$model)"
                            @change="updateInstantField('requestedOn', $event)"/>
                        </div>
                        <div v-if="$v.cSR.requestedOn.$anyDirty && $v.cSR.requestedOn.$invalid">
                            <small class="form-text text-danger" v-if="!$v.cSR.requestedOn.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.cSR.requestedOn.ZonedDateTimelocal" v-text="$t('entity.validation.ZonedDateTimelocal')">
                                This field should be a date and time.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.requestedBy')" for="csr-requestedBy">Requested By</label>
                        <input type="text" class="form-control" name="requestedBy" id="csr-requestedBy"
                            :class="{'valid': !$v.cSR.requestedBy.$invalid, 'invalid': $v.cSR.requestedBy.$invalid }" v-model="$v.cSR.requestedBy.$model"  required/>
                        <div v-if="$v.cSR.requestedBy.$anyDirty && $v.cSR.requestedBy.$invalid">
                            <small class="form-text text-danger" v-if="!$v.cSR.requestedBy.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.pipelineType')" for="csr-pipelineType">Pipeline Type</label>
                        <select class="form-control" name="pipelineType" :class="{'valid': !$v.cSR.pipelineType.$invalid, 'invalid': $v.cSR.pipelineType.$invalid }" v-model="$v.cSR.pipelineType.$model" id="csr-pipelineType"  required>
                            <option value="ACME" v-bind:label="$t('ca3SApp.PipelineType.ACME')">ACME</option>
                            <option value="SCEP" v-bind:label="$t('ca3SApp.PipelineType.SCEP')">SCEP</option>
                            <option value="WEB" v-bind:label="$t('ca3SApp.PipelineType.WEB')">WEB</option>
                            <option value="INTERNAL" v-bind:label="$t('ca3SApp.PipelineType.INTERNAL')">INTERNAL</option>
                        </select>
                        <div v-if="$v.cSR.pipelineType.$anyDirty && $v.cSR.pipelineType.$invalid">
                            <small class="form-text text-danger" v-if="!$v.cSR.pipelineType.required" v-text="$t('entity.validation.required')">
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
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.administeredBy')" for="csr-administeredBy">Administered By</label>
                        <input type="text" class="form-control" name="administeredBy" id="csr-administeredBy"
                            :class="{'valid': !$v.cSR.administeredBy.$invalid, 'invalid': $v.cSR.administeredBy.$invalid }" v-model="$v.cSR.administeredBy.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.approvedOn')" for="csr-approvedOn">Approved On</label>
                        <div class="d-flex">
                            <input id="csr-approvedOn" type="datetime-local" class="form-control" name="approvedOn" :class="{'valid': !$v.cSR.approvedOn.$invalid, 'invalid': $v.cSR.approvedOn.$invalid }"
                            
                            :value="convertDateTimeFromServer($v.cSR.approvedOn.$model)"
                            @change="updateInstantField('approvedOn', $event)"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.rejectedOn')" for="csr-rejectedOn">Rejected On</label>
                        <div class="d-flex">
                            <input id="csr-rejectedOn" type="datetime-local" class="form-control" name="rejectedOn" :class="{'valid': !$v.cSR.rejectedOn.$invalid, 'invalid': $v.cSR.rejectedOn.$invalid }"
                            
                            :value="convertDateTimeFromServer($v.cSR.rejectedOn.$model)"
                            @change="updateInstantField('rejectedOn', $event)"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.rejectionReason')" for="csr-rejectionReason">Rejection Reason</label>
                        <input type="text" class="form-control" name="rejectionReason" id="csr-rejectionReason"
                            :class="{'valid': !$v.cSR.rejectionReason.$invalid, 'invalid': $v.cSR.rejectionReason.$invalid }" v-model="$v.cSR.rejectionReason.$model" />
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
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.keyAlgorithm')" for="csr-keyAlgorithm">Key Algorithm</label>
                        <input type="text" class="form-control" name="keyAlgorithm" id="csr-keyAlgorithm"
                            :class="{'valid': !$v.cSR.keyAlgorithm.$invalid, 'invalid': $v.cSR.keyAlgorithm.$invalid }" v-model="$v.cSR.keyAlgorithm.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.keyLength')" for="csr-keyLength">Key Length</label>
                        <input type="number" class="form-control" name="keyLength" id="csr-keyLength"
                            :class="{'valid': !$v.cSR.keyLength.$invalid, 'invalid': $v.cSR.keyLength.$invalid }" v-model.number="$v.cSR.keyLength.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.publicKeyHash')" for="csr-publicKeyHash">Public Key Hash</label>
                        <input type="text" class="form-control" name="publicKeyHash" id="csr-publicKeyHash"
                            :class="{'valid': !$v.cSR.publicKeyHash.$invalid, 'invalid': $v.cSR.publicKeyHash.$invalid }" v-model="$v.cSR.publicKeyHash.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.serversideKeyGeneration')" for="csr-serversideKeyGeneration">Serverside Key Generation</label>
                        <input type="checkbox" class="form-check" name="serversideKeyGeneration" id="csr-serversideKeyGeneration"
                            :class="{'valid': !$v.cSR.serversideKeyGeneration.$invalid, 'invalid': $v.cSR.serversideKeyGeneration.$invalid }" v-model="$v.cSR.serversideKeyGeneration.$model" />
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
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.requestorComment')" for="csr-requestorComment">Requestor Comment</label>
                        <textarea class="form-control" name="requestorComment" id="csr-requestorComment"
                            :class="{'valid': !$v.cSR.requestorComment.$invalid, 'invalid': $v.cSR.requestorComment.$invalid }" v-model="$v.cSR.requestorComment.$model" ></textarea>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.administrationComment')" for="csr-administrationComment">Administration Comment</label>
                        <textarea class="form-control" name="administrationComment" id="csr-administrationComment"
                            :class="{'valid': !$v.cSR.administrationComment.$invalid, 'invalid': $v.cSR.administrationComment.$invalid }" v-model="$v.cSR.administrationComment.$model" ></textarea>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.pipeline')" for="csr-pipeline">Pipeline</label>
                        <select class="form-control" id="csr-pipeline" name="pipeline" v-model="cSR.pipeline">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="cSR.pipeline && pipelineOption.id === cSR.pipeline.id ? cSR.pipeline : pipelineOption" v-for="pipelineOption in pipelines" :key="pipelineOption.id">{{pipelineOption.id}}</option>
                        </select>
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
