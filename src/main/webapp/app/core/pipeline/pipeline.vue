<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.pipeline.home.title')" id="pipeline-heading">Pipelines</span>
            <router-link :to="{name: 'PipelineCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-pipeline">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.pipeline.home.createLabel')">
                    Create a new Pipeline
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
        <div class="alert alert-warning" v-if="!isFetching && pipelines && pipelines.length === 0">
            <span v-text="$t('ca3SApp.pipeline.home.notFound')">No pipelines found</span>
        </div>
        <div class="table-responsive" v-if="pipelines && pipelines.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.pipeline.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.pipeline.type')">Type</span></th>
                    <th><span v-text="$t('ca3SApp.pipeline.urlPart')">Url Part</span></th>
                    <th><span v-text="$t('ca3SApp.pipeline.description')">Description</span></th>
                    <th><span v-text="$t('ca3SApp.pipeline.approvalRequired')">Approval Required</span></th>
                    <th><span v-text="$t('ca3SApp.pipeline.caConnector')">Ca Connector</span></th>
                    <th><span v-text="$t('ca3SApp.pipeline.processInfo')">Process Info</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="pipeline in pipelines"
                    :key="pipeline.id">
                    <td>
                        <router-link :to="{name: 'PipelineView', params: {pipelineId: pipeline.id}}">{{pipeline.id}}</router-link>
                    </td>
                    <td>{{pipeline.name}}</td>
                    <td v-text="$t('ca3SApp.PipelineType.' + pipeline.type)">{{pipeline.type}}</td>
                    <td>{{pipeline.urlPart}}</td>
                    <td>{{pipeline.description}}</td>
                    <td>{{pipeline.approvalRequired}}</td>
                    <td>
                        <div v-if="pipeline.caConnector">
                            <router-link :to="{name: 'CAConnectorConfigView', params: {cAConnectorConfigId: pipeline.caConnector.id}}">{{pipeline.caConnector.name}}</router-link>
                        </div>
                    </td>
                    <td>
                        <div v-if="pipeline.processInfo">
                            <router-link :to="{name: 'BPNMProcessInfoView', params: {bPNMProcessInfoId: pipeline.processInfo.id}}">{{pipeline.processInfo.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'ConfPipelineView', params: {pipelineId: pipeline.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'ConfPipelineEdit', params: {pipelineId: pipeline.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(pipeline)"
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
            <span slot="modal-title"><span id="ca3SApp.pipeline.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-pipeline-heading" v-text="$t('ca3SApp.pipeline.delete.question', {'id': removeId})">Are you sure you want to delete this Pipeline?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-pipeline" v-text="$t('entity.action.delete')" v-on:click="removePipeline()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./pipeline.component.ts">
</script>
