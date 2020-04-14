<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="cSR">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.cSR.detail.title')">CSR</span> {{cSR.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.status')">Status</span>
                    </dt>
                    <dd>
                        <!--span v-text="$t('ca3SApp.CsrStatus.' + cSR.status)">{{cSR.status}}</span-->
                        <span>{{cSR.status}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.isCSRValid')">Is CSR Valid</span>
                    </dt>
                    <dd>
                        <span>{{cSR.isCSRValid}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.serversideKeyGeneration')">Serverside Key Generation</span>
                    </dt>
                    <dd>
                        <span>{{cSR.serversideKeyGeneration}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.subject')">Subject</span>
                    </dt>
                    <dd>
                        <span>{{cSR.subject}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedBy')">Subject</span>
                    </dt>
                    <dd>
                        <span>{{cSR.requestedBy}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedOn')">Requested On</span>
                    </dt>
                    <dd>
                        <span v-if="cSR.requestedOn">{{$d(Date.parse(cSR.requestedOn), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.rejectedOn')">Rejected On</span>
                    </dt>
                    <dd v-if="cSR.status === 'REJECTED'">
                        <span v-if="cSR.rejectedOn">{{$d(Date.parse(cSR.rejectedOn), 'long') }}</span>
                    </dd>
                    <dt v-if="cSR.status === 'REJECTED'">
                        <span v-text="$t('ca3SApp.cSR.rejectionReason')">Rejection Reason</span>
                    </dt>
                    <dd v-if="cSR.status === 'REJECTED'">
                        <span>{{cSR.rejectionReason}}</span>
                    </dd>
                    <dt v-if="cSR.status === 'REJECTED'">
                        <span v-text="$t('ca3SApp.cSR.x509KeySpec')">X 509 Key Spec</span>
                    </dt>
                    <dd>
                        <span>{{cSR.x509KeySpec}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.signingAlgorithm')">Signing Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{cSR.signingAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.publicKeyAlgorithm')">Public Key Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{cSR.publicKeyAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.keyLength')">Key Length</span>
                    </dt>
                    <dd>
                        <span>{{cSR.keyLength}}</span>
                    </dd>
                    <!--dt>
                        <span v-text="$t('ca3SApp.cSR.publicKeyHash')">Public Key Hash</span>
                    </dt>
                    <dd>
                        <span>{{cSR.publicKeyHash}}</span>
                    </dd-->
                    <!--dt>
                        <span v-text="$t('ca3SApp.cSR.subjectPublicKeyInfoBase64')">Subject Public Key Info Base 64</span>
                    </dt>
                    <dd>
                        <span>{{cSR.subjectPublicKeyInfoBase64}}</span>
                    </dd-->
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.pipeline')">Pipeline</span>
                    </dt>
                    <dd>
                        <div v-if="cSR.pipeline">
                            <router-link :to="{name: 'PipelineView', params: {pipelineId: cSR.pipeline.id}}">{{cSR.pipeline.name}}</router-link>
                        </div>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.pipelineType')">Pipeline Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.PipelineType.' + cSR.pipelineType)">{{cSR.pipelineType}}</span>
                    </dd>
                    <!--dt>
                        <span v-text="$t('ca3SApp.cSR.processInstanceId')">Process Instance Id</span>
                    </dt>
                    <dd>
                        <span>{{cSR.processInstanceId}}</span>
                    </dd-->
                    
                    <dt v-if="requestorComment.length > 0">
                        <span v-text="$t('ca3SApp.cSR.requestorComment')">Requestor comment</span>
                    </dt>
                    <dd v-if="requestorComment.length > 0">
                        <span>{{requestorComment}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.csrBase64')">Csr Base 64</span>
                    </dt>
                    <dd>
                        <span><textarea class="form-control" name="content" id="comment"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly
                            v-model="cSR.csrBase64" /></span>

                    </dd>
                </dl>

            </div>

<!--
    csrAdminData
-->
            <form name="editForm" role="form" novalidate>
                <div>
                    <div v-if="cSR.status === 'PENDING' && ((roles === 'ROLE_RA') || (getUsername() === cSR.requestedBy))" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.rejectionReason')" for="rejectionReason">rejection reason</label>
                        <input type="text" class="form-control" name="rejectionReason" id="csr-rejectionReason" v-model="csrAdminData.rejectionReason" />
                    </div>

                    <div v-if="cSR.status === 'PENDING' && ((roles === 'ROLE_RA') || (getUsername() === cSR.requestedBy))" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.comment')" for="comment">Comment</label>
                        <textarea class="form-control" name="content" id="comment"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="csrAdminData.comment" />
                    </div>


                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                    </button>

                    <button type="button" id="reject" v-if="cSR.status === 'PENDING' && roles === 'ROLE_RA'" class="btn btn-secondary" v-on:click="rejectCSR()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.reject')">Reject</span>
                    </button>

                    <button type="button" id="withdraw" v-if="cSR.status === 'PENDING' && getUsername() === cSR.requestedBy" class="btn btn-secondary" v-on:click="withdrawCSR()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.withdraw')">Withdraw</span>
                    </button>


                    <button type="button" id="confirm" v-if="cSR.status === 'PENDING' && roles === 'ROLE_RA'" class="btn btn-primary"
                        v-on:click="confirmCSR()">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.confirm')">Confirm</span>
                    </button>
                </div>
            </form>
            
        </div>
    </div>
</template>

<script lang="ts" src="./csr-info.component.ts">
</script>
