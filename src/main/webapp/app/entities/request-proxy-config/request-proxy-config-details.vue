<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="requestProxyConfig">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.requestProxyConfig.detail.title')">RequestProxyConfig</span> {{requestProxyConfig.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.requestProxyConfig.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{requestProxyConfig.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.requestProxyConfig.requestProxyUrl')">Request Proxy Url</span>
                    </dt>
                    <dd>
                        <span>{{requestProxyConfig.requestProxyUrl}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.requestProxyConfig.active')">Active</span>
                    </dt>
                    <dd>
                        <span>{{requestProxyConfig.active}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.requestProxyConfig.secret')">Secret</span>
                    </dt>
                    <dd>
                        <div v-if="requestProxyConfig.secret">
                            <router-link :to="{name: 'ProtectedContentView', params: {protectedContentId: requestProxyConfig.secret.id}}">{{requestProxyConfig.secret.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="requestProxyConfig.id" :to="{name: 'RequestProxyConfigEdit', params: {requestProxyConfigId: requestProxyConfig.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./request-proxy-config-details.component.ts">
</script>
