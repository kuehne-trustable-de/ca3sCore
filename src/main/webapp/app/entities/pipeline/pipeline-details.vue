<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="pipeline">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.pipeline.detail.title')">Pipeline</span> {{pipeline.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.pipeline.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{pipeline.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.pipeline.type')">Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.PipelineType.' + pipeline.type)">{{pipeline.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.pipeline.urlPart')">Url Part</span>
                    </dt>
                    <dd>
                        <span>{{pipeline.urlPart}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.pipeline.description')">Description</span>
                    </dt>
                    <dd>
                        <span>{{pipeline.description}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.pipeline.approvalRequired')">Approval Required</span>
                    </dt>
                    <dd>
                        <span>{{pipeline.approvalRequired}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.pipeline.caConnector')">Ca Connector</span>
                    </dt>
                    <dd>
                        <div v-if="pipeline.caConnector">
                            <router-link :to="{name: 'CAConnectorConfigView', params: {cAConnectorConfigId: pipeline.caConnector.id}}">{{pipeline.caConnector.id}}</router-link>
                        </div>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.pipeline.processInfo')">Process Info</span>
                    </dt>
                    <dd>
                        <div v-if="pipeline.processInfo">
                            <router-link :to="{name: 'BPNMProcessInfoView', params: {bPNMProcessInfoId: pipeline.processInfo.id}}">{{pipeline.processInfo.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="pipeline.id" :to="{name: 'PipelineEdit', params: {pipelineId: pipeline.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./pipeline-details.component.ts">
</script>
