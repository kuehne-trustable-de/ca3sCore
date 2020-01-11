<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="certificate">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.certificate.detail.title')">Certificate</span> {{certificate.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.tbsDigest')">Tbs Digest</span>
                    </dt>
                    <dd>
                        <span>{{certificate.tbsDigest}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.subject')">Subject</span>
                    </dt>
                    <dd>
                        <span>{{certificate.subject}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.issuer')">Issuer</span>
                    </dt>
                    <dd>
                        <span>{{certificate.issuer}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{certificate.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.description')">Description</span>
                    </dt>
                    <dd>
                        <span>{{certificate.description}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.subjectKeyIdentifier')">Subject Key Identifier</span>
                    </dt>
                    <dd>
                        <span>{{certificate.subjectKeyIdentifier}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.authorityKeyIdentifier')">Authority Key Identifier</span>
                    </dt>
                    <dd>
                        <span>{{certificate.authorityKeyIdentifier}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.fingerprint')">Fingerprint</span>
                    </dt>
                    <dd>
                        <span>{{certificate.fingerprint}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.serial')">Serial</span>
                    </dt>
                    <dd>
                        <span>{{certificate.serial}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.validFrom')">Valid From</span>
                    </dt>
                    <dd>
                        <span>{{certificate.validFrom}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.validTo')">Valid To</span>
                    </dt>
                    <dd>
                        <span>{{certificate.validTo}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.creationExecutionId')">Creation Execution Id</span>
                    </dt>
                    <dd>
                        <span>{{certificate.creationExecutionId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.contentAddedAt')">Content Added At</span>
                    </dt>
                    <dd>
                        <span>{{certificate.contentAddedAt}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.revokedSince')">Revoked Since</span>
                    </dt>
                    <dd>
                        <span>{{certificate.revokedSince}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.revocationReason')">Revocation Reason</span>
                    </dt>
                    <dd>
                        <span>{{certificate.revocationReason}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.revoked')">Revoked</span>
                    </dt>
                    <dd>
                        <span>{{certificate.revoked}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.revocationExecutionId')">Revocation Execution Id</span>
                    </dt>
                    <dd>
                        <span>{{certificate.revocationExecutionId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.content')">Content</span>
                    </dt>
                    <dd>
                        <span>{{certificate.content}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.csr')">Csr</span>
                    </dt>
                    <dd>
                        <div v-if="certificate.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: certificate.csr.id}}">{{certificate.csr.id}}</router-link>
                        </div>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.issuingCertificate')">Issuing Certificate</span>
                    </dt>
                    <dd>
                        <div v-if="certificate.issuingCertificate">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: certificate.issuingCertificate.id}}">{{certificate.issuingCertificate.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="certificate.id" :to="{name: 'CertificateEdit', params: {certificateId: certificate.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./certificate-details.component.ts">
</script>
