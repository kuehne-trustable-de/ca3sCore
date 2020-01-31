<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.acmeAuthorization.home.createOrEditLabel" v-text="$t('ca3SApp.acmeAuthorization.home.createOrEditLabel')">Create or edit a AcmeAuthorization</h2>
                <div>
                    <div class="form-group" v-if="acmeAuthorization.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="acmeAuthorization.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeAuthorization.acmeAuthorizationId')" for="acme-authorization-acmeAuthorizationId">Acme Authorization Id</label>
                        <input type="number" class="form-control" name="acmeAuthorizationId" id="acme-authorization-acmeAuthorizationId"
                            :class="{'valid': !$v.acmeAuthorization.acmeAuthorizationId.$invalid, 'invalid': $v.acmeAuthorization.acmeAuthorizationId.$invalid }" v-model.number="$v.acmeAuthorization.acmeAuthorizationId.$model"  required/>
                        <div v-if="$v.acmeAuthorization.acmeAuthorizationId.$anyDirty && $v.acmeAuthorization.acmeAuthorizationId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeAuthorization.acmeAuthorizationId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.acmeAuthorization.acmeAuthorizationId.number" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeAuthorization.type')" for="acme-authorization-type">Type</label>
                        <input type="text" class="form-control" name="type" id="acme-authorization-type"
                            :class="{'valid': !$v.acmeAuthorization.type.$invalid, 'invalid': $v.acmeAuthorization.type.$invalid }" v-model="$v.acmeAuthorization.type.$model"  required/>
                        <div v-if="$v.acmeAuthorization.type.$anyDirty && $v.acmeAuthorization.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeAuthorization.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeAuthorization.value')" for="acme-authorization-value">Value</label>
                        <input type="text" class="form-control" name="value" id="acme-authorization-value"
                            :class="{'valid': !$v.acmeAuthorization.value.$invalid, 'invalid': $v.acmeAuthorization.value.$invalid }" v-model="$v.acmeAuthorization.value.$model"  required/>
                        <div v-if="$v.acmeAuthorization.value.$anyDirty && $v.acmeAuthorization.value.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeAuthorization.value.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.acmeAuthorization.order')" for="acme-authorization-order">Order</label>
                        <select class="form-control" id="acme-authorization-order" name="order" v-model="acmeAuthorization.order">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="acmeAuthorization.order && acmeOrderOption.id === acmeAuthorization.order.id ? acmeAuthorization.order : acmeOrderOption" v-for="acmeOrderOption in acmeOrders" :key="acmeOrderOption.id">{{acmeOrderOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.acmeAuthorization.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./acme-authorization-update.component.ts">
</script>
