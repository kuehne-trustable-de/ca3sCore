<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.rDN.home.createOrEditLabel" v-text="$t('ca3SApp.rDN.home.createOrEditLabel')">Create or edit a RDN</h2>
                <div>
                    <div class="form-group" v-if="rDN.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="rDN.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.rDN.csr')" for="rdn-csr">Csr</label>
                        <select class="form-control" id="rdn-csr" name="csr" v-model="rDN.csr">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="rDN.csr && cSROption.id === rDN.csr.id ? rDN.csr : cSROption" v-for="cSROption in cSRS" :key="cSROption.id">{{cSROption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.rDN.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./rdn-update.component.ts">
</script>
