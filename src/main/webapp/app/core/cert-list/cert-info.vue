<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="certificate">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.certificate.detail.title')">Certificate</span> {{certificate.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.subject')">Subject</span>
                    </dt>
                    <dd>
                        <span>{{certificate.subject}}</span>
                    </dd>

                    <dt v-if="certificate.sans && certificate.sans.length > 0">
						<span v-text="$t('ca3SApp.certificate.sans')">Subject alternative names</span>
					</dt>
					<dd v-if="certificate.sans && certificate.sans.length > 0">
						<ul>
							<li v-for="san in sansOnly(certificate.certificateAttributes)" :key="san.Id" >{{san.value}}</li>
						</ul>
					</dd>



                    <dt>
                        <span v-text="$t('ca3SApp.certificate.issuer')">Issuer</span>
                    </dt>
                    <dd>
                        <span v-if="certificate.selfsigned" v-text="$t('ca3SApp.certificate.selfsigned')">Selfsigned</span>
                        <router-link v-else-if="certificate.issuingCertificate" :to="{name: 'CertificateView', params: {certificateId: certificate.issuingCertificate.id}}">{{certificate.issuer}}</router-link>
                        <span v-else>{{certificate.issuer}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{certificate.type}}</span>
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
                    <dt v-if="certificate.curveName">
                        <span v-text="$t('ca3SApp.certificate.curveName')">Curve Name</span>
                    </dt>
                    <dd v-if="certificate.curveName">
                        <span>{{certificate.curveName}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.signingAlgorithm')">Signing Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificate.signingAlgorithm}}</span>
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
                        <span v-text="$t('ca3SApp.certificate.usage')">Usage</span>
                    </dt>
                    <dd>
                        <span>{{usage}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.certificate.contentAddedAt')">Content Added At</span>
                    </dt>
                    <dd>
                        <span v-if="certificate.contentAddedAt">{{$d(Date.parse(certificate.contentAddedAt), 'long') }}</span>
                    </dd>
                    <dt v-if="certificate.revoked">
                        <span v-text="$t('ca3SApp.certificate.revokedSince')">Revoked Since</span>
                    </dt>
                    <dd v-if="certificate.revoked">
                        <span v-if="certificate.revokedSince">{{$d(Date.parse(certificate.revokedSince), 'long') }}</span>
                    </dd>
                    <dt v-if="certificate.revoked">
                        <span v-text="$t('ca3SApp.certificate.revocationReason')">Revocation Reason</span>
                    </dt>
                    <dd v-if="certificate.revoked">
                        <span>{{certificate.revocationReason}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.fingerprint')">Fingerprint</span>
                    </dt>
                    <dd>
                        <span>{{certificate.fingerprint}}</span>
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

                    <dt v-if="certificate.subject">
                        <span v-text="$t('ca3SApp.certificate.download.pkix')">Pkix</span>
                    </dt>
                    <dd v-if="certificate.subject">
                        <div>
                            <a href="downloadUrl" @click.prevent="downloadItem('.crt', 'application/pkix-cert')" >{{certificate.subject}}.crt</a>
                        </div>
                    </dd>
                    <dt v-if="certificate.subject">
                        <span v-text="$t('ca3SApp.certificate.download.pem')">PEM</span>
                    </dt>
                    <dd v-if="certificate.subject">
                        <div>
                            <a href="downloadUrl" @click.prevent="downloadItem('.cer', 'application/pem-certificate')" >{{certificate.subject}}.cer</a>
                        </div>
                    </dd>


                </dl>
            </div>
<!--
    certificateAdminData
-->
            <form name="editForm" role="form" novalidate>
                <div>
                    <div v-if="isRevocable()" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.cSR.revocationReason')" for="revocationReason">Revocation reason</label>
                        <select class="form-control" id="cert-revocationReason" name="revocationReason" v-model="certificateAdminData.revocationReason">
                            <option v-bind:value="'keyCompromise'">keyCompromise</option>
                            <option v-bind:value="'cACompromise'">cACompromise</option>
                            <option v-bind:value="'affiliationChanged'">affiliationChanged</option>
                            <option v-bind:value="'superseded'">superseded</option>
                            <option v-bind:value="'cessationOfOperation'">cessationOfOperation</option>
                            <option v-bind:value="'privilegeWithdrawn'">privilegeWithdrawn</option>
                            <option v-bind:value="'unspecified'">unspecified</option>
                        </select>
                    </div>

                    <div v-if="isRevocable()" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.comment')" for="comment">Comment</label>
                        <textarea class="form-control" name="content" id="comment"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="certificateAdminData.comment" />
                    </div>

                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                    </button>

                    <button type="button" id="revoke" v-if="isRAOfficer() && !isOwnCertificate() && isRevocable()" class="btn btn-secondary" v-on:click="revokeCertificate()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.revoke')">Revoke</span>
                    </button>

                    <button type="button" id="withdraw" v-if="isOwnCertificate() && isRevocable()" class="btn btn-secondary" v-on:click="withdrawCertificate()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.withdraw')">Withdraw</span>
                    </button>

                </div>
            </form>

        </div>
    </div>
</template>

<script lang="ts" src="./cert-info.component.ts">
</script>
