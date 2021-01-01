<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.requestAttributeValue.home.createOrEditLabel" v-text="$t('ca3SApp.requestAttributeValue.home.createOrEditLabel')">Create or edit a RequestAttributeValue</h2>
                <div>
                    <div class="form-group" v-if="requestAttributeValue.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="requestAttributeValue.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.requestAttributeValue.attributeValue')" for="request-attribute-value-attributeValue">Attribute Value</label>
                        <input type="text" class="form-control" name="attributeValue" id="request-attribute-value-attributeValue"
                            :class="{'valid': !$v.requestAttributeValue.attributeValue.$invalid, 'invalid': $v.requestAttributeValue.attributeValue.$invalid }" v-model="$v.requestAttributeValue.attributeValue.$model"  required/>
                        <div v-if="$v.requestAttributeValue.attributeValue.$anyDirty && $v.requestAttributeValue.attributeValue.$invalid">
                            <small class="form-text text-danger" v-if="!$v.requestAttributeValue.attributeValue.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.requestAttributeValue.reqAttr')" for="request-attribute-value-reqAttr">Req Attr</label>
                        <select class="form-control" id="request-attribute-value-reqAttr" name="reqAttr" v-model="requestAttributeValue.reqAttr">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="requestAttributeValue.reqAttr && requestAttributeOption.id === requestAttributeValue.reqAttr.id ? requestAttributeValue.reqAttr : requestAttributeOption" v-for="requestAttributeOption in requestAttributes" :key="requestAttributeOption.id">{{requestAttributeOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.requestAttributeValue.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./request-attribute-value-update.component.ts">
</script>
