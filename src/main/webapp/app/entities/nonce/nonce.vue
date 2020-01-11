<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.nonce.home.title')" id="nonce-heading">Nonces</span>
            <router-link :to="{name: 'NonceCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-nonce">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.nonce.home.createLabel')">
                    Create a new Nonce
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
        <div class="alert alert-warning" v-if="!isFetching && nonces && nonces.length === 0">
            <span v-text="$t('ca3SApp.nonce.home.notFound')">No nonces found</span>
        </div>
        <div class="table-responsive" v-if="nonces && nonces.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.nonce.nonceValue')">Nonce Value</span></th>
                    <th><span v-text="$t('ca3SApp.nonce.expiresAt')">Expires At</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="nonce in nonces"
                    :key="nonce.id">
                    <td>
                        <router-link :to="{name: 'NonceView', params: {nonceId: nonce.id}}">{{nonce.id}}</router-link>
                    </td>
                    <td>{{nonce.nonceValue}}</td>
                    <td>{{nonce.expiresAt}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'NonceView', params: {nonceId: nonce.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'NonceEdit', params: {nonceId: nonce.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(nonce)"
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
            <span slot="modal-title"><span id="ca3SApp.nonce.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-nonce-heading" v-bind:title="$t('ca3SApp.nonce.delete.question')">Are you sure you want to delete this Nonce?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-nonce" v-text="$t('entity.action.delete')" v-on:click="removeNonce()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./nonce.component.ts">
</script>
