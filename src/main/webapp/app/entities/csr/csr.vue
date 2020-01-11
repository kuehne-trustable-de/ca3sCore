<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.cSR.home.title')" id="csr-heading">CSRS</span>
            <router-link :to="{name: 'CSRCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-csr">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.cSR.home.createLabel')">
                    Create a new CSR
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
        <div class="alert alert-warning" v-if="!isFetching && cSRS && cSRS.length === 0">
            <span v-text="$t('ca3SApp.cSR.home.notFound')">No cSRS found</span>
        </div>
        <div class="table-responsive" v-if="cSRS && cSRS.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.csrBase64')">Csr Base 64</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.requestedOn')">Requested On</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.status')">Status</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.processInstanceId')">Process Instance Id</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.signingAlgorithm')">Signing Algorithm</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.isCSRValid')">Is CSR Valid</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.x509KeySpec')">X 509 Key Spec</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.publicKeyAlgorithm')">Public Key Algorithm</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.publicKeyHash')">Public Key Hash</span></th>
                    <th><span v-text="$t('ca3SApp.cSR.subjectPublicKeyInfoBase64')">Subject Public Key Info Base 64</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="cSR in cSRS"
                    :key="cSR.id">
                    <td>
                        <router-link :to="{name: 'CSRView', params: {cSRId: cSR.id}}">{{cSR.id}}</router-link>
                    </td>
                    <td>{{cSR.csrBase64}}</td>
                    <td>{{cSR.requestedOn}}</td>
                    <td v-text="$t('ca3SApp.CsrStatus.' + cSR.status)">{{cSR.status}}</td>
                    <td>{{cSR.processInstanceId}}</td>
                    <td>{{cSR.signingAlgorithm}}</td>
                    <td>{{cSR.isCSRValid}}</td>
                    <td>{{cSR.x509KeySpec}}</td>
                    <td>{{cSR.publicKeyAlgorithm}}</td>
                    <td>{{cSR.publicKeyHash}}</td>
                    <td>{{cSR.subjectPublicKeyInfoBase64}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'CSRView', params: {cSRId: cSR.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'CSREdit', params: {cSRId: cSR.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(cSR)"
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
            <span slot="modal-title"><span id="ca3SApp.cSR.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-cSR-heading" v-bind:title="$t('ca3SApp.cSR.delete.question')">Are you sure you want to delete this CSR?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-cSR" v-text="$t('entity.action.delete')" v-on:click="removeCSR()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./csr.component.ts">
</script>
