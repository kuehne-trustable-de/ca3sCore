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
                        <span v-text="$t('ca3SApp.cSR.subject')">Subject</span>
                    </dt>
                    <dd>
                        <span>{{cSR.subject}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.sans')">Sans</span>
                    </dt>
                    <dd>
                        <span>{{cSR.sans}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedOn')">Requested On</span>
                    </dt>
                    <dd>
                        <span v-if="cSR.requestedOn">{{$d(Date.parse(cSR.requestedOn), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedBy')">Requested By</span>
                    </dt>
                    <dd>
                        <span>{{cSR.requestedBy}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.pipelineType')">Pipeline Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.PipelineType.' + cSR.pipelineType)">{{cSR.pipelineType}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.status')">Status</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.CsrStatus.' + cSR.status)">{{cSR.status}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.administeredBy')">Administered By</span>
                    </dt>
                    <dd>
                        <span>{{cSR.administeredBy}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.approvedOn')">Approved On</span>
                    </dt>
                    <dd>
                        <span v-if="cSR.approvedOn">{{$d(Date.parse(cSR.approvedOn), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.rejectedOn')">Rejected On</span>
                    </dt>
                    <dd>
                        <span v-if="cSR.rejectedOn">{{$d(Date.parse(cSR.rejectedOn), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.rejectionReason')">Rejection Reason</span>
                    </dt>
                    <dd>
                        <span>{{cSR.rejectionReason}}</span>
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
                        <span v-text="$t('ca3SApp.cSR.keyAlgorithm')">Key Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{cSR.keyAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.keyLength')">Key Length</span>
                    </dt>
                    <dd>
                        <span>{{cSR.keyLength}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.publicKeyHash')">Public Key Hash</span>
                    </dt>
                    <dd>
                        <span>{{cSR.publicKeyHash}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.serversideKeyGeneration')">Serverside Key Generation</span>
                    </dt>
                    <dd>
                        <span>{{cSR.serversideKeyGeneration}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.subjectPublicKeyInfoBase64')">Subject Public Key Info Base 64</span>
                    </dt>
                    <dd>
                        <span>{{cSR.subjectPublicKeyInfoBase64}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestorComment')">Requestor Comment</span>
                    </dt>
                    <dd>
                        <span>{{cSR.requestorComment}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.administrationComment')">Administration Comment</span>
                    </dt>
                    <dd>
                        <span>{{cSR.administrationComment}}</span>
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
