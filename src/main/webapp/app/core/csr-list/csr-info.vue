<template>
    <div>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="icsrView">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.cSR.detail.title')"></span> {{ icsrView.id }}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.status')"></span>
                    </dt>
                    <dd>
                        <!--span v-text="$t('ca3SApp.CsrStatus.' + cSR.status)">{{cSR.status}}</span-->
                        <span>{{ icsrView.status }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.isCSRValid')"></span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.csrvalid }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.serversideKeyGeneration')"></span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.serversideKeyGeneration }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.subject')"></span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.subject }}</span>
                    </dd>

                    <dt v-if="icsrView.sanArr && icsrView.sanArr.length > 0">
						<span v-text="$t('ca3SApp.cSR.sans')"></span>
					</dt>
					<dd v-if="icsrView.sanArr && icsrView.sanArr.length > 0">
						<ul>
							<li v-for="san in icsrView.sanArr" :key="san" >{{san}}</li>
						</ul>
					</dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedBy')"></span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.requestedBy }}</span>

                        <span v-if="(icsrView.firstName || icsrView.lastName) && icsrView.email"><a :href="'mailto:' + icsrView.email">{{$t('ca3SApp.cSR.requestor.details', {login: icsrView.login, fistName: icsrView.firstName, lastName: icsrView.lastName})}}</a></span>
                        <span v-if="(icsrView.firstName || icsrView.lastName) && !icsrView.email">{{$t('ca3SApp.cSR.requestor.details', {login: icsrView.login, fistName: icsrView.firstName, lastName: icsrView.lastName})}}</span>
                        <span v-if="icsrView.tenantName">{{$t('ca3SApp.cSR.requestor.tenant', {tenant: icsrView.tenantName})}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedOn')"></span>
                    </dt>
                    <dd>
                        <span v-if="icsrView.requestedOn">{{ $d(Date.parse(icsrView.requestedOn), 'long') }}</span>
                    </dd>

                    <dt v-if="icsrView.requestorComment && icsrView.requestorComment.length > 0">
                        <span v-text="$t('ca3SApp.cSR.requestorComment')"></span>
                    </dt>
                    <dd v-if="icsrView.requestorComment && icsrView.requestorComment.length > 0">
                        <span>{{ icsrView.requestorComment }}</span>
                    </dd>

                    <dt v-if="icsrView.acceptedBy">
                        <span v-text="$t('ca3SApp.cSR.acceptedBy')"></span>
                    </dt>
                    <dd v-if="icsrView.acceptedBy">
                        <span>{{ icsrView.acceptedBy }}</span>
                    </dd>

                    <dt v-if="icsrView.approvedOn">
                        <span v-text="$t('ca3SApp.cSR.approvedOn')"></span>
                    </dt>
                    <dd v-if="icsrView.approvedOn">
                        <span v-if="icsrView.approvedOn">{{ $d(Date.parse(icsrView.approvedOn), 'long') }}</span>
                    </dd>

                    <dt v-if="icsrView.status === 'REJECTED'">
                        <span v-text="$t('ca3SApp.cSR.rejectedOn')"></span>
                    </dt>
                    <dd v-if="icsrView.status === 'REJECTED'">
                        <span v-if="icsrView.rejectedOn">{{ $d(Date.parse(icsrView.rejectedOn), 'long') }}</span>
                    </dd>

                    <dt v-if="icsrView.status === 'REJECTED'">
                        <span v-text="$t('ca3SApp.cSR.rejectionReason')"></span>
                    </dt>
                    <dd v-if="icsrView.status === 'REJECTED'">
                        <span>{{ icsrView.rejectionReason }}</span>
                    </dd>

                    <dt v-if="icsrView.administrationComment && icsrView.administrationComment.length > 0">
                        <span v-text="$t('ca3SApp.cSR.administrationComment')"></span>
                    </dt>
                    <dd v-if="icsrView.administrationComment && icsrView.administrationComment.length > 0">
                        <span>{{ icsrView.administrationComment }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.x509KeySpec')"></span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.x509KeySpec }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.cSR.signingAlgorithm')"></span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.signingAlgorithm }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.keyDetails')"></span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.publicKeyAlgorithm }} / {{ icsrView.keyLength }} bits</span>
                    </dd>

                    <dt v-if="icsrView.certificateId">
                        <span v-text="$t('ca3SApp.cSR.certificate')"></span>
                    </dt>
                    <dd v-if="icsrView.certificateId">
                        <div>
                            <router-link :to="{name: 'CertInfo', params: {certificateId: icsrView.certificateId}}">{{icsrView.certificateId}}</router-link>
                        </div>
                    </dd>

                    <dt v-if="icsrView.pipeline && icsrView.pipeline.name && icsrView.pipeline.name.length > 0">
                        <span v-text="$t('ca3SApp.cSR.pipeline')"></span>
                    </dt>
                    <dd v-if="icsrView.pipeline && icsrView.pipeline.name && icsrView.pipeline.name.length > 0">
                        <div>
                            <router-link :to="{name: 'PipelineView', params: {pipelineId: icsrView.pipeline.id}}">{{ icsrView.pipeline.name }}</router-link>
                        </div>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.pipeline')"></span>
                    </dt>
                    <dd>
                        <span>{{ icsrView.pipelineName }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.pipelineType')"></span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.PipelineType.' + icsrView.pipelineType)">{{ icsrView.pipelineType }}</span>
                    </dd>

                    <dt v-if="icsrView.tosAgreed">
                        <span v-text="$t('ca3SApp.cSR.tosAgreed')"></span>
                    </dt>
                    <dd v-if="icsrView.tosAgreed">
                        <a href="{icsrView.tosAgreementLink}">{{icsrView.tosAgreementLink}}</a>
                    </dd>

                    <!--dt>
                        <span v-text="$t('ca3SApp.cSR.processInstanceId')">Process Instance Id</span>
                    </dt>
                    <dd>
                        <span>{{cSR.processInstanceId}}</span>
                    </dd-->

                    <div v-for="attr in arAttributes" :key="attr.name" v-if="!isEditable()">
                        <dt>
                            <span >{{attr.name}}</span>
                        </dt>
                        <dd >
                            <span >{{attr.value}}</span>
                        </dd>
                    </div>


                    <dt v-if="icsrView.csrBase64 && icsrView.csrBase64.trim().length > 0">
                        <span v-text="$t('ca3SApp.cSR.csrBase64')"></span>
                    </dt>
                    <dd v-if="icsrView.csrBase64 && icsrView.csrBase64.trim().length > 0">
                          <textarea class="form-control pem-content"
                            name="csrContent" id="csrContent"
                            autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly
                            v-model="icsrView.csrBase64" />
                          <CopyClipboardButton contentElementId="csrContent"/>
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

                    <div v-if="isEditable() && icsrView.status === 'PENDING'" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.rejectionReason')" for="csr-rejectionReason"></label>
                        <input type="text" class="form-control" name="rejectionReason" id="csr-rejectionReason" v-model="csrAdminData.rejectionReason" />
                    </div>

                    <div v-if="isEditable()" v-for="attr in csrAdminData.arAttributes" :key="attr.name" class="form-group">
                        <label class="form-control-label"  for="csr-ar-{attr.name}">{{attr.name}}</label>
                        <input type="text" class="form-control" name="csr-ar-{attr.name}" id="csr-ar-{attr.name}" v-model="attr.value" />
                    </div>

                    <div v-if="isEditable()" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.comment')" for="comment"></label>
                        <textarea class="form-control" name="comment" id="comment"
                            autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="csrAdminData.comment" />
                    </div>

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
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"></span>
                    </button>

                    <button type="button" id="update" v-if="isRAOfficer()" class="btn btn-secondary" v-on:click="updateCSR()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.update')"></span>
                    </button>

                    <button type="button" id="selfAdminister" v-if="icsrView.status === 'PENDING' && getUsername() === icsrView.requestedBy && !isRAOfficer()"
                            class="btn btn-secondary" v-on:click="selfAdministerRequest()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.update')"></span>
                    </button>

                    <button type="button" id="reject" v-if="icsrView.status === 'PENDING' && isRAOfficer() && !(getUsername() === icsrView.requestedBy)" class="btn btn-secondary" v-on:click="rejectCSR()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.reject')"></span>
                    </button>

                    <button type="button" id="withdraw" v-if="icsrView.status === 'PENDING' && getUsername() === icsrView.requestedBy" class="btn btn-secondary" v-on:click="withdrawCSR()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.withdraw')"></span>
                    </button>

                    <button type="button" id="confirm" v-if="icsrView.status === 'PENDING' && isRAOfficer()" class="btn btn-primary"
                            v-on:click="confirmCSR()">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.confirm')"></span>
                    </button>
                </div>
            </form>

        </div>
    </div>
    </div>
</template>

<script lang="ts" src="./csr-info.component.ts">
</script>
