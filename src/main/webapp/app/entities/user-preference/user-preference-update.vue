<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.userPreference.home.createOrEditLabel" v-text="$t('ca3SApp.userPreference.home.createOrEditLabel')">Create or edit a UserPreference</h2>
                <div>
                    <div class="form-group" v-if="userPreference.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="userPreference.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.userPreference.userId')" for="user-preference-userId">User Id</label>
                        <input type="number" class="form-control" name="userId" id="user-preference-userId"
                            :class="{'valid': !$v.userPreference.userId.$invalid, 'invalid': $v.userPreference.userId.$invalid }" v-model.number="$v.userPreference.userId.$model"  required/>
                        <div v-if="$v.userPreference.userId.$anyDirty && $v.userPreference.userId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.userPreference.userId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.userPreference.userId.numeric" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.userPreference.name')" for="user-preference-name">Name</label>
                        <input type="text" class="form-control" name="name" id="user-preference-name"
                            :class="{'valid': !$v.userPreference.name.$invalid, 'invalid': $v.userPreference.name.$invalid }" v-model="$v.userPreference.name.$model"  required/>
                        <div v-if="$v.userPreference.name.$anyDirty && $v.userPreference.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.userPreference.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.userPreference.content')" for="user-preference-content">Content</label>
                        <textarea class="form-control" name="content" id="user-preference-content"
                            :class="{'valid': !$v.userPreference.content.$invalid, 'invalid': $v.userPreference.content.$invalid }" v-model="$v.userPreference.content.$model"  required></textarea>
                        <div v-if="$v.userPreference.content.$anyDirty && $v.userPreference.content.$invalid">
                            <small class="form-text text-danger" v-if="!$v.userPreference.content.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.userPreference.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./user-preference-update.component.ts">
</script>
