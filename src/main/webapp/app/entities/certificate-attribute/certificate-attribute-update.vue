<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.certificateAttribute.home.createOrEditLabel" v-text="$t('ca3SApp.certificateAttribute.home.createOrEditLabel')">Create or edit a CertificateAttribute</h2>
                <div>
                    <div class="form-group" v-if="certificateAttribute.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="certificateAttribute.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificateAttribute.name')" for="certificate-attribute-name">Name</label>
                        <input type="text" class="form-control" name="name" id="certificate-attribute-name"
                            :class="{'valid': !$v.certificateAttribute.name.$invalid, 'invalid': $v.certificateAttribute.name.$invalid }" v-model="$v.certificateAttribute.name.$model"  required/>
                        <div v-if="$v.certificateAttribute.name.$anyDirty && $v.certificateAttribute.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.certificateAttribute.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificateAttribute.value')" for="certificate-attribute-value">Value</label>
                        <input type="text" class="form-control" name="value" id="certificate-attribute-value"
                            :class="{'valid': !$v.certificateAttribute.value.$invalid, 'invalid': $v.certificateAttribute.value.$invalid }" v-model="$v.certificateAttribute.value.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificateAttribute.certificate')" for="certificate-attribute-certificate">Certificate</label>
                        <select class="form-control" id="certificate-attribute-certificate" name="certificate" v-model="certificateAttribute.certificate">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="certificateAttribute.certificate && certificateOption.id === certificateAttribute.certificate.id ? certificateAttribute.certificate : certificateOption" v-for="certificateOption in certificates" :key="certificateOption.id">{{certificateOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.certificateAttribute.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./certificate-attribute-update.component.ts">
</script>
