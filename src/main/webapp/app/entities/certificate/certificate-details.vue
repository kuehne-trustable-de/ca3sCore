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
                        <span v-text="$t('ca3SApp.certificate.sans')">Sans</span>
                    </dt>
                    <dd>
                        <span>{{certificate.sans}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.issuer')">Issuer</span>
                    </dt>
                    <dd>
                        <span>{{certificate.issuer}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.root')">Root</span>
                    </dt>
                    <dd>
                        <span>{{certificate.root}}</span>
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
                        <span v-if="certificate.validFrom">{{$d(Date.parse(certificate.validFrom), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.validTo')">Valid To</span>
                    </dt>
                    <dd>
                        <span v-if="certificate.validTo">{{$d(Date.parse(certificate.validTo), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.keyAlgorithm')">Key Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificate.keyAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.keyLength')">Key Length</span>
                    </dt>
                    <dd>
                        <span>{{certificate.keyLength}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.curveName')">Curve Name</span>
                    </dt>
                    <dd>
                        <span>{{certificate.curveName}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.hashingAlgorithm')">Hashing Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificate.hashingAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.paddingAlgorithm')">Padding Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificate.paddingAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.signingAlgorithm')">Signing Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificate.signingAlgorithm}}</span>
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
                        <span v-if="certificate.contentAddedAt">{{$d(Date.parse(certificate.contentAddedAt), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.revokedSince')">Revoked Since</span>
                    </dt>
                    <dd>
                        <span v-if="certificate.revokedSince">{{$d(Date.parse(certificate.revokedSince), 'long') }}</span>
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
                        <span v-text="$t('ca3SApp.certificate.administrationComment')">Administration Comment</span>
                    </dt>
                    <dd>
                        <span>{{certificate.administrationComment}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.endEntity')">End Entity</span>
                    </dt>
                    <dd>
                        <span>{{certificate.endEntity}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.selfsigned')">Selfsigned</span>
                    </dt>
                    <dd>
                        <span>{{certificate.selfsigned}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.trusted')">Trusted</span>
                    </dt>
                    <dd>
                        <span>{{certificate.trusted}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.active')">Active</span>
                    </dt>
                    <dd>
                        <span>{{certificate.active}}</span>
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
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.rootCertificate')">Root Certificate</span>
                    </dt>
                    <dd>
                        <div v-if="certificate.rootCertificate">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: certificate.rootCertificate.id}}">{{certificate.rootCertificate.id}}</router-link>
                        </div>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.revocationCA')">Revocation CA</span>
                    </dt>
                    <dd>
                        <div v-if="certificate.revocationCA">
                            <router-link :to="{name: 'CAConnectorConfigView', params: {cAConnectorConfigId: certificate.revocationCA.id}}">{{certificate.revocationCA.id}}</router-link>
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
