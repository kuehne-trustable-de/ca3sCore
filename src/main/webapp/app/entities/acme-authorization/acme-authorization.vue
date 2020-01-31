<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.acmeAuthorization.home.title')" id="acme-authorization-heading">Acme Authorizations</span>
            <router-link :to="{name: 'AcmeAuthorizationCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-acme-authorization">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.acmeAuthorization.home.createLabel')">
                    Create a new Acme Authorization
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
        <div class="alert alert-warning" v-if="!isFetching && acmeAuthorizations && acmeAuthorizations.length === 0">
            <span v-text="$t('ca3SApp.acmeAuthorization.home.notFound')">No acmeAuthorizations found</span>
        </div>
        <div class="table-responsive" v-if="acmeAuthorizations && acmeAuthorizations.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.acmeAuthorization.acmeAuthorizationId')">Acme Authorization Id</span></th>
                    <th><span v-text="$t('ca3SApp.acmeAuthorization.type')">Type</span></th>
                    <th><span v-text="$t('ca3SApp.acmeAuthorization.value')">Value</span></th>
                    <th><span v-text="$t('ca3SApp.acmeAuthorization.order')">Order</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="acmeAuthorization in acmeAuthorizations"
                    :key="acmeAuthorization.id">
                    <td>
                        <router-link :to="{name: 'AcmeAuthorizationView', params: {acmeAuthorizationId: acmeAuthorization.id}}">{{acmeAuthorization.id}}</router-link>
                    </td>
                    <td>{{acmeAuthorization.acmeAuthorizationId}}</td>
                    <td>{{acmeAuthorization.type}}</td>
                    <td>{{acmeAuthorization.value}}</td>
                    <td>
                        <div v-if="acmeAuthorization.order">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: acmeAuthorization.order.id}}">{{acmeAuthorization.order.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'AcmeAuthorizationView', params: {acmeAuthorizationId: acmeAuthorization.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'AcmeAuthorizationEdit', params: {acmeAuthorizationId: acmeAuthorization.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(acmeAuthorization)"
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
            <span slot="modal-title"><span id="ca3SApp.acmeAuthorization.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-acmeAuthorization-heading" v-bind:title="$t('ca3SApp.acmeAuthorization.delete.question')">Are you sure you want to delete this Acme Authorization?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-acmeAuthorization" v-text="$t('entity.action.delete')" v-on:click="removeAcmeAuthorization()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./acme-authorization.component.ts">
</script>
