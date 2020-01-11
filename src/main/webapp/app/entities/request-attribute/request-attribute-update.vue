<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.requestAttribute.home.createOrEditLabel" v-text="$t('ca3SApp.requestAttribute.home.createOrEditLabel')">Create or edit a RequestAttribute</h2>
                <div>
                    <div class="form-group" v-if="requestAttribute.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="requestAttribute.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.requestAttribute.attributeType')" for="request-attribute-attributeType">Attribute Type</label>
                        <input type="text" class="form-control" name="attributeType" id="request-attribute-attributeType"
                            :class="{'valid': !$v.requestAttribute.attributeType.$invalid, 'invalid': $v.requestAttribute.attributeType.$invalid }" v-model="$v.requestAttribute.attributeType.$model"  required/>
                        <div v-if="$v.requestAttribute.attributeType.$anyDirty && $v.requestAttribute.attributeType.$invalid">
                            <small class="form-text text-danger" v-if="!$v.requestAttribute.attributeType.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.requestAttribute.holdingRequestAttribute')" for="request-attribute-holdingRequestAttribute">Holding Request Attribute</label>
                        <select class="form-control" id="request-attribute-holdingRequestAttribute" name="holdingRequestAttribute" v-model="requestAttribute.holdingRequestAttribute">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="requestAttribute.holdingRequestAttribute && requestAttributeValueOption.id === requestAttribute.holdingRequestAttribute.id ? requestAttribute.holdingRequestAttribute : requestAttributeValueOption" v-for="requestAttributeValueOption in requestAttributeValues" :key="requestAttributeValueOption.id">{{requestAttributeValueOption.id}}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.requestAttribute.csr')" for="request-attribute-csr">Csr</label>
                        <select class="form-control" id="request-attribute-csr" name="csr" v-model="requestAttribute.csr">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="requestAttribute.csr && cSROption.id === requestAttribute.csr.id ? requestAttribute.csr : cSROption" v-for="cSROption in cSRS" :key="cSROption.id">{{cSROption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.requestAttribute.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./request-attribute-update.component.ts">
</script>
