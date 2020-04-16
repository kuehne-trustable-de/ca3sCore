<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.certificate.home.title')" id="certificate-heading">Certificates</span>
            <router-link :to="{name: 'CertificateCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-certificate">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.certificate.home.createLabel')">
                    Create a new Certificate
                </span>
            </router-link>
        </h2>
        <b-alert :show="dismissCountDown"
            dismissible
            :variant="alertType"
            @dismissed="dismissCountDown=0"
            @dismiss-count-down="countDownChanged">
            {{alertMessage}}
        </b-alert>
        <br/>
        <div class="alert alert-warning" v-if="!isFetching && certificates && certificates.length === 0">
            <span v-text="$t('ca3SApp.certificate.home.notFound')">No certificates found</span>
        </div>
        <div class="table-responsive" v-if="certificates && certificates.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th v-on:click="changeOrder('id')"><span v-text="$t('global.field.id')">ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('tbsDigest')"><span v-text="$t('ca3SApp.certificate.tbsDigest')">Tbs Digest</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'tbsDigest'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('subject')"><span v-text="$t('ca3SApp.certificate.subject')">Subject</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'subject'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('sans')"><span v-text="$t('ca3SApp.certificate.sans')">Sans</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'sans'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('issuer')"><span v-text="$t('ca3SApp.certificate.issuer')">Issuer</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'issuer'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('root')"><span v-text="$t('ca3SApp.certificate.root')">Root</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'root'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('type')"><span v-text="$t('ca3SApp.certificate.type')">Type</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'type'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('description')"><span v-text="$t('ca3SApp.certificate.description')">Description</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'description'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('fingerprint')"><span v-text="$t('ca3SApp.certificate.fingerprint')">Fingerprint</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fingerprint'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('serial')"><span v-text="$t('ca3SApp.certificate.serial')">Serial</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'serial'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('validFrom')"><span v-text="$t('ca3SApp.certificate.validFrom')">Valid From</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'validFrom'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('validTo')"><span v-text="$t('ca3SApp.certificate.validTo')">Valid To</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'validTo'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('keyAlgorithm')"><span v-text="$t('ca3SApp.certificate.keyAlgorithm')">Key Algorithm</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'keyAlgorithm'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('keyLength')"><span v-text="$t('ca3SApp.certificate.keyLength')">Key Length</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'keyLength'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('curveName')"><span v-text="$t('ca3SApp.certificate.curveName')">Curve Name</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'curveName'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('hashingAlgorithm')"><span v-text="$t('ca3SApp.certificate.hashingAlgorithm')">Hashing Algorithm</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'hashingAlgorithm'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('paddingAlgorithm')"><span v-text="$t('ca3SApp.certificate.paddingAlgorithm')">Padding Algorithm</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'paddingAlgorithm'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('signingAlgorithm')"><span v-text="$t('ca3SApp.certificate.signingAlgorithm')">Signing Algorithm</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'signingAlgorithm'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('creationExecutionId')"><span v-text="$t('ca3SApp.certificate.creationExecutionId')">Creation Execution Id</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'creationExecutionId'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('contentAddedAt')"><span v-text="$t('ca3SApp.certificate.contentAddedAt')">Content Added At</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'contentAddedAt'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('revokedSince')"><span v-text="$t('ca3SApp.certificate.revokedSince')">Revoked Since</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'revokedSince'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('revocationReason')"><span v-text="$t('ca3SApp.certificate.revocationReason')">Revocation Reason</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'revocationReason'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('revoked')"><span v-text="$t('ca3SApp.certificate.revoked')">Revoked</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'revoked'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('revocationExecutionId')"><span v-text="$t('ca3SApp.certificate.revocationExecutionId')">Revocation Execution Id</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'revocationExecutionId'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('administrationComment')"><span v-text="$t('ca3SApp.certificate.administrationComment')">Administration Comment</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'administrationComment'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('endEntity')"><span v-text="$t('ca3SApp.certificate.endEntity')">End Entity</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'endEntity'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('selfsigned')"><span v-text="$t('ca3SApp.certificate.selfsigned')">Selfsigned</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'selfsigned'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('content')"><span v-text="$t('ca3SApp.certificate.content')">Content</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'content'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('csr.id')"><span v-text="$t('ca3SApp.certificate.csr')">Csr</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'csr.id'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('issuingCertificate.id')"><span v-text="$t('ca3SApp.certificate.issuingCertificate')">Issuing Certificate</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'issuingCertificate.id'"></jhi-sort-indicator></th>
                    <th v-on:click="changeOrder('rootCertificate.id')"><span v-text="$t('ca3SApp.certificate.rootCertificate')">Root Certificate</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'rootCertificate.id'"></jhi-sort-indicator></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="certificate in certificates"
                    :key="certificate.id">
                    <td>
                        <router-link :to="{name: 'CertificateView', params: {certificateId: certificate.id}}">{{certificate.id}}</router-link>
                    </td>
                    <td>{{certificate.tbsDigest}}</td>
                    <td>{{certificate.subject}}</td>
                    <td>{{certificate.sans}}</td>
                    <td>{{certificate.issuer}}</td>
                    <td>{{certificate.root}}</td>
                    <td>{{certificate.type}}</td>
                    <td>{{certificate.description}}</td>
                    <td>{{certificate.fingerprint}}</td>
                    <td>{{certificate.serial}}</td>
                    <td>{{certificate.validFrom ? $d(Date.parse(certificate.validFrom), 'short') : ''}}</td>
                    <td>{{certificate.validTo ? $d(Date.parse(certificate.validTo), 'short') : ''}}</td>
                    <td>{{certificate.keyAlgorithm}}</td>
                    <td>{{certificate.keyLength}}</td>
                    <td>{{certificate.curveName}}</td>
                    <td>{{certificate.hashingAlgorithm}}</td>
                    <td>{{certificate.paddingAlgorithm}}</td>
                    <td>{{certificate.signingAlgorithm}}</td>
                    <td>{{certificate.creationExecutionId}}</td>
                    <td>{{certificate.contentAddedAt ? $d(Date.parse(certificate.contentAddedAt), 'short') : ''}}</td>
                    <td>{{certificate.revokedSince ? $d(Date.parse(certificate.revokedSince), 'short') : ''}}</td>
                    <td>{{certificate.revocationReason}}</td>
                    <td>{{certificate.revoked}}</td>
                    <td>{{certificate.revocationExecutionId}}</td>
                    <td>{{certificate.administrationComment}}</td>
                    <td>{{certificate.endEntity}}</td>
                    <td>{{certificate.selfsigned}}</td>
                    <td>{{certificate.content}}</td>
                    <td>
                        <div v-if="certificate.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: certificate.csr.id}}">{{certificate.csr.id}}</router-link>
                        </div>
                    </td>
                    <td>
                        <div v-if="certificate.issuingCertificate">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: certificate.issuingCertificate.id}}">{{certificate.issuingCertificate.id}}</router-link>
                        </div>
                    </td>
                    <td>
                        <div v-if="certificate.rootCertificate">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: certificate.rootCertificate.id}}">{{certificate.rootCertificate.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: certificate.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'CertificateEdit', params: {certificateId: certificate.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(certificate)"
                                   variant="danger"
                                   class="btn btn-sm"
                                   v-b-modal.removeEntity>
                                <font-awesome-icon icon="times"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
                            </b-button>
                        </div>
                    </td>
                </tr>
                </tbody>
                <infinite-loading
                    ref="infiniteLoading"
                    v-if="totalItems > itemsPerPage"
                    :identifier="infiniteId"
                    slot="append"
                    @infinite="loadMore"
                    force-use-infinite-wrapper=".el-table__body-wrapper"
                    :distance='20'>
                </infinite-loading>
            </table>
        </div>
        <b-modal ref="removeEntity" id="removeEntity" >
            <span slot="modal-title"><span id="ca3SApp.certificate.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-certificate-heading" v-text="$t('ca3SApp.certificate.delete.question', {'id': removeId})">Are you sure you want to delete this Certificate?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-certificate" v-text="$t('entity.action.delete')" v-on:click="removeCertificate()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./certificate.component.ts">
</script>
