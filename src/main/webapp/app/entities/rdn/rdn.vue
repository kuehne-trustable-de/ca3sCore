<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.rDN.home.title')" id="rdn-heading">RDNS</span>
            <router-link :to="{name: 'RDNCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-rdn">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.rDN.home.createLabel')">
                    Create a new RDN
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
        <div class="alert alert-warning" v-if="!isFetching && rDNS && rDNS.length === 0">
            <span v-text="$t('ca3SApp.rDN.home.notFound')">No rDNS found</span>
        </div>
        <div class="table-responsive" v-if="rDNS && rDNS.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.rDN.csr')">Csr</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="rDN in rDNS"
                    :key="rDN.id">
                    <td>
                        <router-link :to="{name: 'RDNView', params: {rDNId: rDN.id}}">{{rDN.id}}</router-link>
                    </td>
                    <td>
                        <div v-if="rDN.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: rDN.csr.id}}">{{rDN.csr.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'RDNView', params: {rDNId: rDN.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'RDNEdit', params: {rDNId: rDN.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(rDN)"
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
            <span slot="modal-title"><span id="ca3SApp.rDN.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-rDN-heading" v-text="$t('ca3SApp.rDN.delete.question', {'id': removeId})">Are you sure you want to delete this RDN?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-rDN" v-text="$t('entity.action.delete')" v-on:click="removeRDN()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./rdn.component.ts">
</script>
