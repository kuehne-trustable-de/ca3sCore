<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.aCMEAccount.home.createOrEditLabel" v-text="$t('ca3SApp.aCMEAccount.home.createOrEditLabel')">Create or edit a ACMEAccount</h2>
                <div>
                    <div class="form-group" v-if="aCMEAccount.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="aCMEAccount.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.aCMEAccount.accountId')" for="acme-account-accountId">Account Id</label>
                        <input type="number" class="form-control" name="accountId" id="acme-account-accountId"
                            :class="{'valid': !$v.aCMEAccount.accountId.$invalid, 'invalid': $v.aCMEAccount.accountId.$invalid }" v-model.number="$v.aCMEAccount.accountId.$model"  required/>
                        <div v-if="$v.aCMEAccount.accountId.$anyDirty && $v.aCMEAccount.accountId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.aCMEAccount.accountId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.aCMEAccount.accountId.numeric" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.aCMEAccount.realm')" for="acme-account-realm">Realm</label>
                        <input type="text" class="form-control" name="realm" id="acme-account-realm"
                            :class="{'valid': !$v.aCMEAccount.realm.$invalid, 'invalid': $v.aCMEAccount.realm.$invalid }" v-model="$v.aCMEAccount.realm.$model"  required/>
                        <div v-if="$v.aCMEAccount.realm.$anyDirty && $v.aCMEAccount.realm.$invalid">
                            <small class="form-text text-danger" v-if="!$v.aCMEAccount.realm.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.aCMEAccount.status')" for="acme-account-status">Status</label>
                        <select class="form-control" name="status" :class="{'valid': !$v.aCMEAccount.status.$invalid, 'invalid': $v.aCMEAccount.status.$invalid }" v-model="$v.aCMEAccount.status.$model" id="acme-account-status" >
                            <option value="VALID" v-bind:label="$t('ca3SApp.AccountStatus.VALID')">VALID</option>
                            <option value="DEACTIVATED" v-bind:label="$t('ca3SApp.AccountStatus.DEACTIVATED')">DEACTIVATED</option>
                            <option value="REVOKED" v-bind:label="$t('ca3SApp.AccountStatus.REVOKED')">REVOKED</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.aCMEAccount.termsOfServiceAgreed')" for="acme-account-termsOfServiceAgreed">Terms Of Service Agreed</label>
                        <input type="checkbox" class="form-check" name="termsOfServiceAgreed" id="acme-account-termsOfServiceAgreed"
                            :class="{'valid': !$v.aCMEAccount.termsOfServiceAgreed.$invalid, 'invalid': $v.aCMEAccount.termsOfServiceAgreed.$invalid }" v-model="$v.aCMEAccount.termsOfServiceAgreed.$model"  required/>
                        <div v-if="$v.aCMEAccount.termsOfServiceAgreed.$anyDirty && $v.aCMEAccount.termsOfServiceAgreed.$invalid">
                            <small class="form-text text-danger" v-if="!$v.aCMEAccount.termsOfServiceAgreed.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.aCMEAccount.publicKeyHash')" for="acme-account-publicKeyHash">Public Key Hash</label>
                        <input type="text" class="form-control" name="publicKeyHash" id="acme-account-publicKeyHash"
                            :class="{'valid': !$v.aCMEAccount.publicKeyHash.$invalid, 'invalid': $v.aCMEAccount.publicKeyHash.$invalid }" v-model="$v.aCMEAccount.publicKeyHash.$model"  required/>
                        <div v-if="$v.aCMEAccount.publicKeyHash.$anyDirty && $v.aCMEAccount.publicKeyHash.$invalid">
                            <small class="form-text text-danger" v-if="!$v.aCMEAccount.publicKeyHash.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.aCMEAccount.publicKey')" for="acme-account-publicKey">Public Key</label>
                        <textarea class="form-control" name="publicKey" id="acme-account-publicKey"
                            :class="{'valid': !$v.aCMEAccount.publicKey.$invalid, 'invalid': $v.aCMEAccount.publicKey.$invalid }" v-model="$v.aCMEAccount.publicKey.$model"  required></textarea>
                        <div v-if="$v.aCMEAccount.publicKey.$anyDirty && $v.aCMEAccount.publicKey.$invalid">
                            <small class="form-text text-danger" v-if="!$v.aCMEAccount.publicKey.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.aCMEAccount.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./acme-account-update.component.ts">
</script>
