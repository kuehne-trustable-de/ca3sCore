<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.protectedContent.home.createOrEditLabel" v-text="$t('ca3SApp.protectedContent.home.createOrEditLabel')">Create or edit a ProtectedContent</h2>
                <div>
                    <div class="form-group" v-if="protectedContent.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="protectedContent.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.protectedContent.contentBase64')" for="protected-content-contentBase64">Content Base 64</label>
                        <textarea class="form-control" name="contentBase64" id="protected-content-contentBase64"
                            :class="{'valid': !$v.protectedContent.contentBase64.$invalid, 'invalid': $v.protectedContent.contentBase64.$invalid }" v-model="$v.protectedContent.contentBase64.$model"  required></textarea>
                        <div v-if="$v.protectedContent.contentBase64.$anyDirty && $v.protectedContent.contentBase64.$invalid">
                            <small class="form-text text-danger" v-if="!$v.protectedContent.contentBase64.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.protectedContent.type')" for="protected-content-type">Type</label>
                        <select class="form-control" name="type" :class="{'valid': !$v.protectedContent.type.$invalid, 'invalid': $v.protectedContent.type.$invalid }" v-model="$v.protectedContent.type.$model" id="protected-content-type"  required>
                            <option value="KEY" v-bind:label="$t('ca3SApp.ProtectedContentType.KEY')">KEY</option>
                            <option value="SECRET" v-bind:label="$t('ca3SApp.ProtectedContentType.SECRET')">SECRET</option>
                            <option value="PASSWORD" v-bind:label="$t('ca3SApp.ProtectedContentType.PASSWORD')">PASSWORD</option>
                        </select>
                        <div v-if="$v.protectedContent.type.$anyDirty && $v.protectedContent.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.protectedContent.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.protectedContent.relationType')" for="protected-content-relationType">Relation Type</label>
                        <select class="form-control" name="relationType" :class="{'valid': !$v.protectedContent.relationType.$invalid, 'invalid': $v.protectedContent.relationType.$invalid }" v-model="$v.protectedContent.relationType.$model" id="protected-content-relationType" >
                            <option value="CERTIFICATE" v-bind:label="$t('ca3SApp.ContentRelationType.CERTIFICATE')">CERTIFICATE</option>
                            <option value="CONNECTION" v-bind:label="$t('ca3SApp.ContentRelationType.CONNECTION')">CONNECTION</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.protectedContent.relatedId')" for="protected-content-relatedId">Related Id</label>
                        <input type="number" class="form-control" name="relatedId" id="protected-content-relatedId"
                            :class="{'valid': !$v.protectedContent.relatedId.$invalid, 'invalid': $v.protectedContent.relatedId.$invalid }" v-model.number="$v.protectedContent.relatedId.$model" />
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.protectedContent.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./protected-content-update.component.ts">
</script>
