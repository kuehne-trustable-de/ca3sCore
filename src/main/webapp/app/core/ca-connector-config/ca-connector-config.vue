<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.cAConnectorConfig.home.title')" id="ca-connector-config-heading">CA Connector Configs</span>
            <router-link :to="{name: 'ConfCaConnectorCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-ca-connector-config">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.cAConnectorConfig.home.createLabel')">
                    Create a new CA Connector Config
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
        <div class="alert alert-warning" v-if="!isFetching && cAConnectorConfigs && cAConnectorConfigs.length === 0">
            <span v-text="$t('ca3SApp.cAConnectorConfig.home.notFound')">No cAConnectorConfigs found</span>
        </div>
        <div class="table-responsive" v-if="cAConnectorConfigs && cAConnectorConfigs.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <!--th><span v-text="$t('global.field.id')">ID</span></th-->
                    <th><span v-text="$t('ca3SApp.cAConnectorConfig.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.cAConnectorConfig.caConnectorType')">Ca Connector Type</span></th>
                    <th><span v-text="$t('ca3SApp.cAConnectorConfig.caUrl')">Ca Url</span></th>
                    <th><span v-text="$t('ca3SApp.cAConnectorConfig.pollingOffset')">Polling Offset</span></th>
                    <th><span v-text="$t('ca3SApp.cAConnectorConfig.defaultCA')">Default CA</span></th>
                    <th><span v-text="$t('ca3SApp.cAConnectorConfig.active')">Active</span></th>
                    <th><span v-text="$t('ca3SApp.cAConnectorConfig.selector')">Selector</span></th>
                    <th><span v-text="$t('ca3SApp.cAConnectorConfig.interval')">Interval</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="cAConnectorConfig in cAConnectorConfigs"
                    :key="cAConnectorConfig.id">
                    <!--td>
                        <router-link :to="{name: 'CAConnectorConfigView', params: {cAConnectorConfigId: cAConnectorConfig.id}}">{{cAConnectorConfig.id}}</router-link>
                    </td-->
                    <td>{{cAConnectorConfig.name}}</td>
                    <td v-text="$t('ca3SApp.CAConnectorType.' + cAConnectorConfig.caConnectorType)">{{cAConnectorConfig.caConnectorType}}</td>
                    <td>{{cAConnectorConfig.caUrl}}</td>
                    <td>{{cAConnectorConfig.pollingOffset}}</td>
                    <td>{{cAConnectorConfig.defaultCA}}</td>
                    <td>{{cAConnectorConfig.active}}</td>
                    <td>{{cAConnectorConfig.selector}}</td>
                    <td v-text="$t('ca3SApp.Interval.' + cAConnectorConfig.interval)">{{cAConnectorConfig.interval}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <!--router-link :to="{name: 'ConfCaConnectorView', params: {cAConnectorConfigId: cAConnectorConfig.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link-->
                            <router-link :to="{name: 'ConfCaConnectorEdit', params: {cAConnectorConfigId: cAConnectorConfig.id, mode: 'edit'}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <router-link :to="{name: 'ConfCaConnectorCopy', params: {cAConnectorConfigId: cAConnectorConfig.id, mode: 'copy'}}"  tag="button" class="btn btn-secondary btn-sm copy">
                                <font-awesome-icon icon="clone"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.copy')">Copy</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(cAConnectorConfig)"
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
            <span slot="modal-title"><span id="ca3SApp.cAConnectorConfig.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-cAConnectorConfig-heading" v-text="$t('ca3SApp.cAConnectorConfig.delete.question', {'id': removeId})">Are you sure you want to delete this CA Connector Config?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-cAConnectorConfig" v-text="$t('entity.action.delete')" v-on:click="removeCAConnectorConfig()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./ca-connector-config.component.ts">
</script>
