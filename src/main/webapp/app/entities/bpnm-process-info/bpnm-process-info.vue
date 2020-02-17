<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.bPNMProcessInfo.home.title')" id="bpnm-process-info-heading">BPNM Process Infos</span>
            <router-link :to="{name: 'BPNMProcessInfoCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-bpnm-process-info">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.bPNMProcessInfo.home.createLabel')">
                    Create a new BPNM Process Info
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
        <div class="alert alert-warning" v-if="!isFetching && bPNMProcessInfos && bPNMProcessInfos.length === 0">
            <span v-text="$t('ca3SApp.bPNMProcessInfo.home.notFound')">No bPNMProcessInfos found</span>
        </div>
        <div class="table-responsive" v-if="bPNMProcessInfos && bPNMProcessInfos.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.bPNMProcessInfo.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.bPNMProcessInfo.version')">Version</span></th>
                    <th><span v-text="$t('ca3SApp.bPNMProcessInfo.type')">Type</span></th>
                    <th><span v-text="$t('ca3SApp.bPNMProcessInfo.author')">Author</span></th>
                    <th><span v-text="$t('ca3SApp.bPNMProcessInfo.lastChange')">Last Change</span></th>
                    <th><span v-text="$t('ca3SApp.bPNMProcessInfo.signatureBase64')">Signature Base 64</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="bPNMProcessInfo in bPNMProcessInfos"
                    :key="bPNMProcessInfo.id">
                    <td>
                        <router-link :to="{name: 'BPNMProcessInfoView', params: {bPNMProcessInfoId: bPNMProcessInfo.id}}">{{bPNMProcessInfo.id}}</router-link>
                    </td>
                    <td>{{bPNMProcessInfo.name}}</td>
                    <td>{{bPNMProcessInfo.version}}</td>
                    <td v-text="$t('ca3SApp.BPNMProcessType.' + bPNMProcessInfo.type)">{{bPNMProcessInfo.type}}</td>
                    <td>{{bPNMProcessInfo.author}}</td>
                    <td v-if="bPNMProcessInfo.lastChange"> {{$d(Date.parse(bPNMProcessInfo.lastChange), 'short') }}</td>
                    <td>{{bPNMProcessInfo.signatureBase64}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'BPNMProcessInfoView', params: {bPNMProcessInfoId: bPNMProcessInfo.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'BPNMProcessInfoEdit', params: {bPNMProcessInfoId: bPNMProcessInfo.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(bPNMProcessInfo)"
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
            <span slot="modal-title"><span id="ca3SApp.bPNMProcessInfo.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-bPNMProcessInfo-heading" v-bind:title="$t('ca3SApp.bPNMProcessInfo.delete.question')">Are you sure you want to delete this BPNM Process Info?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-bPNMProcessInfo" v-text="$t('entity.action.delete')" v-on:click="removeBPNMProcessInfo()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./bpnm-process-info.component.ts">
</script>
