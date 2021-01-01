<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.nonce.home.createOrEditLabel" v-text="$t('ca3SApp.nonce.home.createOrEditLabel')">Create or edit a Nonce</h2>
                <div>
                    <div class="form-group" v-if="nonce.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="nonce.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.nonce.nonceValue')" for="nonce-nonceValue">Nonce Value</label>
                        <input type="text" class="form-control" name="nonceValue" id="nonce-nonceValue"
                            :class="{'valid': !$v.nonce.nonceValue.$invalid, 'invalid': $v.nonce.nonceValue.$invalid }" v-model="$v.nonce.nonceValue.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.nonce.expiresAt')" for="nonce-expiresAt">Expires At</label>
                        <div class="input-group">
                            <input id="nonce-expiresAt" type="date" class="form-control" name="expiresAt"  :class="{'valid': !$v.nonce.expiresAt.$invalid, 'invalid': $v.nonce.expiresAt.$invalid }"
                            v-model="$v.nonce.expiresAt.$model"  />
                        </div>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.nonce.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./nonce-update.component.ts">
</script>
