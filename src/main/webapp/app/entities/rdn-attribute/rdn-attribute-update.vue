<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.rDNAttribute.home.createOrEditLabel" v-text="$t('ca3SApp.rDNAttribute.home.createOrEditLabel')">Create or edit a RDNAttribute</h2>
                <div>
                    <div class="form-group" v-if="rDNAttribute.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="rDNAttribute.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.rDNAttribute.attributeType')" for="rdn-attribute-attributeType">Attribute Type</label>
                        <input type="text" class="form-control" name="attributeType" id="rdn-attribute-attributeType"
                            :class="{'valid': !$v.rDNAttribute.attributeType.$invalid, 'invalid': $v.rDNAttribute.attributeType.$invalid }" v-model="$v.rDNAttribute.attributeType.$model"  required/>
                        <div v-if="$v.rDNAttribute.attributeType.$anyDirty && $v.rDNAttribute.attributeType.$invalid">
                            <small class="form-text text-danger" v-if="!$v.rDNAttribute.attributeType.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.rDNAttribute.attributeValue')" for="rdn-attribute-attributeValue">Attribute Value</label>
                        <input type="text" class="form-control" name="attributeValue" id="rdn-attribute-attributeValue"
                            :class="{'valid': !$v.rDNAttribute.attributeValue.$invalid, 'invalid': $v.rDNAttribute.attributeValue.$invalid }" v-model="$v.rDNAttribute.attributeValue.$model"  required/>
                        <div v-if="$v.rDNAttribute.attributeValue.$anyDirty && $v.rDNAttribute.attributeValue.$invalid">
                            <small class="form-text text-danger" v-if="!$v.rDNAttribute.attributeValue.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.rDNAttribute.rdn')" for="rdn-attribute-rdn">Rdn</label>
                        <select class="form-control" id="rdn-attribute-rdn" name="rdn" v-model="rDNAttribute.rdn">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="rDNAttribute.rdn && rDNOption.id === rDNAttribute.rdn.id ? rDNAttribute.rdn : rDNOption" v-for="rDNOption in rDNS" :key="rDNOption.id">{{rDNOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.rDNAttribute.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./rdn-attribute-update.component.ts">
</script>
