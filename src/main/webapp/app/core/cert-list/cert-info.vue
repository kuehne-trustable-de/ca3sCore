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

                    <dt v-if="!certificateView.selfsigned && certificateView.root">
                        <span v-text="$t('ca3SApp.certificate.root')">Root</span>
                    </dt>
                    <dd v-if="!certificateView.selfsigned && certificateView.root">
                        <span >{{certificateView.root}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.certificate.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.type}}</span>
                        <span v-if="certificateView.ca"><b>CA</b></span>
                        <span v-if="certificateView.selfsigned"><b>Selfsigned</b></span>

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
                        <span v-text="$t('ca3SApp.certificate.keyDetails')">Key Details</span>
                    </dt>
                    <dd>
                        <span>{{certificateView.keyAlgorithm}} / {{certificateView.keyLength}} bits</span>
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
                        <span>{{certificateView.signingAlgorithm}} / {{certificateView.hashAlgorithm}} / {{certificateView.paddingAlgorithm}}</span>
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

                    <dt v-if="certificateView.crlUrl">
                        <span v-text="$t('ca3SApp.certificate.crlUrl')">CRL Uri</span>
                    </dt>
                    <dd v-if="certificateView.crlUrl">
                        <span name="crlUrl">{{certificateView.crlUrl}}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.certificate.fingerprint')">Fingerprint</span>
                    </dt>
                    <dd>
                        <ul>
                            <li>
                                <span>{{certificateView.fingerprintSha1}}</span>
                            </li>
                            <li>
                                <span>{{certificateView.fingerprintSha256}}</span>
                            </li>
                        </ul>
                    </dd>
                    <dt v-if="certificateView.csrId">
                        <span v-text="$t('ca3SApp.certificate.csr')">Csr</span>
                    </dt>
                    <dd v-if="certificateView.csrId">
                        <div>
                            <router-link :to="{name: 'CsrInfo', params: {csrId: certificateView.csrId}}">{{certificateView.csrId}}</router-link>
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

                    <Fragment v-for="attr in certificateView.arArr" :key="attr.name" v-if="!isEditable()">
                        <dt>
                            <span >{{attr.name}}</span>
                        </dt>
                        <dd >
                            <span >{{attr.value}}</span>
                        </dd>
                    </Fragment>


                    <Fragment  v-if="certificateView.auditPresent">
                        <dt>
                            <span v-text="$t('ca3SApp.certificate.audit')">Audit</span>
                        </dt>
                        <dd>
                            <audit-tag :certificateId="certificateView.id" :csrId="certificateView.csrId" showLinks="false"></audit-tag>
                        </dd>
                    </Fragment>

                    <!-- donwload section -->
                    <dt v-if="certificateView.isServersideKeyGeneration">
                        <span v-text="$t('ca3SApp.certificate.download.PKCS12')">PKCS12 keystore</span> <help-tag target="ca3SApp.certificate.download.PKCS12"/>
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
                        <span v-text="$t('ca3SApp.certificate.download.pkix')">Pkix</span> <help-tag target="ca3SApp.certificate.download.PKIX"/>
                    </dt>
                    <dd v-if="certificateView.subject">
                        <div>
                            <a href="downloadUrl" @click.prevent="downloadPKIX('.crt', 'application/pkix-cert')" >{{certificateView.downloadFilename}}.crt</a>
                        </div>
                    </dd>
                    <dt v-if="certificateView.subject">
                        <span v-text="$t('ca3SApp.certificate.download.pem')">PEM</span> <help-tag target="ca3SApp.certificate.download.PEM"/>
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
                        <div class="form-group wrap">
                            <textarea class="form-control pem-content" name="certContent" id="certContent"
                                      autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly
                                      v-model="certificateView.certB64" />

                            <CopyClipboardButton contentElementId="certContent"/>
                        </div>

                    </dd>

                </dl>
            </div>
<!--
    certificateAdminData
-->
            <form name="editForm" role="form" novalidate>
                <div>

                    <Fragment v-if="isEditable()">
                        <div v-for="attr in certificateView.arArr" :key="attr.name" class="form-group">
                            <label class="form-control-label"  :for="'cert-ar-'+attr.name">{{attr.name}}</label>
                            <input type="text" class="form-control" name="rejectionReason" :id="'cert-ar-'+attr.name" v-model="attr.value" />
                        </div>
                    </Fragment>

                    <div v-if="isRevocable()" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.revocationReason')" for="cert-revocationReason">Revocation reason</label> <help-tag target="ca3SApp.certificate.download.revocationReason"/>
                        <select class="form-control" id="cert-revocationReason" name="revocationReason"  v-model="certificateAdminData.revocationReason">
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
                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.comment')" for="comment">Comment</label> <help-tag target="ca3SApp.certificate.comment"/>
                        <textarea class="form-control" name="content" id="comment"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="certificateAdminData.comment" />
                    </div>


                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                    </button>

                    <button type="button" id="edit" v-if="isOwnCertificate()" class="btn btn-secondary" v-on:click="selfAdministerCertificate()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')">Update</span>
                    </button>

                    <button type="button" id="update" v-if="isRAOfficer()" class="btn btn-secondary" v-on:click="updateCertificate()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')">Update</span>
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

<style scoped>
</style>

<script lang="ts" src="./cert-info.component.ts">
</script>
