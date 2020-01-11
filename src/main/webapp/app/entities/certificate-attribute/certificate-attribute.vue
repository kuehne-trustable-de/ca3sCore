<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.certificateAttribute.home.title')" id="certificate-attribute-heading">Certificate Attributes</span>
            <router-link :to="{name: 'CertificateAttributeCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-certificate-attribute">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.certificateAttribute.home.createLabel')">
                    Create a new Certificate Attribute
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
        <div class="alert alert-warning" v-if="!isFetching && certificateAttributes && certificateAttributes.length === 0">
            <span v-text="$t('ca3SApp.certificateAttribute.home.notFound')">No certificateAttributes found</span>
        </div>
        <div class="table-responsive" v-if="certificateAttributes && certificateAttributes.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.certificateAttribute.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.certificateAttribute.value')">Value</span></th>
                    <th><span v-text="$t('ca3SApp.certificateAttribute.certificate')">Certificate</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="certificateAttribute in certificateAttributes"
                    :key="certificateAttribute.id">
                    <td>
                        <router-link :to="{name: 'CertificateAttributeView', params: {certificateAttributeId: certificateAttribute.id}}">{{certificateAttribute.id}}</router-link>
                    </td>
                    <td>{{certificateAttribute.name}}</td>
                    <td>{{certificateAttribute.value}}</td>
                    <td>
                        <div v-if="certificateAttribute.certificate">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: certificateAttribute.certificate.id}}">{{certificateAttribute.certificate.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'CertificateAttributeView', params: {certificateAttributeId: certificateAttribute.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'CertificateAttributeEdit', params: {certificateAttributeId: certificateAttribute.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(certificateAttribute)"
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
            <span slot="modal-title"><span id="ca3SApp.certificateAttribute.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-certificateAttribute-heading" v-bind:title="$t('ca3SApp.certificateAttribute.delete.question')">Are you sure you want to delete this Certificate Attribute?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-certificateAttribute" v-text="$t('entity.action.delete')" v-on:click="removeCertificateAttribute()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./certificate-attribute.component.ts">
</script>
