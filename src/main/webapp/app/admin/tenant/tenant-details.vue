<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="tenant">
        <h2 class="jh-entity-heading" data-cy="tenantDetailsHeading">
          <span v-text="$t('ca3SApp.tenant.detail.title')">Tenant</span> {{ tenant.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('ca3SApp.tenant.name')">Name</span>
          </dt>
          <dd>
            <span>{{ tenant.name }}</span>
          </dd>
          <dt>
            <span v-text="$t('ca3SApp.tenant.longname')">Longname</span>
          </dt>
          <dd>
            <span>{{ tenant.longname }}</span>
          </dd>
          <dt>
            <span v-text="$t('ca3SApp.tenant.active')">Active</span>
          </dt>
          <dd>
            <span>{{ tenant.active }}</span>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link v-if="tenant.id" :to="{ name: 'TenantEdit', params: { tenantId: tenant.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./tenant-details.component.ts"></script>
