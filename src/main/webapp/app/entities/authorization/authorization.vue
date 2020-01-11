<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.authorization.home.title')" id="authorization-heading">Authorizations</span>
            <router-link :to="{name: 'AuthorizationCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-authorization">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.authorization.home.createLabel')">
                    Create a new Authorization
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
        <div class="alert alert-warning" v-if="!isFetching && authorizations && authorizations.length === 0">
            <span v-text="$t('ca3SApp.authorization.home.notFound')">No authorizations found</span>
        </div>
        <div class="table-responsive" v-if="authorizations && authorizations.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.authorization.authorizationId')">Authorization Id</span></th>
                    <th><span v-text="$t('ca3SApp.authorization.type')">Type</span></th>
                    <th><span v-text="$t('ca3SApp.authorization.value')">Value</span></th>
                    <th><span v-text="$t('ca3SApp.authorization.order')">Order</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="authorization in authorizations"
                    :key="authorization.id">
                    <td>
                        <router-link :to="{name: 'AuthorizationView', params: {authorizationId: authorization.id}}">{{authorization.id}}</router-link>
                    </td>
                    <td>{{authorization.authorizationId}}</td>
                    <td>{{authorization.type}}</td>
                    <td>{{authorization.value}}</td>
                    <td>
                        <div v-if="authorization.order">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: authorization.order.id}}">{{authorization.order.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'AuthorizationView', params: {authorizationId: authorization.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'AuthorizationEdit', params: {authorizationId: authorization.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(authorization)"
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
            <span slot="modal-title"><span id="ca3SApp.authorization.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-authorization-heading" v-bind:title="$t('ca3SApp.authorization.delete.question')">Are you sure you want to delete this Authorization?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-authorization" v-text="$t('entity.action.delete')" v-on:click="removeAuthorization()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./authorization.component.ts">
</script>
