<template>
    <div>
        <h2 id="configuration-page-heading" v-text="$t('ca3SApp.notification.title')">Notifications</h2>

        <p/>

        <div class="form-group" >
            <label class="form-control-label" v-text="$t('ca3SApp.notification.select')" for="notification-select">Notification Selection</label>
            <select class="form-control" id="notification-select" name="notification-select" v-model="selectedNotification" v-on:change="updateNotification()">

                <option value="sendRequestorExpirySummary" v-text="$t('ca3SApp.notification.sendRequestorExpirySummary')" selected="selected">sendRequestorExpirySummary</option>
                <option value="sendExpiryPendingSummary" v-text="$t('ca3SApp.notification.sendExpiryPendingSummary')">sendExpiryPendingSummary</option>
                <option value="sendRAOfficerOnRequest" v-text="$t('ca3SApp.notification.sendRAOfficerOnRequest')" >sendRAOfficerOnRequest</option>
                <option value="sendUserCertificateIssued" v-text="$t('ca3SApp.notification.sendUserCertificateIssued')" >sendUserCertificateIssued</option>
                <option value="sendUserCertificateRejected" v-text="$t('ca3SApp.notification.sendUserCertificateRejected')" >sendUserCertificateRejected</option>
                <option value="sendCertificateRevoked" v-text="$t('ca3SApp.notification.sendCertificateRevoked')" >sendCertificateRevoked</option>
                <option value="sendUserCertificateRevoked" v-text="$t('ca3SApp.notification.sendUserCertificateRevoked')" >sendUserCertificateRevoked</option>
            </select>
        </div>

        <form name="editForm" role="form" autocomplete="off" novalidate >

            <div v-if="selectedNotification === 'sendRequestorExpirySummary' || selectedNotification === 'sendUserCertificateIssued' || selectedNotification === 'sendCertificateRevoked' || selectedNotification === 'sendUserCertificateRevoked'">
                <div class="row">
                    <div class="col">
                        <label class="form-control-label" v-text="$t('ca3SApp.notification.certificate.id')" for="certificate-id">select certificate id</label>
                    </div>
                    <div class="col colContent">
                        <input type="text" class="form-control" name="certificate-id" id="certificate-id" v-model="certificateId" />
                    </div>
                </div>

            </div>

            <div v-if="selectedNotification === 'sendUserCertificateRejected' || selectedNotification === 'sendRAOfficerOnRequest'" >

                <div class="row">
                    <div class="col">
                        <label class="form-control-label" v-text="$t('ca3SApp.notification.csr.id')" for="csr-id">select request id</label>
                    </div>
                    <div class="col colContent">
                        <input type="text" class="form-control" name="csr-id" id="csr-id" v-model="csrId" />
                    </div>
                </div>

            </div>

            <button type="button" id="send-issued" class="btn btn-primary" v-on:click="send()">
                <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.notification.action.send')">Send</span>
            </button>

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
        </form>
    </div>
</template>

<script lang="ts" src="./notification.components.ts">
</script>
