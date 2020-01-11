<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.pipelineAttribute.home.title')" id="pipeline-attribute-heading">Pipeline Attributes</span>
            <router-link :to="{name: 'PipelineAttributeCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-pipeline-attribute">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.pipelineAttribute.home.createLabel')">
                    Create a new Pipeline Attribute
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
        <div class="alert alert-warning" v-if="!isFetching && pipelineAttributes && pipelineAttributes.length === 0">
            <span v-text="$t('ca3SApp.pipelineAttribute.home.notFound')">No pipelineAttributes found</span>
        </div>
        <div class="table-responsive" v-if="pipelineAttributes && pipelineAttributes.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.pipelineAttribute.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.pipelineAttribute.value')">Value</span></th>
                    <th><span v-text="$t('ca3SApp.pipelineAttribute.pipeline')">Pipeline</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="pipelineAttribute in pipelineAttributes"
                    :key="pipelineAttribute.id">
                    <td>
                        <router-link :to="{name: 'PipelineAttributeView', params: {pipelineAttributeId: pipelineAttribute.id}}">{{pipelineAttribute.id}}</router-link>
                    </td>
                    <td>{{pipelineAttribute.name}}</td>
                    <td>{{pipelineAttribute.value}}</td>
                    <td>
                        <div v-if="pipelineAttribute.pipeline">
                            <router-link :to="{name: 'PipelineView', params: {pipelineId: pipelineAttribute.pipeline.id}}">{{pipelineAttribute.pipeline.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'PipelineAttributeView', params: {pipelineAttributeId: pipelineAttribute.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'PipelineAttributeEdit', params: {pipelineAttributeId: pipelineAttribute.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(pipelineAttribute)"
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
            <span slot="modal-title"><span id="ca3SApp.pipelineAttribute.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-pipelineAttribute-heading" v-bind:title="$t('ca3SApp.pipelineAttribute.delete.question')">Are you sure you want to delete this Pipeline Attribute?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-pipelineAttribute" v-text="$t('entity.action.delete')" v-on:click="removePipelineAttribute()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./pipeline-attribute.component.ts">
</script>
