<template>
    <div>
        <h2 id="page-heading" data-cy="TenantHeading">
            <span v-text="$t('ca3SApp.tenant.home.title')" id="tenant-heading">Tenants</span>
            <div class="d-flex justify-content-end">
                <!--button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
                  <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
                  <span v-text="$t('ca3SApp.tenant.home.refreshListLabel')">Refresh List</span>
                </button-->
                <router-link :to="{ name: 'TenantNew' }" tag="button" id="jh-create-tenant"
                             class="btn btn-primary float-right jh-create-entity create-tenant">
                    <font-awesome-icon icon="plus"></font-awesome-icon>
                    <span v-text="$t('ca3SApp.tenant.home.createLabel')"> Create a new Tenant </span>
                </router-link>
            </div>
        </h2>
        <br/>
        <div class="alert alert-warning" v-if="!isFetching && tenants && tenants.length === 0">
            <span v-text="$t('ca3SApp.tenant.home.notFound')">No tenants found</span>
        </div>
        <div class="table-responsive" v-if="tenants && tenants.length > 0">
            <table class="table table-striped" aria-describedby="tenants">
                <thead>
                <tr>
                    <th scope="row"><span v-text="$t('global.field.id')">ID</span></th>
                    <th scope="row"><span v-text="$t('ca3SApp.tenant.name')">Name</span></th>
                    <th scope="row"><span v-text="$t('ca3SApp.tenant.longname')">Longname</span></th>
                    <th scope="row"><span v-text="$t('ca3SApp.tenant.active')">Active</span></th>
                    <th scope="row"></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="tenant in tenants" :key="tenant.id" data-cy="entityTable">
                    <td @click="$router.push({name: 'TenantEdit', params: {tenantId: tenant.id}})">{{ tenant.id }}</td>
                    <td @click="$router.push({name: 'TenantEdit', params: {tenantId: tenant.id}})">{{
                            tenant.name
                        }}
                    </td>
                    <td @click="$router.push({name: 'TenantEdit', params: {tenantId: tenant.id}})">{{
                            tenant.longname
                        }}
                    </td>
                    <td @click="$router.push({name: 'TenantEdit', params: {tenantId: tenant.id}})">{{
                            tenant.active
                        }}
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <b-button
                                v-on:click="prepareRemove(tenant)"
                                variant="danger"
                                class="btn btn-sm"
                                data-cy="entityDeleteButton"
                                v-b-modal.removeEntity>
                                <font-awesome-icon icon="times"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
                            </b-button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <b-modal ref="removeEntity" id="removeEntity">
          <span slot="modal-title">
            <span id="ca3SApp.tenant.delete.question" data-cy="tenantDeleteDialogHeading" v-text="$t('entity.delete.title')">Confirm delete operation</span>
          </span>
            <div class="modal-body">
                <p id="jhi-delete-tenant-heading" v-text="$t('ca3SApp.tenant.delete.question', { id: removeId })">
                    Are you sure you want to delete this Tenant?
                </p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')"
                        v-on:click="closeDialog()">Cancel
                </button>
                <button
                    type="button"
                    class="btn btn-primary"
                    id="jhi-confirm-delete-tenant"
                    data-cy="entityConfirmDeleteButton"
                    v-text="$t('entity.action.delete')"
                    v-on:click="removeTenant()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./tenant.component.ts"></script>
