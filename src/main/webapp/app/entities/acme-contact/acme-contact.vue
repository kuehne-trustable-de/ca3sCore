<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.acmeContact.home.title')" id="acme-contact-heading">Acme Contacts</span>
            <router-link :to="{name: 'AcmeContactCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-acme-contact">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.acmeContact.home.createLabel')">
                    Create a new Acme Contact
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
        <div class="alert alert-warning" v-if="!isFetching && acmeContacts && acmeContacts.length === 0">
            <span v-text="$t('ca3SApp.acmeContact.home.notFound')">No acmeContacts found</span>
        </div>
        <div class="table-responsive" v-if="acmeContacts && acmeContacts.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.acmeContact.contactId')">Contact Id</span></th>
                    <th><span v-text="$t('ca3SApp.acmeContact.contactUrl')">Contact Url</span></th>
                    <th><span v-text="$t('ca3SApp.acmeContact.account')">Account</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="acmeContact in acmeContacts"
                    :key="acmeContact.id">
                    <td>
                        <router-link :to="{name: 'AcmeContactView', params: {acmeContactId: acmeContact.id}}">{{acmeContact.id}}</router-link>
                    </td>
                    <td>{{acmeContact.contactId}}</td>
                    <td>{{acmeContact.contactUrl}}</td>
                    <td>
                        <div v-if="acmeContact.account">
                            <router-link :to="{name: 'ACMEAccountView', params: {aCMEAccountId: acmeContact.account.id}}">{{acmeContact.account.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'AcmeContactView', params: {acmeContactId: acmeContact.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'AcmeContactEdit', params: {acmeContactId: acmeContact.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(acmeContact)"
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
            <span slot="modal-title"><span id="ca3SApp.acmeContact.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-acmeContact-heading" v-text="$t('ca3SApp.acmeContact.delete.question', {'id': removeId})">Are you sure you want to delete this Acme Contact?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-acmeContact" v-text="$t('entity.action.delete')" v-on:click="removeAcmeContact()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./acme-contact.component.ts">
</script>
