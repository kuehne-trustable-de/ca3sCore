<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.csrAttribute.home.title')" id="csr-attribute-heading">Csr Attributes</span>
            <router-link :to="{name: 'CsrAttributeCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-csr-attribute">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.csrAttribute.home.createLabel')">
                    Create a new Csr Attribute
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
        <div class="alert alert-warning" v-if="!isFetching && csrAttributes && csrAttributes.length === 0">
            <span v-text="$t('ca3SApp.csrAttribute.home.notFound')">No csrAttributes found</span>
        </div>
        <div class="table-responsive" v-if="csrAttributes && csrAttributes.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.csrAttribute.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.csrAttribute.value')">Value</span></th>
                    <th><span v-text="$t('ca3SApp.csrAttribute.csr')">Csr</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="csrAttribute in csrAttributes"
                    :key="csrAttribute.id">
                    <td>
                        <router-link :to="{name: 'CsrAttributeView', params: {csrAttributeId: csrAttribute.id}}">{{csrAttribute.id}}</router-link>
                    </td>
                    <td>{{csrAttribute.name}}</td>
                    <td>{{csrAttribute.value}}</td>
                    <td>
                        <div v-if="csrAttribute.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: csrAttribute.csr.id}}">{{csrAttribute.csr.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'CsrAttributeView', params: {csrAttributeId: csrAttribute.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'CsrAttributeEdit', params: {csrAttributeId: csrAttribute.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(csrAttribute)"
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
            <span slot="modal-title"><span id="ca3SApp.csrAttribute.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-csrAttribute-heading" v-text="$t('ca3SApp.csrAttribute.delete.question', {'id': removeId})">Are you sure you want to delete this Csr Attribute?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-csrAttribute" v-text="$t('entity.action.delete')" v-on:click="removeCsrAttribute()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./csr-attribute.component.ts">
</script>
