<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.requestProxyConfig.home.title')" id="request-proxy-config-heading">Request Proxy Configs</span>
            <router-link :to="{name: 'RequestProxyConfigCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-request-proxy-config">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.requestProxyConfig.home.createLabel')">
                    Create a new Request Proxy Config
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
        <div class="alert alert-warning" v-if="!isFetching && requestProxyConfigs && requestProxyConfigs.length === 0">
            <span v-text="$t('ca3SApp.requestProxyConfig.home.notFound')">No requestProxyConfigs found</span>
        </div>
        <div class="table-responsive" v-if="requestProxyConfigs && requestProxyConfigs.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.requestProxyConfig.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.requestProxyConfig.requestProxyUrl')">Request Proxy Url</span></th>
                    <th><span v-text="$t('ca3SApp.requestProxyConfig.active')">Active</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="requestProxyConfig in requestProxyConfigs"
                    :key="requestProxyConfig.id">
                    <td>{{requestProxyConfig.id}}</td>
                    <td>{{requestProxyConfig.name}}</td>
                    <td>{{requestProxyConfig.requestProxyUrl}}</td>
                    <td>{{requestProxyConfig.active}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'RequestProxyConfigEdit', params: {requestProxyConfigId: requestProxyConfig.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(requestProxyConfig)"
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
            <span slot="modal-title"><span id="ca3SApp.requestProxyConfig.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-requestProxyConfig-heading" v-bind:title="$t('ca3SApp.requestProxyConfig.delete.question')">Are you sure you want to delete this Request Proxy Config?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-requestProxyConfig" v-text="$t('entity.action.delete')" v-on:click="removeRequestProxyConfig()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./request-proxy-config.component.ts">
</script>
