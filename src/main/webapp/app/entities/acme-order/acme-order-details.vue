<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="acmeOrder">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.acmeOrder.detail.title')">AcmeOrder</span> {{acmeOrder.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.orderId')">Order Id</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrder.orderId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.status')">Status</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.OrderStatus.' + acmeOrder.status)">{{acmeOrder.status}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.expires')">Expires</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrder.expires}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.notBefore')">Not Before</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrder.notBefore}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.notAfter')">Not After</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrder.notAfter}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.error')">Error</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrder.error}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.finalizeUrl')">Finalize Url</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrder.finalizeUrl}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.certificateUrl')">Certificate Url</span>
                    </dt>
                    <dd>
                        <span>{{acmeOrder.certificateUrl}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.csr')">Csr</span>
                    </dt>
                    <dd>
                        <div v-if="acmeOrder.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: acmeOrder.csr.id}}">{{acmeOrder.csr.id}}</router-link>
                        </div>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.certificate')">Certificate</span>
                    </dt>
                    <dd>
                        <div v-if="acmeOrder.certificate">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: acmeOrder.certificate.id}}">{{acmeOrder.certificate.id}}</router-link>
                        </div>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeOrder.account')">Account</span>
                    </dt>
                    <dd>
                        <div v-if="acmeOrder.account">
                            <router-link :to="{name: 'ACMEAccountView', params: {aCMEAccountId: acmeOrder.account.id}}">{{acmeOrder.account.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="acmeOrder.id" :to="{name: 'AcmeOrderEdit', params: {acmeOrderId: acmeOrder.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./acme-order-details.component.ts">
</script>
