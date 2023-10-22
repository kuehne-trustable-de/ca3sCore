<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="acmeOrderView">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.acmeOrder.home.title')">ACME Order</span> {{acmeOrderView.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.orderId')">Order Id</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrderView.orderId}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.accountId')">Account Id</span>
                    </dt>
                    <dd>
                        <div>
                            <router-link :to="{name: 'AcmeAccountInfo', params: {accountId: acmeOrderView.accountId}}">{{acmeOrderView.accountId}}</router-link>
                        </div>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.realm')">Realm</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrderView.realm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.status')">Status</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.AcmeOrderStatus.' + acmeOrderView.status)">{{acmeOrderView.status}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.created.expires')">Cretaed / Expires</span>
                    </dt>
                    <dd>
                        <span>{{$d(Date.parse(acmeOrderView.createdOn), 'long') }}</span> -> <span v-if="acmeOrderView.expires">{{$d(Date.parse(acmeOrderView.expires), 'long') }}</span>
                    </dd>


                    <dt v-if="acmeOrderView.notBefore && acmeOrderView.notAfter">
                        <span v-text="$t('ca3SApp.acmeOrder.fromTo')">From / To</span>
                    </dt>
                    <dd v-if="acmeOrderView.notBefore && acmeOrderView.notAfter">
                        <span v-if="acmeOrderView.notBefore">{{$d(Date.parse(acmeOrderView.notBefore), 'long') }}</span> -&gt; <span v-if="acmeOrderView.notAfter">{{$d(Date.parse(acmeOrderView.notAfter), 'long') }}</span>
                    </dd>

                    <dt v-if="acmeOrderView.error">
                        <span v-text="$t('ca3SApp.aCMEAccount.error')">Error</span>
                    </dt>
                    <dd v-if="acmeOrderView.error">
                        <span>{{acmeOrderView.error}}</span>
                    </dd>

                    <dt v-if="acmeOrderView.csrId">
                        <span v-text="$t('ca3SApp.certificate.csr')">Csr</span>
                    </dt>
                    <dd v-if="acmeOrderView.csrId">
                        <div>
                            <router-link :to="{name: 'CsrInfo', params: {csrId: acmeOrderView.csrId}}">{{acmeOrderView.csrId}}</router-link>
                        </div>
                    </dd>

                    <dt v-if="acmeOrderView.certificateId">
                        <span v-text="$t('ca3SApp.certificate.certificate')">Certificate</span>
                    </dt>
                    <dd v-if="acmeOrderView.certificateId">
                        <div>
                            <router-link :to="{name: 'CertInfo', params: {certificateId: acmeOrderView.certificateId}}">{{acmeOrderView.certificateId}}</router-link>
                        </div>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.acmeChallenges')">Challenges</span>
                    </dt>
                    <dd>
                        <challenges-tag v-if="acmeOrderView.id" :orderId="acmeOrderView.id" ></challenges-tag>
                    </dd>


                    <dt>
                    </dt>
                    <dd>
                        <audit-tag :acmeOrderId="acmeOrderView.id" showLinks="true"></audit-tag>
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

<script lang="ts" src="./acme-order-info.component.ts">
</script>
