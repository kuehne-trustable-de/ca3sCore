<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.authorization.home.createOrEditLabel" v-text="$t('ca3SApp.authorization.home.createOrEditLabel')">Create or edit a Authorization</h2>
                <div>
                    <div class="form-group" v-if="authorization.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="authorization.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.authorization.authorizationId')" for="authorization-authorizationId">Authorization Id</label>
                        <input type="number" class="form-control" name="authorizationId" id="authorization-authorizationId"
                            :class="{'valid': !$v.authorization.authorizationId.$invalid, 'invalid': $v.authorization.authorizationId.$invalid }" v-model.number="$v.authorization.authorizationId.$model"  required/>
                        <div v-if="$v.authorization.authorizationId.$anyDirty && $v.authorization.authorizationId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.authorization.authorizationId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.authorization.authorizationId.number" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.authorization.type')" for="authorization-type">Type</label>
                        <input type="text" class="form-control" name="type" id="authorization-type"
                            :class="{'valid': !$v.authorization.type.$invalid, 'invalid': $v.authorization.type.$invalid }" v-model="$v.authorization.type.$model"  required/>
                        <div v-if="$v.authorization.type.$anyDirty && $v.authorization.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.authorization.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.authorization.value')" for="authorization-value">Value</label>
                        <input type="text" class="form-control" name="value" id="authorization-value"
                            :class="{'valid': !$v.authorization.value.$invalid, 'invalid': $v.authorization.value.$invalid }" v-model="$v.authorization.value.$model"  required/>
                        <div v-if="$v.authorization.value.$anyDirty && $v.authorization.value.$invalid">
                            <small class="form-text text-danger" v-if="!$v.authorization.value.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.authorization.order')" for="authorization-order">Order</label>
                        <select class="form-control" id="authorization-order" name="order" v-model="authorization.order">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="authorization.order && acmeOrderOption.id === authorization.order.id ? authorization.order : acmeOrderOption" v-for="acmeOrderOption in acmeOrders" :key="acmeOrderOption.id">{{acmeOrderOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.authorization.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./authorization-update.component.ts">
</script>
