<template>
    <div>
        <b-alert :show="dismissCountDown"
                 dismissible
                 :variant="alertType"
                 @dismissed="dismissCountDown=0"
                 @dismiss-count-down="countDownChanged">
            {{alertMessage}}
        </b-alert>
        <div class="row justify-content-center">
            <div class="col-8">
                <div v-if="certificateView">
                    <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.certificate.detail.title')">Certificate</span> {{certificateView.id}}</h2>
                    <dl class="row jh-entity-details">
                        <dt>
                            <span v-text="$t('ca3SApp.certificate.subject')">Subject</span>
                        </dt>
                        <dd :style="getRevocationStyle(certificateView.revoked)">
                            <span>{{certificateView.subject}}</span>
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
                            <a v-else-if="certificateView.issuerId" href="issuer" @click.prevent="retrieveCertificate(certificateView.issuerId)">{{certificateView.issuer}}</a>
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
                            <span v-if="certificateView.trusted"><b>Trusted</b></span>

                        </dd>
                        <dt>
                            <span v-text="$t('ca3SApp.certificate.serial')">Serial</span>
                        </dt>
                        <dd>
                            <ul>
                                <li >{{certificateView.serialHex}}</li>
                                <li >{{certificateView.serial}}</li>
                            </ul>

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

                            <div class="row" v-if="getP12PbeAlgoArr().length > 1">
                                <div class="col">

                                    <Fragment v-if="collapsed ">
                                        <button type="button" class="addRemoveSelector" v-on:click="setCollapsed(false)">
                                            <font-awesome-icon icon="plus"></font-awesome-icon>
                                    </button>
                                    </Fragment>

                                    <Fragment v-if="!collapsed ">
                                        <button type="button" class="addRemoveSelector" v-on:click="setCollapsed(true)">
                                            <font-awesome-icon icon="minus"></font-awesome-icon>
                                        </button>

                                        <label class="form-control-label" v-text="$t('ca3SApp.certificate.download.p12pbe')" for="p12Pbe">PBE</label>
                                        <select class="form-control" id="p12Pbe" name="p12Pbe" v-model="p12Pbe" >
                                            <option v-for="algo in getP12PbeAlgoArr()" :key="algo" :value="algo" >{{algo}}</option>
                                        </select>

                                        <!--label class="form-control-label" v-text="$t('ca3SApp.certificate.p12keyex')" for="p12KeyEx">KeyEx</label>
                                        <input type="checkbox" class="form-check-inline" name="p12KeyEx" id="p12KeyEx" v-model="p12KeyEx" /-->
                                    </Fragment>
                                </div>
                            </div>
                        </dd>


                        <dt v-if="certificateView.downloadFilename">
                            <span v-text="$t('ca3SApp.certificate.download.pkix')">Pkix</span> <help-tag target="ca3SApp.certificate.download.PKIX"/>
                        </dt>
                        <dd v-if="certificateView.downloadFilename">
                            <div class="container">
                                <div class="row" >
                                    <div class="col">
                                        <select class="form-control" id="download-format" name="download-format" v-model="downloadFormat" >
                                            <option value="pkix" v-text="$t('ca3SApp.certificate.download.PKIX')" selected="selected">PKIX</option>
                                            <option value="pem" v-text="$t('ca3SApp.certificate.download.PEM')" >PEM</option>
                                            <option v-if="certificateView.endEntity && (certificateView.issuerId !== undefined)" value="pemPart" v-text="$t('ca3SApp.certificate.download.pemPartChain')" >PEMPartChain</option>
                                            <option v-if="certificateView.endEntity && (certificateView.issuerId !== undefined)" value="pemFull" v-text="$t('ca3SApp.certificate.download.pemFullChain')" >PEMFullChain</option>
                                        </select>
                                    </div>
                                    <div class="col">
                                        <a href="downloadUrl" @click.prevent="downloadItem()" >{{getDownloadFilename()}}</a>
                                    </div>
                                </div>
                            </div>
                        </dd>
                    </dl>
                    <Fragment  v-if="certificateView.auditPresent">
                        <div>
                            <audit-tag :certificateId="certificateView.id" :csrId="certificateView.csrId" showLinks="false" :title="$t('ca3SApp.certificate.audit')"></audit-tag>
                        </div>
                    </Fragment>
                </div>

                <!--
        certificateAdminData
    -->
                <form name="editForm" role="form" novalidate>
                    <div>
                        <div v-if="isTrustable() && isRAOfficer()" class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.pipeline.trusted')" for="certificate-trusted">Trusted</label>
                            <input type="checkbox" class="form-check-inline" name="trusted" id="certificate-trusted"
                                   v-model="certificateAdminData.trusted" />
                        </div>

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
                            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.update')">Update</span>
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
    </div>
</template>

<style scoped>
</style>

<script lang="ts" src="./cert-info.component.ts">
</script>
