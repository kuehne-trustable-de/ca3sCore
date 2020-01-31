<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.acmeIdentifier.home.title')" id="acme-identifier-heading">Acme Identifiers</span>
            <router-link :to="{name: 'AcmeIdentifierCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-acme-identifier">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.acmeIdentifier.home.createLabel')">
                    Create a new Acme Identifier
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
        <div class="alert alert-warning" v-if="!isFetching && acmeIdentifiers && acmeIdentifiers.length === 0">
            <span v-text="$t('ca3SApp.acmeIdentifier.home.notFound')">No acmeIdentifiers found</span>
        </div>
        <div class="table-responsive" v-if="acmeIdentifiers && acmeIdentifiers.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.acmeIdentifier.acmeIdentifierId')">Acme Identifier Id</span></th>
                    <th><span v-text="$t('ca3SApp.acmeIdentifier.type')">Type</span></th>
                    <th><span v-text="$t('ca3SApp.acmeIdentifier.value')">Value</span></th>
                    <th><span v-text="$t('ca3SApp.acmeIdentifier.order')">Order</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="acmeIdentifier in acmeIdentifiers"
                    :key="acmeIdentifier.id">
                    <td>
                        <router-link :to="{name: 'AcmeIdentifierView', params: {acmeIdentifierId: acmeIdentifier.id}}">{{acmeIdentifier.id}}</router-link>
                    </td>
                    <td>{{acmeIdentifier.acmeIdentifierId}}</td>
                    <td>{{acmeIdentifier.type}}</td>
                    <td>{{acmeIdentifier.value}}</td>
                    <td>
                        <div v-if="acmeIdentifier.order">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: acmeIdentifier.order.id}}">{{acmeIdentifier.order.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'AcmeIdentifierView', params: {acmeIdentifierId: acmeIdentifier.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'AcmeIdentifierEdit', params: {acmeIdentifierId: acmeIdentifier.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(acmeIdentifier)"
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
            <span slot="modal-title"><span id="ca3SApp.acmeIdentifier.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-acmeIdentifier-heading" v-bind:title="$t('ca3SApp.acmeIdentifier.delete.question')">Are you sure you want to delete this Acme Identifier?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-acmeIdentifier" v-text="$t('entity.action.delete')" v-on:click="removeAcmeIdentifier()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./acme-identifier.component.ts">
</script>
