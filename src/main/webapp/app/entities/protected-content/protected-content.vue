<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.protectedContent.home.title')" id="protected-content-heading">Protected Contents</span>
            <router-link :to="{name: 'ProtectedContentCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-protected-content">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.protectedContent.home.createLabel')">
                    Create a new Protected Content
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
        <div class="alert alert-warning" v-if="!isFetching && protectedContents && protectedContents.length === 0">
            <span v-text="$t('ca3SApp.protectedContent.home.notFound')">No protectedContents found</span>
        </div>
        <div class="table-responsive" v-if="protectedContents && protectedContents.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.protectedContent.contentBase64')">Content Base 64</span></th>
                    <th><span v-text="$t('ca3SApp.protectedContent.type')">Type</span></th>
                    <th><span v-text="$t('ca3SApp.protectedContent.relationType')">Relation Type</span></th>
                    <th><span v-text="$t('ca3SApp.protectedContent.relatedId')">Related Id</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="protectedContent in protectedContents"
                    :key="protectedContent.id">
                    <td>
                        <router-link :to="{name: 'ProtectedContentView', params: {protectedContentId: protectedContent.id}}">{{protectedContent.id}}</router-link>
                    </td>
                    <td>{{protectedContent.contentBase64}}</td>
                    <td v-text="$t('ca3SApp.ProtectedContentType.' + protectedContent.type)">{{protectedContent.type}}</td>
                    <td v-text="$t('ca3SApp.ContentRelationType.' + protectedContent.relationType)">{{protectedContent.relationType}}</td>
                    <td>{{protectedContent.relatedId}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'ProtectedContentView', params: {protectedContentId: protectedContent.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'ProtectedContentEdit', params: {protectedContentId: protectedContent.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(protectedContent)"
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
            <span slot="modal-title"><span id="ca3SApp.protectedContent.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-protectedContent-heading" v-bind:title="$t('ca3SApp.protectedContent.delete.question')">Are you sure you want to delete this Protected Content?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-protectedContent" v-text="$t('entity.action.delete')" v-on:click="removeProtectedContent()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./protected-content.component.ts">
</script>
