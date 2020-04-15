<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.csrAttribute.home.createOrEditLabel" v-text="$t('ca3SApp.csrAttribute.home.createOrEditLabel')">Create or edit a CsrAttribute</h2>
                <div>
                    <div class="form-group" v-if="csrAttribute.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="csrAttribute.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.csrAttribute.name')" for="csr-attribute-name">Name</label>
                        <input type="text" class="form-control" name="name" id="csr-attribute-name"
                            :class="{'valid': !$v.csrAttribute.name.$invalid, 'invalid': $v.csrAttribute.name.$invalid }" v-model="$v.csrAttribute.name.$model"  required/>
                        <div v-if="$v.csrAttribute.name.$anyDirty && $v.csrAttribute.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.csrAttribute.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.csrAttribute.value')" for="csr-attribute-value">Value</label>
                        <input type="text" class="form-control" name="value" id="csr-attribute-value"
                            :class="{'valid': !$v.csrAttribute.value.$invalid, 'invalid': $v.csrAttribute.value.$invalid }" v-model="$v.csrAttribute.value.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.csrAttribute.csr')" for="csr-attribute-csr">Csr</label>
                        <select class="form-control" id="csr-attribute-csr" name="csr" v-model="csrAttribute.csr">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="csrAttribute.csr && cSROption.id === csrAttribute.csr.id ? csrAttribute.csr : cSROption" v-for="cSROption in cSRS" :key="cSROption.id">{{cSROption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.csrAttribute.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./csr-attribute-update.component.ts">
</script>
