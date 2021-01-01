<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.identifier.home.createOrEditLabel" v-text="$t('ca3SApp.identifier.home.createOrEditLabel')">Create or edit a Identifier</h2>
                <div>
                    <div class="form-group" v-if="identifier.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="identifier.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.identifier.identifierId')" for="identifier-identifierId">Identifier Id</label>
                        <input type="number" class="form-control" name="identifierId" id="identifier-identifierId"
                            :class="{'valid': !$v.identifier.identifierId.$invalid, 'invalid': $v.identifier.identifierId.$invalid }" v-model.number="$v.identifier.identifierId.$model"  required/>
                        <div v-if="$v.identifier.identifierId.$anyDirty && $v.identifier.identifierId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.identifier.identifierId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.identifier.identifierId.numeric" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.identifier.type')" for="identifier-type">Type</label>
                        <input type="text" class="form-control" name="type" id="identifier-type"
                            :class="{'valid': !$v.identifier.type.$invalid, 'invalid': $v.identifier.type.$invalid }" v-model="$v.identifier.type.$model"  required/>
                        <div v-if="$v.identifier.type.$anyDirty && $v.identifier.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.identifier.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.identifier.value')" for="identifier-value">Value</label>
                        <input type="text" class="form-control" name="value" id="identifier-value"
                            :class="{'valid': !$v.identifier.value.$invalid, 'invalid': $v.identifier.value.$invalid }" v-model="$v.identifier.value.$model"  required/>
                        <div v-if="$v.identifier.value.$anyDirty && $v.identifier.value.$invalid">
                            <small class="form-text text-danger" v-if="!$v.identifier.value.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.identifier.order')" for="identifier-order">Order</label>
                        <select class="form-control" id="identifier-order" name="order" v-model="identifier.order">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="identifier.order && acmeOrderOption.id === identifier.order.id ? identifier.order : acmeOrderOption" v-for="acmeOrderOption in acmeOrders" :key="acmeOrderOption.id">{{acmeOrderOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.identifier.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./identifier-update.component.ts">
</script>
