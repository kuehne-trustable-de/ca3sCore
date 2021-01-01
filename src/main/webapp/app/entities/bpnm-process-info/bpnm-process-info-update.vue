<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.bPNMProcessInfo.home.createOrEditLabel" v-text="$t('ca3SApp.bPNMProcessInfo.home.createOrEditLabel')">Create or edit a BPNMProcessInfo</h2>
                <div>
                    <div class="form-group" v-if="bPNMProcessInfo.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="bPNMProcessInfo.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.name')" for="bpnm-process-info-name">Name</label>
                        <input type="text" class="form-control" name="name" id="bpnm-process-info-name"
                            :class="{'valid': !$v.bPNMProcessInfo.name.$invalid, 'invalid': $v.bPNMProcessInfo.name.$invalid }" v-model="$v.bPNMProcessInfo.name.$model"  required/>
                        <div v-if="$v.bPNMProcessInfo.name.$anyDirty && $v.bPNMProcessInfo.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.bPNMProcessInfo.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.version')" for="bpnm-process-info-version">Version</label>
                        <input type="text" class="form-control" name="version" id="bpnm-process-info-version"
                            :class="{'valid': !$v.bPNMProcessInfo.version.$invalid, 'invalid': $v.bPNMProcessInfo.version.$invalid }" v-model="$v.bPNMProcessInfo.version.$model"  required/>
                        <div v-if="$v.bPNMProcessInfo.version.$anyDirty && $v.bPNMProcessInfo.version.$invalid">
                            <small class="form-text text-danger" v-if="!$v.bPNMProcessInfo.version.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.type')" for="bpnm-process-info-type">Type</label>
                        <select class="form-control" name="type" :class="{'valid': !$v.bPNMProcessInfo.type.$invalid, 'invalid': $v.bPNMProcessInfo.type.$invalid }" v-model="$v.bPNMProcessInfo.type.$model" id="bpnm-process-info-type"  required>
                            <option value="CA_INVOCATION" v-bind:label="$t('ca3SApp.BPNMProcessType.CA_INVOCATION')">CA_INVOCATION</option>
                            <option value="REQUEST_AUTHORIZATION" v-bind:label="$t('ca3SApp.BPNMProcessType.REQUEST_AUTHORIZATION')">REQUEST_AUTHORIZATION</option>
                        </select>
                        <div v-if="$v.bPNMProcessInfo.type.$anyDirty && $v.bPNMProcessInfo.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.bPNMProcessInfo.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.author')" for="bpnm-process-info-author">Author</label>
                        <input type="text" class="form-control" name="author" id="bpnm-process-info-author"
                            :class="{'valid': !$v.bPNMProcessInfo.author.$invalid, 'invalid': $v.bPNMProcessInfo.author.$invalid }" v-model="$v.bPNMProcessInfo.author.$model"  required/>
                        <div v-if="$v.bPNMProcessInfo.author.$anyDirty && $v.bPNMProcessInfo.author.$invalid">
                            <small class="form-text text-danger" v-if="!$v.bPNMProcessInfo.author.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.lastChange')" for="bpnm-process-info-lastChange">Last Change</label>
                        <div class="d-flex">
                            <input id="bpnm-process-info-lastChange" type="datetime-local" class="form-control" name="lastChange" :class="{'valid': !$v.bPNMProcessInfo.lastChange.$invalid, 'invalid': $v.bPNMProcessInfo.lastChange.$invalid }"
                             required
                            :value="convertDateTimeFromServer($v.bPNMProcessInfo.lastChange.$model)"
                            @change="updateInstantField('lastChange', $event)"/>
                        </div>
                        <div v-if="$v.bPNMProcessInfo.lastChange.$anyDirty && $v.bPNMProcessInfo.lastChange.$invalid">
                            <small class="form-text text-danger" v-if="!$v.bPNMProcessInfo.lastChange.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.bPNMProcessInfo.lastChange.ZonedDateTimelocal" v-text="$t('entity.validation.ZonedDateTimelocal')">
                                This field should be a date and time.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.signatureBase64')" for="bpnm-process-info-signatureBase64">Signature Base 64</label>
                        <textarea class="form-control" name="signatureBase64" id="bpnm-process-info-signatureBase64"
                            :class="{'valid': !$v.bPNMProcessInfo.signatureBase64.$invalid, 'invalid': $v.bPNMProcessInfo.signatureBase64.$invalid }" v-model="$v.bPNMProcessInfo.signatureBase64.$model"  required></textarea>
                        <div v-if="$v.bPNMProcessInfo.signatureBase64.$anyDirty && $v.bPNMProcessInfo.signatureBase64.$invalid">
                            <small class="form-text text-danger" v-if="!$v.bPNMProcessInfo.signatureBase64.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.bPNMProcessInfo.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./bpnm-process-info-update.component.ts">
</script>
