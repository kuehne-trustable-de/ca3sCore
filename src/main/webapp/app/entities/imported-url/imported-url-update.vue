<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.importedURL.home.createOrEditLabel" v-text="$t('ca3SApp.importedURL.home.createOrEditLabel')">Create or edit a ImportedURL</h2>
                <div>
                    <div class="form-group" v-if="importedURL.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="importedURL.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.importedURL.name')" for="imported-url-name">Name</label>
                        <input type="text" class="form-control" name="name" id="imported-url-name"
                            :class="{'valid': !$v.importedURL.name.$invalid, 'invalid': $v.importedURL.name.$invalid }" v-model="$v.importedURL.name.$model"  required/>
                        <div v-if="$v.importedURL.name.$anyDirty && $v.importedURL.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.importedURL.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.importedURL.importDate')" for="imported-url-importDate">Import Date</label>
                        <div class="input-group">
                            <input id="imported-url-importDate" type="date" class="form-control" name="importDate"  :class="{'valid': !$v.importedURL.importDate.$invalid, 'invalid': $v.importedURL.importDate.$invalid }"
                            v-model="$v.importedURL.importDate.$model"  required />
                        </div>
                        <div v-if="$v.importedURL.importDate.$anyDirty && $v.importedURL.importDate.$invalid">
                            <small class="form-text text-danger" v-if="!$v.importedURL.importDate.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.importedURL.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./imported-url-update.component.ts">
</script>
