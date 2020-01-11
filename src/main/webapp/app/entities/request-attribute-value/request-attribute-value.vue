<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.requestAttributeValue.home.title')" id="request-attribute-value-heading">Request Attribute Values</span>
            <router-link :to="{name: 'RequestAttributeValueCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-request-attribute-value">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.requestAttributeValue.home.createLabel')">
                    Create a new Request Attribute Value
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
        <div class="alert alert-warning" v-if="!isFetching && requestAttributeValues && requestAttributeValues.length === 0">
            <span v-text="$t('ca3SApp.requestAttributeValue.home.notFound')">No requestAttributeValues found</span>
        </div>
        <div class="table-responsive" v-if="requestAttributeValues && requestAttributeValues.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.requestAttributeValue.attributeValue')">Attribute Value</span></th>
                    <th><span v-text="$t('ca3SApp.requestAttributeValue.reqAttr')">Req Attr</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="requestAttributeValue in requestAttributeValues"
                    :key="requestAttributeValue.id">
                    <td>
                        <router-link :to="{name: 'RequestAttributeValueView', params: {requestAttributeValueId: requestAttributeValue.id}}">{{requestAttributeValue.id}}</router-link>
                    </td>
                    <td>{{requestAttributeValue.attributeValue}}</td>
                    <td>
                        <div v-if="requestAttributeValue.reqAttr">
                            <router-link :to="{name: 'RequestAttributeView', params: {requestAttributeId: requestAttributeValue.reqAttr.id}}">{{requestAttributeValue.reqAttr.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'RequestAttributeValueView', params: {requestAttributeValueId: requestAttributeValue.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'RequestAttributeValueEdit', params: {requestAttributeValueId: requestAttributeValue.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(requestAttributeValue)"
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
            <span slot="modal-title"><span id="ca3SApp.requestAttributeValue.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-requestAttributeValue-heading" v-bind:title="$t('ca3SApp.requestAttributeValue.delete.question')">Are you sure you want to delete this Request Attribute Value?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-requestAttributeValue" v-text="$t('entity.action.delete')" v-on:click="removeRequestAttributeValue()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./request-attribute-value.component.ts">
</script>
