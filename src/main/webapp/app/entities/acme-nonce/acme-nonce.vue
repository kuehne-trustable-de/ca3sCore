<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.acmeNonce.home.title')" id="acme-nonce-heading">Acme Nonces</span>
            <router-link :to="{name: 'AcmeNonceCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-acme-nonce">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.acmeNonce.home.createLabel')">
                    Create a new Acme Nonce
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
        <div class="alert alert-warning" v-if="!isFetching && acmeNonces && acmeNonces.length === 0">
            <span v-text="$t('ca3SApp.acmeNonce.home.notFound')">No acmeNonces found</span>
        </div>
        <div class="table-responsive" v-if="acmeNonces && acmeNonces.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.acmeNonce.nonceValue')">Nonce Value</span></th>
                    <th><span v-text="$t('ca3SApp.acmeNonce.expiresAt')">Expires At</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="acmeNonce in acmeNonces"
                    :key="acmeNonce.id">
                    <td>
                        <router-link :to="{name: 'AcmeNonceView', params: {acmeNonceId: acmeNonce.id}}">{{acmeNonce.id}}</router-link>
                    </td>
                    <td>{{acmeNonce.nonceValue}}</td>
                    <td v-if="acmeNonce.expiresAt"> {{$d(Date.parse(acmeNonce.expiresAt), 'short') }}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'AcmeNonceView', params: {acmeNonceId: acmeNonce.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'AcmeNonceEdit', params: {acmeNonceId: acmeNonce.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(acmeNonce)"
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
            <span slot="modal-title"><span id="ca3SApp.acmeNonce.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-acmeNonce-heading" v-bind:title="$t('ca3SApp.acmeNonce.delete.question')">Are you sure you want to delete this Acme Nonce?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-acmeNonce" v-text="$t('entity.action.delete')" v-on:click="removeAcmeNonce()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./acme-nonce.component.ts">
</script>
