<template>
    <div>
        <h2 id="configuration-page-heading" v-text="$t('ca3SApp.info.title')">Info</h2>

        <p/>
        <div>
            <label class="form-control-label" v-text="$t('ca3SApp.notification.certificate.id')" for="certificate-id">send 'user cerificate issued'</label>
            <input type="text" class="form-control" name="certificate-id" id="certificate-id" v-model="certificateId" />

            <button type="button" id="send-issued" class="btn btn-primary" v-on:click="sendUserCertificateIssued()">
                <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.notification.action.send')">Send</span>
            </button>
        </div>


        <p/>
        <div>
            <label class="form-control-label" v-text="$t('ca3SApp.notification.csr.id')" for="csr-id">send 'user certificate rejected'</label>
            <input type="text" class="form-control" name="csr-id" id="csr-id" v-model="csrId" />

            <button type="button" id="send-rejected" class="btn btn-primary" v-on:click="sendUserCertificateRejected()">
                <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.notification.action.send')">Send</span>
            </button>
        </div>

        <p/>
        <div>
            <label class="form-control-label" v-text="$t('ca3SApp.notification.expiry.summary')" for="send-expiry">send 'expiry pending summary'</label>
            <button type="button" id="send-expiry" class="btn btn-primary" v-on:click="sendExpiryPendingSummary()">
                <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.notification.action.send')">Send</span>
            </button>
        </div>

        <p/>
        <dl class="row jh-entity-details" v-if="problemDetail.title">
            <dt>
                <span v-text="$t('ca3SApp.notification.message.title')">Title</span>
            </dt>
            <dd>
                <span>{{problemDetail.title}}</span>
            </dd>
            <dt>
                <span v-text="$t('ca3SApp.notification.message.type')">Title</span>
            </dt>
            <dd>
                <span>{{problemDetail.type}} / {{problemDetail.status}}</span>
            </dd>
            <dt>
                <span v-text="$t('ca3SApp.notification.message.details')">Title</span>
            </dt>
            <dd>
                <span>{{problemDetail.detail}}</span>
            </dd>
        </dl>
    </div>
</template>

<script lang="ts" src="./notification.components.ts">
</script>
