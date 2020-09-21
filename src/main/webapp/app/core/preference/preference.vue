<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.preference.home.createOrEditLabel" v-text="$t('ca3SApp.preference.home.createOrEditLabel')">Create or edit a Preference</h2>
                <div>
                    <div class="form-group" v-if="preference.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="preference.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.preference.userId')" for="user-preference-userId">User Id</label>
                        <input type="number" class="form-control" name="userId" id="user-preference-userId"
                            :class="{'valid': !$v.preference.userId.$invalid, 'invalid': $v.preference.userId.$invalid }" v-model.number="$v.preference.userId.$model"  required/>
                        <div v-if="$v.preference.userId.$anyDirty && $v.preference.userId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.preference.userId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.preference.userId.numeric" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.preference.name')" for="user-preference-name">Name</label>
                        <input type="text" class="form-control" name="name" id="user-preference-name"
                            :class="{'valid': !$v.preference.name.$invalid, 'invalid': $v.preference.name.$invalid }" v-model="$v.preference.name.$model"  required/>
                        <div v-if="$v.preference.name.$anyDirty && $v.preference.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.preference.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.preference.content')" for="user-preference-content">Content</label>
                        <textarea class="form-control" name="content" id="user-preference-content"
                            :class="{'valid': !$v.preference.content.$invalid, 'invalid': $v.preference.content.$invalid }" v-model="$v.preference.content.$model"  required></textarea>
                        <div v-if="$v.preference.content.$anyDirty && $v.preference.content.$invalid">
                            <small class="form-text text-danger" v-if="!$v.preference.content.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.preference.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./preference.component.ts">
</script>
