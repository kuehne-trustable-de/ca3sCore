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
                    <th v-on:click="changeOrder('id')"><span v-text="$t('global.field.id')">ID</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('tbsDigest')"><span v-text="$t('ca3SApp.certificate.tbsDigest')">Tbs Digest</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('subject')"><span v-text="$t('ca3SApp.certificate.subject')">Subject</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('issuer')"><span v-text="$t('ca3SApp.certificate.issuer')">Issuer</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('type')"><span v-text="$t('ca3SApp.certificate.type')">Type</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('description')"><span v-text="$t('ca3SApp.certificate.description')">Description</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('subjectKeyIdentifier')"><span v-text="$t('ca3SApp.certificate.subjectKeyIdentifier')">Subject Key Identifier</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('authorityKeyIdentifier')"><span v-text="$t('ca3SApp.certificate.authorityKeyIdentifier')">Authority Key Identifier</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('fingerprint')"><span v-text="$t('ca3SApp.certificate.fingerprint')">Fingerprint</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('serial')"><span v-text="$t('ca3SApp.certificate.serial')">Serial</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('validFrom')"><span v-text="$t('ca3SApp.certificate.validFrom')">Valid From</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('validTo')"><span v-text="$t('ca3SApp.certificate.validTo')">Valid To</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('creationExecutionId')"><span v-text="$t('ca3SApp.certificate.creationExecutionId')">Creation Execution Id</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('contentAddedAt')"><span v-text="$t('ca3SApp.certificate.contentAddedAt')">Content Added At</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('revokedSince')"><span v-text="$t('ca3SApp.certificate.revokedSince')">Revoked Since</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('revocationReason')"><span v-text="$t('ca3SApp.certificate.revocationReason')">Revocation Reason</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('revoked')"><span v-text="$t('ca3SApp.certificate.revoked')">Revoked</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('revocationExecutionId')"><span v-text="$t('ca3SApp.certificate.revocationExecutionId')">Revocation Execution Id</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('content')"><span v-text="$t('ca3SApp.certificate.content')">Content</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('csr.id')"><span v-text="$t('ca3SApp.certificate.csr')">Csr</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
                    <th v-on:click="changeOrder('issuingCertificate.id')"><span v-text="$t('ca3SApp.certificate.issuingCertificate')">Issuing Certificate</span> <font-awesome-icon icon="sort"></font-awesome-icon></th>
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
                    <td>{{certificate.issuer}}</td>
                    <td>{{certificate.type}}</td>
                    <td>{{certificate.description}}</td>
                    <td>{{certificate.subjectKeyIdentifier}}</td>
                    <td>{{certificate.authorityKeyIdentifier}}</td>
                    <td>{{certificate.fingerprint}}</td>
                    <td>{{certificate.serial}}</td>
                    <td v-if="certificate.validFrom"> {{$d(Date.parse(certificate.validFrom), 'short') }}</td>
                    <td v-if="certificate.validTo"> {{$d(Date.parse(certificate.validTo), 'short') }}</td>
                    <td>{{certificate.creationExecutionId}}</td>
                    <td v-if="certificate.contentAddedAt"> {{$d(Date.parse(certificate.contentAddedAt), 'short') }}</td>
                    <td v-if="certificate.revokedSince"> {{$d(Date.parse(certificate.revokedSince), 'short') }}</td>
                    <td>{{certificate.revocationReason}}</td>
                    <td>{{certificate.revoked}}</td>
                    <td>{{certificate.revocationExecutionId}}</td>
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
            </table>
        </div>
        <b-modal ref="removeEntity" id="removeEntity" >
            <span slot="modal-title"><span id="ca3SApp.certificate.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-certificate-heading" v-bind:title="$t('ca3SApp.certificate.delete.question')">Are you sure you want to delete this Certificate?</p>
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
