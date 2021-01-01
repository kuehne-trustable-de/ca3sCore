<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.acmeContact.home.createOrEditLabel" v-text="$t('ca3SApp.acmeContact.home.createOrEditLabel')">Create or edit a AcmeContact</h2>
                <div>
                    <div class="form-group" v-if="acmeContact.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="acmeContact.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeContact.contactId')" for="acme-contact-contactId">Contact Id</label>
                        <input type="number" class="form-control" name="contactId" id="acme-contact-contactId"
                            :class="{'valid': !$v.acmeContact.contactId.$invalid, 'invalid': $v.acmeContact.contactId.$invalid }" v-model.number="$v.acmeContact.contactId.$model"  required/>
                        <div v-if="$v.acmeContact.contactId.$anyDirty && $v.acmeContact.contactId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeContact.contactId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.acmeContact.contactId.numeric" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeContact.contactUrl')" for="acme-contact-contactUrl">Contact Url</label>
                        <input type="text" class="form-control" name="contactUrl" id="acme-contact-contactUrl"
                            :class="{'valid': !$v.acmeContact.contactUrl.$invalid, 'invalid': $v.acmeContact.contactUrl.$invalid }" v-model="$v.acmeContact.contactUrl.$model"  required/>
                        <div v-if="$v.acmeContact.contactUrl.$anyDirty && $v.acmeContact.contactUrl.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeContact.contactUrl.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeContact.account')" for="acme-contact-account">Account</label>
                        <select class="form-control" id="acme-contact-account" name="account" v-model="acmeContact.account">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="acmeContact.account && aCMEAccountOption.id === acmeContact.account.id ? acmeContact.account : aCMEAccountOption" v-for="aCMEAccountOption in aCMEAccounts" :key="aCMEAccountOption.id">{{aCMEAccountOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.acmeContact.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./acme-contact-update.component.ts">
</script>
