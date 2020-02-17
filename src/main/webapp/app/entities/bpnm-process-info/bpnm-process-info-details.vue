<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="bPNMProcessInfo">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.detail.title')">BPNMProcessInfo</span> {{bPNMProcessInfo.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.version')">Version</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.version}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.type')">Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.BPNMProcessType.' + bPNMProcessInfo.type)">{{bPNMProcessInfo.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.author')">Author</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.author}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.lastChange')">Last Change</span>
                    </dt>
                    <dd>
                        <span v-if="bPNMProcessInfo.lastChange">{{$d(Date.parse(bPNMProcessInfo.lastChange), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.signatureBase64')">Signature Base 64</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.signatureBase64}}</span>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                
                <router-link v-if="bPNMProcessInfo.id" :to="{name: 'BPNMProcessInfoEdit', params: {bPNMProcessInfoId: bPNMProcessInfo.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./bpnm-process-info-details.component.ts">
</script>
