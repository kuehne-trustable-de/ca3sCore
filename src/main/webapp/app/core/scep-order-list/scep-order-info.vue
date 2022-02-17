<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="scepOrderView">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.scepOrder.home.title')">ACME Order</span> {{scepOrderView.id}}</h2>
                <dl class="row jh-entity-details">



                    <dt>
                        <span v-text="$t('ca3SApp.scepOrder.transId')">transId</span>
                    </dt>
                    <dd>
                        <span>{{scepOrderView.transId}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('status')">Status</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.scepOrderStatus.' + scepOrderView.status)">{{scepOrderView.status}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('realm')">Realm</span>
                    </dt>

                    <dd>
                        <span>{{scepOrderView.realm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.scepOrder.pipeline')">Pipeline</span>
                    </dt>
                    <dd>
                        <span>{{scepOrderView.pipelineName}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('subject')">subject</span>
                    </dt>
                    <dd>
                        <span>{{scepOrderView.subject}}</span>
                    </dd>


                    <dt v-if="scepOrderView.sanArr && scepOrderView.sanArr.length > 0">
                        <span v-text="$t('ca3SApp.certificate.sans')">Subject alternative names</span>
                    </dt>
                    <dd v-if="scepOrderView.sanArr && scepOrderView.sanArr.length > 0">
                        <ul>
                            <li v-for="san in scepOrderView.sanArr" :key="san" >{{san}}</li>
                        </ul>
                    </dd>


                    <dt v-if="scepOrderView.requestedOn">
                        <span v-text="$t('ca3SApp.cSR.requestedOn')">requestedOn</span>
                    </dt>
                    <dd v-if="scepOrderView.requestedOn">
                        <span>{{$d(Date.parse(scepOrderView.requestedOn), 'long') }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.cSR.requestedBy')">requestedBy</span>
                    </dt>
                    <dd>
                        <span>{{scepOrderView.requestedBy}}</span>
                    </dd>

<!--
                    asyncProcessing?: boolean;
                    passwordAuthentication?: boolean;
-->

                    <dt v-if="scepOrderView.csrId">
                        <span v-text="$t('ca3SApp.certificate.csr')">Csr</span>
                    </dt>
                    <dd v-if="scepOrderView.csrId">
                        <div>
                            <router-link :to="{name: 'CsrInfo', params: {csrId: scepOrderView.csrId}}">{{scepOrderView.csrId}}</router-link>
                        </div>
                    </dd>

                    <dt v-if="scepOrderView.certificateId">
                        <span v-text="$t('ca3SApp.certificate.certificate')">Certificate</span>
                    </dt>
                    <dd v-if="scepOrderView.certificateId">
                        <div>
                            <router-link :to="{name: 'CertInfo', params: {certificateId: scepOrderView.certificateId}}">{{scepOrderView.certificateId}}</router-link>
                        </div>
                    </dd>

                </dl>

            </div>

            <form name="editForm" role="form" novalidate>
                <div>
<!--
                    <Fragment v-if="cSR.status === 'PENDING' && ((roles === 'ROLE_RA') || (getUsername() === cSR.requestedBy))">

                        <div v-for="attr in csrAdminData.arAttributes" :key="attr.name" class="form-group">
                            <label class="form-control-label"  for="csr-ar-{attr.name}">{{attr.name}}</label>
                            <input type="text" class="form-control" name="rejectionReason" id="csr-ar-{attr.name}" v-model="attr.value" />
                        </div>

                        <div class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.cSR.rejectionReason')" for="csr-rejectionReason">rejection reason</label>
                            <input type="text" class="form-control" name="rejectionReason" id="csr-rejectionReason" v-model="csrAdminData.rejectionReason" />
                        </div>

                        <div class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.certificate.comment')" for="comment">Comment</label>
                            <textarea class="form-control" name="content" id="comment"
                                autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                v-model="csrAdminData.comment" />
                        </div>
                    </Fragment>
-->

                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                    </button>

                    <!--
                    <button type="button" id="reject" v-if="cSR.status === 'PENDING' && roles === 'ROLE_RA' && !(getUsername() === cSR.requestedBy)" class="btn btn-secondary" v-on:click="rejectCSR()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.reject')">Reject</span>
                    </button>

                    <button type="button" id="withdraw" v-if="cSR.status === 'PENDING' && getUsername() === cSR.requestedBy" class="btn btn-secondary" v-on:click="withdrawCSR()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.withdraw')">Withdraw</span>
                    </button>

                    <button type="button" id="update" v-if="cSR.status === 'PENDING' && roles === 'ROLE_RA'" class="btn btn-secondary" v-on:click="updateCSR()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')">Update</span>
                    </button>

                    <button type="button" id="selfAdminister" v-if="cSR.status === 'PENDING' && getUsername() === cSR.requestedBy && roles !== 'ROLE_RA'"
                            class="btn btn-secondary" v-on:click="selfAdministerRequest()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.update')">Update</span>
                    </button>

                    <button type="button" id="confirm" v-if="cSR.status === 'PENDING' && roles === 'ROLE_RA'" class="btn btn-primary"
                        v-on:click="confirmCSR()">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.confirm')">Confirm</span>
                    </button>
                    -->
                </div>
            </form>


        </div>
    </div>
</template>

<script lang="ts" src="./scep-order-info.component.ts">
</script>
