<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="certificateView">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.certificate.detail.title')">Certificate</span> {{certificateView.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.subject')">Subject</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.subject}}</span>
                    </dd>

                    <dt v-if="certificateView.sanArr && certificateView.sanArr.length > 0">
						<span v-text="$t('ca3SApp.certificate.sans')">Subject alternative names</span>
					</dt>
					<dd v-if="certificateView.sanArr && certificateView.sanArr.length > 0">
						<ul>
							<li v-for="san in certificateView.sanArr" :key="san" >{{san}}</li>
						</ul>
					</dd>

                    <dt>
                        <span v-text="$t('ca3SApp.certificate.issuer')">Issuer</span>
                    </dt>
                    <dd>
                        <span v-if="certificateView.selfsigned" v-text="$t('ca3SApp.certificate.selfsigned')">Selfsigned</span>
                        <router-link v-else-if="certificateView.issuerId" :to="{name: 'CertInfo', params: {certificateId: certificateView.issuerId}}">{{certificateView.issuer}}</router-link>
                        <span v-else>{{certificateView.issuer}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.certificate.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.serial')">Serial</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.serial}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.validFrom')">Valid From</span>
                    </dt>
                    <dd>
                        <span v-if="certificateView.validFrom">{{$d(Date.parse(certificateView.validFrom), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.validTo')">Valid To</span>
                    </dt>
                    <dd>
                        <span v-if="certificateView.validTo">{{$d(Date.parse(certificateView.validTo), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.keyAlgorithm')">Key Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.keyAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.keyLength')">Key Length</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.keyLength}}</span>
                    </dd>
                    <dt v-if="certificateView.curveName && certificateView.curveName.length > 0">
                        <span v-text="$t('ca3SApp.certificate.curveName')">Curve Name</span>
                    </dt>
                    <dd v-if="certificateView.curveName && certificateView.curveName.length > 0">
                        <span>{{certificateView.curveName}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.signingAlgorithm')">Signing Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.signingAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.hashingAlgorithm')">Hashing Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.hashAlgorithm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.paddingAlgorithm')">Padding Algorithm</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.paddingAlgorithm}}</span>
                    </dd>
                    
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.usage')">Usage</span>
                    </dt>
					<dd>
						<ul v-if="certificateView.usage && certificateView.usage.length > 0">
							<li v-for="usg in certificateView.usage" :key="usg" >{{usg}}</li>
						</ul>
					</dd>

                    <dt v-if="certificateView.extUsage && certificateView.extUsage.length > 0">
                        <span v-text="$t('ca3SApp.certificate.extended.usage')">Usage</span>
                    </dt>
					<dd v-if="certificateView.extUsage && certificateView.extUsage.length > 0">
						<ul>
							<li v-for="extUsage in certificateView.extUsage" :key="extUsage" >{{extUsage}}</li>
						</ul>
					</dd>

                    <dt>
                        <span v-text="$t('ca3SApp.certificate.contentAddedAt')">Content Added At</span>
                    </dt>
                    <dd>
                        <span v-if="certificateView.contentAddedAt">{{$d(Date.parse(certificateView.contentAddedAt), 'long') }}</span>
                    </dd>
                    <dt v-if="certificateView.revoked">
                        <span v-text="$t('ca3SApp.certificate.revokedSince')">Revoked Since</span>
                    </dt>
                    <dd v-if="certificateView.revoked">
                        <span v-if="certificateView.revokedSince">{{$d(Date.parse(certificateView.revokedSince), 'long') }}</span>
                    </dd>
                    <dt v-if="certificateView.revoked">
                        <span v-text="$t('ca3SApp.certificate.revocationReason')">Revocation Reason</span>
                    </dt>
                    <dd v-if="certificateView.revoked">
                        <span name="revocationReason">{{certificateView.revocationReason}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificate.fingerprint')">Fingerprint</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.fingerprint}}</span>
                    </dd>
                    <dt v-if="certificateView.csrId">
                        <span v-text="$t('ca3SApp.certificate.csr')">Csr</span>
                    </dt>
                    <dd v-if="certificateView.csrId">
                        <div>
                            <router-link :to="{name: 'CsrInfo', params: {cSRId: certificateView.csrId}}">{{certificateView.csrId}}</router-link>
                        </div>
                    </dd>

                    <dt v-if="certificateView.requestedBy">
                        <span v-text="$t('ca3SApp.certificate.requestedBy')">Requested by</span>
                    </dt>
                    <dd v-if="certificateView.requestedBy">
                        <span>{{certificateView.requestedBy}}</span>
                    </dd>

                    <dt v-if="certificateView.requestedOn">
                        <span v-text="$t('ca3SApp.certificate.requestedOn')">Requested on</span>
                    </dt>
                    <dd v-if="certificateView.requestedOn">
                        <span>{{certificateView.requestedOn}}</span>
                    </dd>


                    <dt v-if="certificateView.isServersideKeyGeneration">
                        <span v-text="$t('ca3SApp.certificate.download.PKCS12')">PKCS12 keystore</span>
                    </dt>
                    <dd v-if="certificateView.isServersideKeyGeneration">
                    	<div class="row">
                            <div class="col">
								<label class="form-control-label" v-text="$t('ca3SApp.certificate.download.p12Alias')" for="p12Alias">Alias</label>
                                <input type="text" class="form-check-inline" name="p12Alias" id="p12Alias" v-model="p12Alias" />
                            </div>

                            <div class="col colContent">
                                <a href="downloadUrl" @click.prevent="downloadKeystore('.p12', 'application/x-pkcs12')" >{{certificateView.downloadFilename}}.p12</a>
                            </div>
                        </div>
                    </dd>

                    <dt v-if="certificateView.subject">
                        <span v-text="$t('ca3SApp.certificate.download.pkix')">Pkix</span>
                    </dt>
                    <dd v-if="certificateView.subject">
                        <div>
                            <a href="downloadUrl" @click.prevent="downloadPKIX('.crt', 'application/pkix-cert')" >{{certificateView.downloadFilename}}.crt</a>
                        </div>
                    </dd>
                    <dt v-if="certificateView.subject">
                        <span v-text="$t('ca3SApp.certificate.download.pem')">PEM</span>
                    </dt>
                    <dd v-if="certificateView.subject">
                        <div>
                            <a href="downloadUrl" @click.prevent="downloadItem('.cer', 'application/pem-certificate')" >{{certificateView.downloadFilename}}.cer</a>
                        </div>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.certificate.csrBase64')">Certificate as PEM</span>
                    </dt>
                    <dd>
                        <span><textarea class="form-control pem-content" name="certContent" id="comment"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly
                            v-model="certificateView.certB64" /></span>

                    </dd>

                </dl>
            </div>
<!--
    certificateAdminData
-->
            <form name="editForm" role="form" novalidate>
                <div>
                    <div v-if="isRevocable()" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.revocationReason')" for="revocationReason">Revocation reason</label>
                        <select class="form-control" id="cert-revocationReason" name="revocationReason" v-model="certificateAdminData.revocationReason">
                            <option v-bind:value="'certificateHold'">certificateHold</option>
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

                    <button type="button" id="removeFromCRL" v-if="isRemovableFromCRL()" class="btn btn-secondary" v-on:click="removeCertificateFromCRL()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.removeCertificateFromCRL')">Remove from CRL</span>
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
