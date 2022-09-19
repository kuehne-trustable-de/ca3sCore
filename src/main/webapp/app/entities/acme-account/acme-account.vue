<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.aCMEAccount.home.title')" id="acme-account-heading">ACME Accounts</span>
            <router-link :to="{name: 'AcmeAccountCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-acme-account">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.aCMEAccount.home.createLabel')">
                    Create a new ACME Account
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
        <div class="alert alert-warning" v-if="!isFetching && aCMEAccounts && aCMEAccounts.length === 0">
            <span v-text="$t('ca3SApp.aCMEAccount.home.notFound')">No aCMEAccounts found</span>
        </div>
        <div class="table-responsive" v-if="aCMEAccounts && aCMEAccounts.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.aCMEAccount.accountId')">Account Id</span></th>
                    <th><span v-text="$t('ca3SApp.aCMEAccount.realm')">Realm</span></th>
                    <th><span v-text="$t('ca3SApp.aCMEAccount.status')">Status</span></th>
                    <th><span v-text="$t('ca3SApp.aCMEAccount.termsOfServiceAgreed')">Terms Of Service Agreed</span></th>
                    <th><span v-text="$t('ca3SApp.aCMEAccount.publicKeyHash')">Public Key Hash</span></th>
                    <th><span v-text="$t('ca3SApp.aCMEAccount.publicKey')">Public Key</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="aCMEAccount in aCMEAccounts"
                    :key="aCMEAccount.id">
                    <td>
                        <router-link :to="{name: 'AcmeAccountView', params: {aCMEAccountId: aCMEAccount.id}}">{{aCMEAccount.id}}</router-link>
                    </td>
                    <td>{{aCMEAccount.accountId}}</td>
                    <td>{{aCMEAccount.realm}}</td>
                    <td v-text="$t('ca3SApp.AccountStatus.' + aCMEAccount.status)">{{aCMEAccount.status}}</td>
                    <td>{{aCMEAccount.termsOfServiceAgreed}}</td>
                    <td>{{aCMEAccount.publicKeyHash}}</td>
                    <td>{{aCMEAccount.publicKey}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'AcmeAccountView', params: {aCMEAccountId: aCMEAccount.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'AcmeAccountEdit', params: {aCMEAccountId: aCMEAccount.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(aCMEAccount)"
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
            <span slot="modal-title"><span id="ca3SApp.aCMEAccount.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-aCMEAccount-heading" v-bind:title="$t('ca3SApp.aCMEAccount.delete.question')">Are you sure you want to delete this ACME Account?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-aCMEAccount" v-text="$t('entity.action.delete')" v-on:click="removeAcmeAccount()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./acme-account.component.ts">
</script>
