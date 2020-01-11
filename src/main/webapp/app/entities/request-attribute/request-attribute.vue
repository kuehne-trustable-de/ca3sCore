<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.requestAttribute.home.title')" id="request-attribute-heading">Request Attributes</span>
            <router-link :to="{name: 'RequestAttributeCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-request-attribute">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.requestAttribute.home.createLabel')">
                    Create a new Request Attribute
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
        <div class="alert alert-warning" v-if="!isFetching && requestAttributes && requestAttributes.length === 0">
            <span v-text="$t('ca3SApp.requestAttribute.home.notFound')">No requestAttributes found</span>
        </div>
        <div class="table-responsive" v-if="requestAttributes && requestAttributes.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.requestAttribute.attributeType')">Attribute Type</span></th>
                    <th><span v-text="$t('ca3SApp.requestAttribute.holdingRequestAttribute')">Holding Request Attribute</span></th>
                    <th><span v-text="$t('ca3SApp.requestAttribute.csr')">Csr</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="requestAttribute in requestAttributes"
                    :key="requestAttribute.id">
                    <td>
                        <router-link :to="{name: 'RequestAttributeView', params: {requestAttributeId: requestAttribute.id}}">{{requestAttribute.id}}</router-link>
                    </td>
                    <td>{{requestAttribute.attributeType}}</td>
                    <td>
                        <div v-if="requestAttribute.holdingRequestAttribute">
                            <router-link :to="{name: 'RequestAttributeValueView', params: {requestAttributeValueId: requestAttribute.holdingRequestAttribute.id}}">{{requestAttribute.holdingRequestAttribute.id}}</router-link>
                        </div>
                    </td>
                    <td>
                        <div v-if="requestAttribute.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: requestAttribute.csr.id}}">{{requestAttribute.csr.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'RequestAttributeView', params: {requestAttributeId: requestAttribute.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'RequestAttributeEdit', params: {requestAttributeId: requestAttribute.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(requestAttribute)"
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
            <span slot="modal-title"><span id="ca3SApp.requestAttribute.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-requestAttribute-heading" v-bind:title="$t('ca3SApp.requestAttribute.delete.question')">Are you sure you want to delete this Request Attribute?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-requestAttribute" v-text="$t('entity.action.delete')" v-on:click="removeRequestAttribute()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./request-attribute.component.ts">
</script>
