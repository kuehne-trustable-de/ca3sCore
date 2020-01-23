<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.importedURL.home.title')" id="imported-url-heading">Imported URLS</span>
            <router-link :to="{name: 'ImportedURLCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-imported-url">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.importedURL.home.createLabel')">
                    Create a new Imported URL
                </span>
            </router-link>
        </h2>
        <b-alert :show="dismissCountDown"
            dismissible
            :variant="alertType"
            @dismissed="dismissCountDown=0"
            @dismiss-count-down="countDownChanged">
            {{alertMessage}}
        </b-alert>
        <br/>
        <div class="alert alert-warning" v-if="!isFetching && importedURLS && importedURLS.length === 0">
            <span v-text="$t('ca3SApp.importedURL.home.notFound')">No importedURLS found</span>
        </div>
        <div class="table-responsive" v-if="importedURLS && importedURLS.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.importedURL.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.importedURL.importDate')">Import Date</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="importedURL in importedURLS"
                    :key="importedURL.id">
                    <td>
                        <router-link :to="{name: 'ImportedURLView', params: {importedURLId: importedURL.id}}">{{importedURL.id}}</router-link>
                    </td>
                    <td>{{importedURL.name}}</td>
                    <td>{{importedURL.importDate}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'ImportedURLView', params: {importedURLId: importedURL.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'ImportedURLEdit', params: {importedURLId: importedURL.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(importedURL)"
                                   variant="danger"
                                   class="btn btn-sm"
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
        <b-modal ref="removeEntity" id="removeEntity" >
            <span slot="modal-title"><span id="ca3SApp.importedURL.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-importedURL-heading" v-bind:title="$t('ca3SApp.importedURL.delete.question')">Are you sure you want to delete this Imported URL?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-importedURL" v-text="$t('entity.action.delete')" v-on:click="removeImportedURL()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./imported-url.component.ts">
</script>
