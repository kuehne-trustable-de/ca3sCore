<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="cAConnectorConfig">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.cAConnectorConfig.detail.title')">CAConnectorConfig</span> {{cAConnectorConfig.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.cAConnectorConfig.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{cAConnectorConfig.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cAConnectorConfig.caConnectorType')">Ca Connector Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.CAConnectorType.' + cAConnectorConfig.caConnectorType)">{{cAConnectorConfig.caConnectorType}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cAConnectorConfig.caUrl')">Ca Url</span>
                    </dt>
                    <dd>
                        <span>{{cAConnectorConfig.caUrl}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cAConnectorConfig.secret')">Secret</span>
                    </dt>
                    <dd>
                        <span>{{cAConnectorConfig.secret}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cAConnectorConfig.pollingOffset')">Polling Offset</span>
                    </dt>
                    <dd>
                        <span>{{cAConnectorConfig.pollingOffset}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cAConnectorConfig.defaultCA')">Default CA</span>
                    </dt>
                    <dd>
                        <span>{{cAConnectorConfig.defaultCA}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cAConnectorConfig.active')">Active</span>
                    </dt>
                    <dd>
                        <span>{{cAConnectorConfig.active}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cAConnectorConfig.selector')">Selector</span>
                    </dt>
                    <dd>
                        <span>{{cAConnectorConfig.selector}}</span>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                
                <router-link v-if="cAConnectorConfig.id" :to="{name: 'CAConnectorConfigEdit', params: {cAConnectorConfigId: cAConnectorConfig.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./ca-connector-config-details.component.ts">
</script>
