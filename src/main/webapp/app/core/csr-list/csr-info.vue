<template>
    <div>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="icsrView">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.cSR.detail.title')">CSR</span> {{ icsrView.id }}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.status')">Status</span>
                    </dt>
                    <dd>
                        <!--span v-text="$t('ca3SApp.CsrStatus.' + cSR.status)">{{cSR.status}}</span-->
                        <span>{{ icsrView.status }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.isCSRValid')">Is CSR Valid</span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.csrvalid }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.serversideKeyGeneration')">Serverside Key Generation</span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.serversideKeyGeneration }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.subject')">Subject</span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.subject }}</span>
                    </dd>

                    <dt v-if="icsrView.sanArr && icsrView.sanArr.length > 0">
						<span v-text="$t('ca3SApp.cSR.sans')">Subject alternative names</span>
					</dt>
					<dd v-if="icsrView.sanArr && icsrView.sanArr.length > 0">
						<ul>
							<li v-for="san in icsrView.sanArr" :key="san" >{{san}}</li>
						</ul>
					</dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedBy')">Subject</span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.requestedBy }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedOn')">Requested On</span>
                    </dt>
                    <dd>
                        <span v-if="icsrView.requestedOn">{{ $d(Date.parse(icsrView.requestedOn), 'long') }}</span>
                    </dd>

                    <dt v-if="icsrView.requestorComment && icsrView.requestorComment.length > 0">
                        <span v-text="$t('ca3SApp.cSR.requestorComment')">Requestor comment</span>
                    </dt>
                    <dd v-if="icsrView.requestorComment && icsrView.requestorComment.length > 0">
                        <span>{{ icsrView.requestorComment }}</span>
                    </dd>

                    <dt v-if="icsrView.status === 'REJECTED'">
                        <span v-text="$t('ca3SApp.cSR.rejectedOn')">Rejected On</span>
                    </dt>
                    <dd v-if="icsrView.status === 'REJECTED'">
                        <span v-if="icsrView.rejectedOn">{{ $d(Date.parse(icsrView.rejectedOn), 'long') }}</span>
                    </dd>

                    <dt v-if="icsrView.status === 'REJECTED'">
                        <span v-text="$t('ca3SApp.cSR.rejectionReason')">Rejection Reason</span>
                    </dt>
                    <dd v-if="icsrView.status === 'REJECTED'">
                        <span>{{ icsrView.rejectionReason }}</span>
                    </dd>

                    <dt v-if="icsrView.administrationComment && icsrView.administrationComment.length > 0">
                        <span v-text="$t('ca3SApp.cSR.administrationComment')">Administrator comment</span>
                    </dt>
                    <dd v-if="icsrView.administrationComment && icsrView.administrationComment.length > 0">
                        <span>{{ icsrView.administrationComment }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.x509KeySpec')">X 509 Key Spec</span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.x509KeySpec }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.signingAlgorithm')">Signing Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.signingAlgorithm }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.keyDetails')">Key Details</span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.publicKeyAlgorithm }} / {{ icsrView.keyLength }} bits</span>
                    </dd>



                    <dt v-if="icsrView.certificateId">
                        <span v-text="$t('ca3SApp.cSR.certificate')">Certificate</span>
                    </dt>
                    <dd v-if="icsrView.certificateId">
                        <div>
                            <router-link :to="{name: 'CertificateView', params: {pipelineId: icsrView.certificateId}}">{{icsrView.certificateId}}</router-link>
                        </div>
                    </dd>

                    <dt v-if="icsrView.certificateId">
                        <span v-text="$t('ca3SApp.cSR.certificate')">Certificate</span>
                    </dt>
                    <dd v-if="icsrView.certificateId">
                        <div>
                            <router-link :to="{name: 'CertInfo', params: {certificateId: icsrView.certificateId}}">{{icsrView.certificateId}}</router-link>
                        </div>
                    </dd>

                    <dt v-if="icsrView.pipeline && icsrView.pipeline.name && icsrView.pipeline.name.length > 0">
                        <span v-text="$t('ca3SApp.cSR.pipeline')">Pipeline</span>
                    </dt>
                    <dd v-if="icsrView.pipeline && icsrView.pipeline.name && icsrView.pipeline.name.length > 0">
                        <div>
                            <router-link :to="{name: 'PipelineView', params: {pipelineId: icsrView.pipeline.id}}">{{ icsrView.pipeline.name }}</router-link>
                        </div>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.pipeline')">Pipeline</span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.pipelineName }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.pipelineType')">Pipeline Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.PipelineType.' + icsrView.pipelineType)">{{ icsrView.pipelineType }}</span>
                    </dd>

                    <!--dt>
                        <span v-text="$t('ca3SApp.cSR.processInstanceId')">Process Instance Id</span>
                    </dt>
                    <dd>
                        <span>{{cSR.processInstanceId}}</span>
                    </dd-->

                    <Fragment v-for="attr in arAttributes" :key="attr.name" v-if="!(isRAOfficer() || (getUsername() === icsrView.requestedBy))">
                        <dt>
                            <span >{{attr.name}}</span>
                        </dt>
                        <dd >
                            <span >{{attr.value}}</span>
                        </dd>
                    </Fragment>


                    <dt v-if="icsrView.csrBase64 && icsrView.csrBase64.trim().length > 0">
                        <span v-text="$t('ca3SApp.cSR.csrBase64')">Csr Base64</span>
                    </dt>
                    <dd v-if="icsrView.csrBase64 && icsrView.csrBase64.trim().length > 0">
                        <div class="form-group wrap">
                            <textarea class="form-control pem-content" name="csrContent" id="csrContent"
                                      autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly
                                      v-model="icsrView.csrBase64" />

                            <CopyClipboardButton contentElementId="csrContent"/>
                        </div>

                    </dd>
                </dl>
                <div>
                    <audit-tag :csrId="icsrView.id" showLinks="false" :title="$t('ca3SApp.certificate.audit')"></audit-tag>
                </div>

            </div>

<!--
    csrAdminData
-->
            <form name="editForm" role="form" novalidate>
                <div>

                    <Fragment v-if="isRAOfficer() || (getUsername() === icsrView.requestedBy)">

                        <div v-if="icsrView.status === 'PENDING'" class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.cSR.rejectionReason')" for="csr-rejectionReason">rejection reason</label>
                            <input type="text" class="form-control" name="rejectionReason" id="csr-rejectionReason" v-model="csrAdminData.rejectionReason" />
                        </div>

                        <div v-for="attr in csrAdminData.arAttributes" :key="attr.name" class="form-group">
                            <label class="form-control-label"  for="csr-ar-{attr.name}">{{attr.name}}</label>
                            <input type="text" class="form-control" name="rejectionReason" id="csr-ar-{attr.name}" v-model="attr.value" />
                        </div>

                        <div class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.certificate.comment')" for="comment">Comment</label>
                            <textarea class="form-control" name="content" id="comment"
                                autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                v-model="csrAdminData.comment" />
                        </div>
                    </Fragment>

                    <b-alert :show="dismissCountDown"
                             dismissible
                             :variant="alertType"
                             @dismissed="dismissCountDown=0"
                             @dismiss-count-down="countDownChanged">
                        {{alertMessage}}
                    </b-alert>
                    <br/>

                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                    </button>

                    <button type="button" id="update" v-if="isRAOfficer()" class="btn btn-secondary" v-on:click="updateCSR()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.update')">Update</span>
                    </button>

                    <button type="button" id="selfAdminister" v-if="icsrView.status === 'PENDING' && getUsername() === icsrView.requestedBy && !isRAOfficer()"
                            class="btn btn-secondary" v-on:click="selfAdministerRequest()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.update')">Update</span>
                    </button>

                    <button type="button" id="reject" v-if="icsrView.status === 'PENDING' && isRAOfficer() && !(getUsername() === icsrView.requestedBy)" class="btn btn-secondary" v-on:click="rejectCSR()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.reject')">Reject</span>
                    </button>

                    <button type="button" id="withdraw" v-if="icsrView.status === 'PENDING' && getUsername() === icsrView.requestedBy" class="btn btn-secondary" v-on:click="withdrawCSR()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.withdraw')">Withdraw</span>
                    </button>

                    <button type="button" id="confirm" v-if="icsrView.status === 'PENDING' && isRAOfficer()" class="btn btn-primary"
                            v-on:click="confirmCSR()">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.confirm')">Confirm</span>
                    </button>
                </div>
            </form>

        </div>
    </div>
    </div>
</template>

<script lang="ts" src="./csr-info.component.ts">
</script>
