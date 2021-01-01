<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="pipelineAttribute">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.pipelineAttribute.detail.title')">PipelineAttribute</span> {{pipelineAttribute.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.pipelineAttribute.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{pipelineAttribute.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.pipelineAttribute.value')">Value</span>
                    </dt>
                    <dd>
                        <span>{{pipelineAttribute.value}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.pipelineAttribute.pipeline')">Pipeline</span>
                    </dt>
                    <dd>
                        <div v-if="pipelineAttribute.pipeline">
                            <router-link :to="{name: 'PipelineView', params: {pipelineId: pipelineAttribute.pipeline.id}}">{{pipelineAttribute.pipeline.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="pipelineAttribute.id" :to="{name: 'PipelineAttributeEdit', params: {pipelineAttributeId: pipelineAttribute.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./pipeline-attribute-details.component.ts">
</script>
