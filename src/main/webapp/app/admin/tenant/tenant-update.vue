<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="ca3SApp.tenant.home.createOrEditLabel"
          data-cy="TenantCreateUpdateHeading"
          v-text="$t('ca3SApp.tenant.home.createOrEditLabel')"
        >
          Create or edit a Tenant
        </h2>
        <div>
          <div class="form-group" v-if="tenant.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="tenant.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('ca3SApp.tenant.name')" for="tenant-name">Name</label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="tenant-name"
              data-cy="name"
              :class="{ valid: !$v.tenant.name.$invalid, invalid: $v.tenant.name.$invalid }"
              v-model="$v.tenant.name.$model"
              required
            />
            <div v-if="$v.tenant.name.$anyDirty && $v.tenant.name.$invalid">
              <small class="form-text text-danger" v-if="!$v.tenant.name.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('ca3SApp.tenant.longname')" for="tenant-longname">Longname</label>
            <input
              type="text"
              class="form-control"
              name="longname"
              id="tenant-longname"
              data-cy="longname"
              :class="{ valid: !$v.tenant.longname.$invalid, invalid: $v.tenant.longname.$invalid }"
              v-model="$v.tenant.longname.$model"
              required
            />
            <div v-if="$v.tenant.longname.$anyDirty && $v.tenant.longname.$invalid">
              <small class="form-text text-danger" v-if="!$v.tenant.longname.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('ca3SApp.tenant.active')" for="tenant-active">Active</label>
            <input
              type="checkbox"
              class="form-check"
              name="active"
              id="tenant-active"
              data-cy="active"
              :class="{ valid: !$v.tenant.active.$invalid, invalid: $v.tenant.active.$invalid }"
              v-model="$v.tenant.active.$model"
            />
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" data-cy="entityCreateCancelButton" class="btn btn-secondary" v-on:click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="$v.tenant.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./tenant-update.component.ts"></script>
