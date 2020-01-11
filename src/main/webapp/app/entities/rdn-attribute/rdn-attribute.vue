<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.rDNAttribute.home.title')" id="rdn-attribute-heading">RDN Attributes</span>
            <router-link :to="{name: 'RDNAttributeCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-rdn-attribute">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.rDNAttribute.home.createLabel')">
                    Create a new RDN Attribute
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
        <div class="alert alert-warning" v-if="!isFetching && rDNAttributes && rDNAttributes.length === 0">
            <span v-text="$t('ca3SApp.rDNAttribute.home.notFound')">No rDNAttributes found</span>
        </div>
        <div class="table-responsive" v-if="rDNAttributes && rDNAttributes.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.rDNAttribute.attributeType')">Attribute Type</span></th>
                    <th><span v-text="$t('ca3SApp.rDNAttribute.attributeValue')">Attribute Value</span></th>
                    <th><span v-text="$t('ca3SApp.rDNAttribute.rdn')">Rdn</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="rDNAttribute in rDNAttributes"
                    :key="rDNAttribute.id">
                    <td>
                        <router-link :to="{name: 'RDNAttributeView', params: {rDNAttributeId: rDNAttribute.id}}">{{rDNAttribute.id}}</router-link>
                    </td>
                    <td>{{rDNAttribute.attributeType}}</td>
                    <td>{{rDNAttribute.attributeValue}}</td>
                    <td>
                        <div v-if="rDNAttribute.rdn">
                            <router-link :to="{name: 'RDNView', params: {rDNId: rDNAttribute.rdn.id}}">{{rDNAttribute.rdn.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'RDNAttributeView', params: {rDNAttributeId: rDNAttribute.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'RDNAttributeEdit', params: {rDNAttributeId: rDNAttribute.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(rDNAttribute)"
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
            <span slot="modal-title"><span id="ca3SApp.rDNAttribute.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-rDNAttribute-heading" v-bind:title="$t('ca3SApp.rDNAttribute.delete.question')">Are you sure you want to delete this RDN Attribute?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-rDNAttribute" v-text="$t('entity.action.delete')" v-on:click="removeRDNAttribute()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./rdn-attribute.component.ts">
</script>
