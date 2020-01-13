<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="cSR">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.cSR.detail.title')">CSR</span> {{cSR.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.csrBase64')">Csr Base 64</span>
                    </dt>
                    <dd>
                        <span>{{cSR.csrBase64}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedOn')">Requested On</span>
                    </dt>
                    <dd>
                        <span>{{cSR.requestedOn}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.status')">Status</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.CsrStatus.' + cSR.status)">{{cSR.status}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.processInstanceId')">Process Instance Id</span>
                    </dt>
                    <dd>
                        <span>{{cSR.processInstanceId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.signingAlgorithm')">Signing Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{cSR.signingAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.isCSRValid')">Is CSR Valid</span>
                    </dt>
                    <dd>
                        <span>{{cSR.isCSRValid}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.x509KeySpec')">X 509 Key Spec</span>
                    </dt>
                    <dd>
                        <span>{{cSR.x509KeySpec}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.publicKeyAlgorithm')">Public Key Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{cSR.publicKeyAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.publicKeyHash')">Public Key Hash</span>
                    </dt>
                    <dd>
                        <span>{{cSR.publicKeyHash}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.subjectPublicKeyInfoBase64')">Subject Public Key Info Base 64</span>
                    </dt>
                    <dd>
                        <span>{{cSR.subjectPublicKeyInfoBase64}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.pipeline')">Pipeline</span>
                    </dt>
                    <dd>
                        <div v-if="cSR.pipeline">
                            <router-link :to="{name: 'PipelineView', params: {pipelineId: cSR.pipeline.id}}">{{cSR.pipeline.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                
                <router-link v-if="cSR.id" :to="{name: 'CSREdit', params: {cSRId: cSR.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./csr-details.component.ts">
</script>
