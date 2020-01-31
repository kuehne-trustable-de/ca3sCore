<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.acmeIdentifier.home.createOrEditLabel" v-text="$t('ca3SApp.acmeIdentifier.home.createOrEditLabel')">Create or edit a AcmeIdentifier</h2>
                <div>
                    <div class="form-group" v-if="acmeIdentifier.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="acmeIdentifier.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeIdentifier.acmeIdentifierId')" for="acme-identifier-acmeIdentifierId">Acme Identifier Id</label>
                        <input type="number" class="form-control" name="acmeIdentifierId" id="acme-identifier-acmeIdentifierId"
                            :class="{'valid': !$v.acmeIdentifier.acmeIdentifierId.$invalid, 'invalid': $v.acmeIdentifier.acmeIdentifierId.$invalid }" v-model.number="$v.acmeIdentifier.acmeIdentifierId.$model"  required/>
                        <div v-if="$v.acmeIdentifier.acmeIdentifierId.$anyDirty && $v.acmeIdentifier.acmeIdentifierId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeIdentifier.acmeIdentifierId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.acmeIdentifier.acmeIdentifierId.number" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeIdentifier.type')" for="acme-identifier-type">Type</label>
                        <input type="text" class="form-control" name="type" id="acme-identifier-type"
                            :class="{'valid': !$v.acmeIdentifier.type.$invalid, 'invalid': $v.acmeIdentifier.type.$invalid }" v-model="$v.acmeIdentifier.type.$model"  required/>
                        <div v-if="$v.acmeIdentifier.type.$anyDirty && $v.acmeIdentifier.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeIdentifier.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeIdentifier.value')" for="acme-identifier-value">Value</label>
                        <input type="text" class="form-control" name="value" id="acme-identifier-value"
                            :class="{'valid': !$v.acmeIdentifier.value.$invalid, 'invalid': $v.acmeIdentifier.value.$invalid }" v-model="$v.acmeIdentifier.value.$model"  required/>
                        <div v-if="$v.acmeIdentifier.value.$anyDirty && $v.acmeIdentifier.value.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeIdentifier.value.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.acmeIdentifier.order')" for="acme-identifier-order">Order</label>
                        <select class="form-control" id="acme-identifier-order" name="order" v-model="acmeIdentifier.order">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="acmeIdentifier.order && acmeOrderOption.id === acmeIdentifier.order.id ? acmeIdentifier.order : acmeOrderOption" v-for="acmeOrderOption in acmeOrders" :key="acmeOrderOption.id">{{acmeOrderOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.acmeIdentifier.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./acme-identifier-update.component.ts">
</script>
