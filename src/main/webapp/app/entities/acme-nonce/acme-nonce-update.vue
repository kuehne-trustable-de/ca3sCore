<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.acmeNonce.home.createOrEditLabel" v-text="$t('ca3SApp.acmeNonce.home.createOrEditLabel')">Create or edit a AcmeNonce</h2>
                <div>
                    <div class="form-group" v-if="acmeNonce.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="acmeNonce.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeNonce.nonceValue')" for="acme-nonce-nonceValue">Nonce Value</label>
                        <input type="text" class="form-control" name="nonceValue" id="acme-nonce-nonceValue"
                            :class="{'valid': !$v.acmeNonce.nonceValue.$invalid, 'invalid': $v.acmeNonce.nonceValue.$invalid }" v-model="$v.acmeNonce.nonceValue.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeNonce.expiresAt')" for="acme-nonce-expiresAt">Expires At</label>
                        <div class="input-group">
                            <input id="acme-nonce-expiresAt" type="date" class="form-control" name="expiresAt"  :class="{'valid': !$v.acmeNonce.expiresAt.$invalid, 'invalid': $v.acmeNonce.expiresAt.$invalid }"
                            v-model="$v.acmeNonce.expiresAt.$model"  />
                        </div>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.acmeNonce.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./acme-nonce-update.component.ts">
</script>
