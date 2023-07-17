<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="acmeAccountView">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.aCMEAccount.detail.title')">AcmeAccount</span> {{acmeAccountView.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.accountId')">Account Id</span>
                    </dt>
                    <dd>
                        <span>{{acmeAccountView.accountId}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.realm')">Realm</span>
                    </dt>
                    <dd>
                        <span>{{acmeAccountView.realm}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.createdOn')">Created On</span>
                    </dt>
                    <dd>
                        <span>{{$d(Date.parse(acmeAccountView.createdOn), 'long') }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.status')">Status</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.aCMEAccount.' + acmeAccountView.status)">{{$t(acmeAccountView.status)}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.termsOfServiceAgreed')">Terms Of Service Agreed</span>
                    </dt>
                    <dd>
                        <span>{{acmeAccountView.termsOfServiceAgreed}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.publicKeyHash')">Public Key Hash</span>
                    </dt>
                    <dd>
                        <span>{{acmeAccountView.publicKeyHash}}</span>
                    </dd>

                    <!--dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.publicKey')">Public Key</span>
                    </dt>

                    <dd>
                        <span>{{acmeAccountView.publicKey}}</span>
                    </dd-->

                    <!--dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.orderCount')">Order Count</span>
                    </dt>
                    <dd>
                        <span>{{acmeAccountView.orderCount}}</span>
                    </dd-->

                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.contacts')">Contacts</span>
                    </dt>
                    <dd>
                        <ul v-if="acmeAccountView.contactUrls && acmeAccountView.contactUrls.length > 0">
                            <li v-for="contacts in acmeAccountView.contactUrls" :key="contacts" >{{contacts}}</li>
                        </ul>
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

                    <div v-if="isRAOfficer() || isAdmin()">
                        <label class="form-control-label" v-text="$t('ca3SApp.aCMEAccount.statusComment')">Status Comment</label>
                        <textarea type="text" class="form-control" name="status-comment" id="status-comment"
                                  autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                  v-model="acmeAccountStatusAdministration.comment" />
                    </div>

                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                    </button>


                    <button type="button" id="deactivate" v-if="acmeAccountView.status == 'valid' &&( isRAOfficer() || isAdmin())" class="btn btn-secondary" v-on:click="deactivateAccount()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.deactivate')">deactivate</span>
                    </button>

                    <button type="button" id="reactivate" v-if="acmeAccountView.status == 'deactivated' &&( isRAOfficer() || isAdmin())" class="btn btn-secondary" v-on:click="reactivateAccount()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.reactivate')">reactivate</span>
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

<script lang="ts" src="./acme-account-info.component.ts">
</script>
