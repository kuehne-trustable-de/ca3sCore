<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.identifier.home.title')" id="identifier-heading">Identifiers</span>
            <router-link :to="{name: 'IdentifierCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-identifier">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.identifier.home.createLabel')">
                    Create a new Identifier
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
        <div class="alert alert-warning" v-if="!isFetching && identifiers && identifiers.length === 0">
            <span v-text="$t('ca3SApp.identifier.home.notFound')">No identifiers found</span>
        </div>
        <div class="table-responsive" v-if="identifiers && identifiers.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.identifier.identifierId')">Identifier Id</span></th>
                    <th><span v-text="$t('ca3SApp.identifier.type')">Type</span></th>
                    <th><span v-text="$t('ca3SApp.identifier.value')">Value</span></th>
                    <th><span v-text="$t('ca3SApp.identifier.order')">Order</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="identifier in identifiers"
                    :key="identifier.id">
                    <td>
                        <router-link :to="{name: 'IdentifierView', params: {identifierId: identifier.id}}">{{identifier.id}}</router-link>
                    </td>
                    <td>{{identifier.identifierId}}</td>
                    <td>{{identifier.type}}</td>
                    <td>{{identifier.value}}</td>
                    <td>
                        <div v-if="identifier.order">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: identifier.order.id}}">{{identifier.order.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'IdentifierView', params: {identifierId: identifier.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'IdentifierEdit', params: {identifierId: identifier.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(identifier)"
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
            <span slot="modal-title"><span id="ca3SApp.identifier.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-identifier-heading" v-text="$t('ca3SApp.identifier.delete.question', {'id': removeId})">Are you sure you want to delete this Identifier?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-identifier" v-text="$t('entity.action.delete')" v-on:click="removeIdentifier()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./identifier.component.ts">
</script>
