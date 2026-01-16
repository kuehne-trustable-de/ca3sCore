<template>
    <div>
        <div class="row justify-content-center">
            <div class="col-8">
                <div v-if="certificateView">
                    <h2 class="jh-entity-heading" id="certificateHeader"><span v-text="$t('ca3SApp.certificate.detail.title')"></span> {{certificateView.id}}</h2>
                    <dl class="row jh-entity-details">
                        <dt>
                            <span v-text="$t('ca3SApp.certificate.subject')"></span>
                        </dt>
                        <dd :style="getRevocationStyle(certificateView.revoked)">
                            <span>{{certificateView.subject}}</span>
                        </dd>

                        <dt v-if="certificateView.revoked">
                            <span v-text="$t('ca3SApp.certificate.revokedSince')"></span>
                        </dt>
                        <dd v-if="certificateView.revoked">
                            <span v-if="certificateView.revokedSince">{{$d(Date.parse(certificateView.revokedSince), 'long') }}</span>
                        </dd>
                        <dt v-if="certificateView.revoked">
                            <span v-text="$t('ca3SApp.certificate.revocationReason')"></span>
                        </dt>
                        <dd v-if="certificateView.revoked">
                            <span name="revocationReason">{{certificateView.revocationReason}}</span>
                        </dd>

                        <dt v-if="certificateView.sanArr && certificateView.sanArr.length > 0">
                            <span v-text="$t('ca3SApp.certificate.sans')"></span>
                        </dt>
                        <dd v-if="certificateView.sanArr && certificateView.sanArr.length > 0">
                            <ul>
                                <li v-for="san in certificateView.sanArr" :key="san" >{{san}}</li>
                            </ul>
                        </dd>

                        <dt>
                            <span v-text="$t('ca3SApp.certificate.issuer')"></span>
                        </dt>
                        <dd>
                            <span v-if="certificateView.selfsigned" v-text="$t('ca3SApp.certificate.selfsigned')"></span>
                            <a v-else-if="certificateView.issuerId" href="issuer" @click.prevent="retrieveCertificate(certificateView.issuerId)">{{certificateView.issuer}}</a>
                            <span v-else>{{certificateView.issuer}}</span>

                        </dd>

                        <dt v-if="!certificateView.selfsigned && certificateView.root">
                            <span v-text="$t('ca3SApp.certificate.root')"></span>
                        </dt>
                        <dd v-if="!certificateView.selfsigned && certificateView.root">
                            <a v-if="certificateView.rootId" href="root" @click.prevent="retrieveCertificate(certificateView.rootId)">{{certificateView.root}}</a>
                            <span v-else>{{certificateView.root}}</span>
                        </dd>

                        <dt>
                            <span v-text="$t('ca3SApp.certificate.type')"></span>
                        </dt>
                        <dd>
                            <span>{{certificateView.type}}</span>
                            <span v-if="certificateView.ca">, <b>CA</b></span>
                            <span v-if="certificateView.selfsigned">, <b>Selfsigned</b></span>
                            <span v-if="certificateView.trusted">, <b>Trusted</b></span>
                            <span v-if="certificateView.issuingActiveCertificates" v-text="$t('ca3SApp.certificate.active.issued')"></span>
                        </dd>

                        <dt>
                            <span v-text="$t('ca3SApp.certificate.serial')"></span>
                        </dt>
                        <dd>
                            <ul>
                                <li >{{certificateView.serialHex}} ({{$t('HEX')}})</li>
                                <li v-if="certificateView.serialHex != certificateView.serial">{{certificateView.serial}} ({{$t('DECIMAL')}})</li>
                            </ul>
                        </dd>
                        <dt>
                            <span v-text="$t('ca3SApp.certificate.validFrom')"></span>
                        </dt>
                        <dd>
                            <span v-if="certificateView.validFrom">{{$d(Date.parse(certificateView.validFrom), 'long') }}</span>
                        </dd>
                        <dt>
                            <span v-text="$t('ca3SApp.certificate.validTo')"></span>
                        </dt>
                        <dd>
                            <span v-if="certificateView.validTo">{{$d(Date.parse(certificateView.validTo), 'long') }}</span>
                        </dd>

                         <dt>
                            <span v-text="$t('ca3SApp.certificate.keyDetails')"></span>
                        </dt>
                        <dd>
                            <span>{{certificateView.keyAlgorithm}}</span>
                            <span v-if="certificateView.curveName && certificateView.curveName.length > 0">{{certificateView.curveName}}</span>
                            <span v-if="certificateView.keyLength && certificateView.keyLength.length > 0 && certificateView.keyLength !== '-1'"> / {{certificateView.keyLength}} bits</span>
                        </dd>

                        <dt v-if="certificateView.altKeyAlgorithm">
                            <span v-text="$t('ca3SApp.certificate.altKeyDetails')"></span>
                        </dt>
                        <dd v-if="certificateView.altKeyAlgorithm">
                            <span>{{certificateView.altKeyAlgorithm}}</span>
                            <span v-if="certificateView.altCurveName && certificateView.altCurveName.length > 0">{{certificateView.altCurveName}}</span>
                            <span v-if="certificateView.altKeyLength && certificateView.altKeyLength.length > 0 && certificateView.altKeyLength !== '-1'"> / {{certificateView.keyLength}} bits</span>
                        </dd>

                        <dt>
                            <span v-text="$t('ca3SApp.certificate.signingAlgorithm')"></span>
                        </dt>
                        <dd>
                            <span>{{certificateView.signingAlgorithm}}</span>
                            <span v-if="certificateView.hashAlgorithm && certificateView.hashAlgorithm.length > 0"> / {{certificateView.hashAlgorithm}}</span>
                            <span v-if="certificateView.paddingAlgorithm && certificateView.paddingAlgorithm.length > 0"> / {{certificateView.paddingAlgorithm}}</span>
                        </dd>

                        <dt>
                            <span v-text="$t('ca3SApp.certificate.usage')"></span>
                        </dt>
                        <dd>
                            <ul v-if="certificateView.usage && certificateView.usage.length > 0">
                                <li v-for="usg in certificateView.usage" :key="usg" >{{usg}}</li>
                            </ul>
                        </dd>

                        <dt v-if="certificateView.extUsage && certificateView.extUsage.length > 0">
                            <span v-text="$t('ca3SApp.certificate.extended.usage')"></span>
                        </dt>
                        <dd v-if="certificateView.extUsage && certificateView.extUsage.length > 0">
                            <ul>
                                <li v-for="extUsage in certificateView.extUsage" :key="extUsage" >{{extUsage}}</li>
                            </ul>
                        </dd>

                        <dt>
                            <span v-text="$t('ca3SApp.certificate.contentAddedAt')"></span>
                        </dt>
                        <dd>
                            <span v-if="certificateView.contentAddedAt">{{$d(Date.parse(certificateView.contentAddedAt), 'long') }}</span>
                        </dd>

                        <dt v-if="certificateView.crlUrl">
                            <span v-text="$t('ca3SApp.certificate.crlUrl')"></span>
                        </dt>
                        <dd v-if="certificateView.crlUrl">
                            <span name="crlUrl">{{certificateView.crlUrl}}</span>

                            <!--a href="crlExpiryNotification" id="crlExpiryNotification"
                               @click.prevent="crlExpiryNotification(certificateView.id)" >#########</a-->

                        </dd>

                        <dt>
                            <span v-text="$t('ca3SApp.certificate.fingerprint')"></span>
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
                            <span v-text="$t('ca3SApp.certificate.csr')"></span>
                        </dt>
                        <dd v-if="certificateView.csrId">
                            <div>
                                <router-link :to="{name: 'CsrInfo', params: {csrId: certificateView.csrId}}">{{certificateView.csrId}}</router-link>
                            </div>
                        </dd>

                        <dt v-if="isRAOrAdmin() && (certificateView.acmeAccountId || certificateView.acmeOrderId)">
                            <span v-text="$t('ca3SApp.certificate.acme')"></span>
                        </dt>
                        <dd v-if="isRAOrAdmin() && (certificateView.acmeAccountId || certificateView.acmeOrderId)">
                            <div>
                                <router-link v-if="certificateView.acmeAccountId"
                                             :to="{name: 'AcmeAccountInfo', params: {accountId: certificateView.acmeAccountId}}">Account</router-link>
                                <router-link v-if="certificateView.acmeOrderId"
                                             :to="{name: 'AcmeOrderInfo', params: {orderId: certificateView.acmeOrderId}}">Order</router-link>
                            </div>
                        </dd>

                        <dt v-if="certificateView.requestedBy">
                            <span v-text="$t('ca3SApp.certificate.requestedBy')"></span>
                        </dt>
                        <dd v-if="certificateView.requestedBy">

                            <span v-if="(certificateView.firstName || certificateView.lastName) && certificateView.email"><a :href="'mailto:' + certificateView.email">{{$t('ca3SApp.cSR.requestor.details', {fistName: certificateView.firstName, lastName: certificateView.lastName})}}</a></span>
                            <span v-if="(certificateView.firstName || certificateView.lastName) && !certificateView.email">{{$t('ca3SApp.cSR.requestor.details', {fistName: certificateView.firstName, lastName: certificateView.lastName})}}</span>
                            <span v-if="certificateView.tenantName">{{$t('ca3SApp.cSR.requestor.tenant', {tenant: certificateView.tenantName})}}</span>

                            <span>{{certificateView.requestedBy}}</span>
                            <span v-if="certificateView.tenantName"> / {{certificateView.tenantName}}</span>
                        </dd>

                        <dt v-if="certificateView.requestedOn">
                            <span v-text="$t('ca3SApp.certificate.requestedOn')"></span>
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

                        <dt v-if="certificateView.isServersideKeyGeneration && isEditable()">
                            <span v-text="$t('ca3SApp.certificate.download.PKCS12')"></span> <help-tag target="ca3SApp.certificate.download.PKCS12"/>
                        </dt>

                        <dd v-if="certificateView.isServersideKeyGeneration && isEditable() && isPrivateKeyAvailable()">
                            <div class="row">
                                <div class="col">
                                    <label class="form-control-label" v-text="$t('ca3SApp.certificate.download.p12Alias')" for="p12Alias"></label>
                                    <input type="text" class="form-check-inline" name="p12Alias" id="p12Alias" v-model="p12Alias" />
                                    <small class="form-text text-danger" v-if="showRegExpFieldWarning(p12Alias, '^[a-zA-Z0-9_.-]{5,100}$')" v-text="$t('ca3SApp.messages.password.requirement.min5NumberOrChars')"></small>

                                </div>
                                <div class="col colContent" v-if="!showRegExpFieldWarning(p12Alias, '^[a-zA-Z0-9_.-]{5,100}$')">
                                    <a href="downloadUrl" id="pkcs12-download"
                                       @click.prevent="downloadKeystore('.p12', 'application/x-pkcs12')" >{{certificateView.downloadFilename}}.p12</a>
                                </div>
                            </div>

                            <div class="row">
                                <div v-if="certificateView.serversideKeyValidTo && (certificateView.serversideKeyValidTo < 9999)" class="col">
                                    <label class="form-control-label" v-text="$t('ca3SApp.certificate.key.availableUntil')" for="availableUntil"></label>
                                    <span name="availableUntil">{{$d(Date.parse(certificateView.serversideKeyValidTo), 'short') }}</span>
                                </div>
                                <!--div class="col">
                                    <label class="form-control-label" v-text="$t('ca3SApp.certificate.key.leftUsages')" for="leftUsages">Key left usages</label>
                                    <span>{{certificateView.serversideKeyLeftUsages}}</span>
                                </div-->
                            </div>

                            <div class="row" v-if="getP12PbeAlgoArr().length > 1">
                                <div class="col">

                                    <button type="button"
                                        v-if="collapsed"
                                        class="addRemoveSelector" v-on:click="setCollapsed(false)">
                                            <font-awesome-icon icon="plus"></font-awesome-icon>
                                    </button>

                                    <button type="button"
                                            v-if="!collapsed"
                                            class="addRemoveSelector" v-on:click="setCollapsed(true)">
                                        <font-awesome-icon icon="minus"></font-awesome-icon>
                                    </button>

                                    <label
                                        v-if="!collapsed"
                                        class="form-control-label" v-text="$t('ca3SApp.certificate.download.p12pbe')" for="p12Pbe"></label>

                                    <select
                                        v-if="!collapsed"
                                        class="form-control" id="p12Pbe" name="p12Pbe" v-model="p12Pbe" >
                                        <option v-for="algo in getP12PbeAlgoArr()" :key="algo" :value="algo" >{{algo}}</option>
                                    </select>

                                    <!--label class="form-control-label" v-text="$t('ca3SApp.certificate.p12keyex')" for="p12KeyEx">KeyEx</label>
                                    <input type="checkbox" class="form-check-inline" name="p12KeyEx" id="p12KeyEx" v-model="p12KeyEx" /-->
                                </div>
                            </div>
                        </dd>

                        <dd v-if="certificateView.isServersideKeyGeneration && isEditable() && !isPrivateKeyAvailable()">
                            <span v-text="$t('ca3SApp.certificate.download.PKCS12.key.expired')"></span>
                        </dd>

                        <dt v-if="certificateView.downloadFilename">
                            <span v-text="$t('ca3SApp.certificate.download.pkix')"></span> <help-tag target="ca3SApp.certificate.download.PKIX"/>
                        </dt>
                        <dd v-if="certificateView.downloadFilename">
                            <div class="container">
                                <div class="row" >
                                    <div class="col">
                                        <select class="form-control" id="download-format" name="download-format"
                                                v-model="downloadFormat">
                                            <option value="pkix" v-text="$t('ca3SApp.certificate.download.PKIX')" selected="selected"></option>
                                            <option value="pem" v-text="$t('ca3SApp.certificate.download.PEM')" ></option>
                                            <option v-if="certificateView.endEntity && (certificateView.issuerId !== undefined)" value="pemPart" v-text="$t('ca3SApp.certificate.download.pemPartChain')" ></option>
                                            <option v-if="certificateView.endEntity && (certificateView.issuerId !== undefined)" value="pemFull" v-text="$t('ca3SApp.certificate.download.pemFullChain')" ></option>
                                        </select>
                                    </div>
                                    <div class="col">
                                        <a href="downloadUrl" id="certificate-download" @click.prevent="downloadItem()" >{{getDownloadFilename()}}</a>
                                    </div>
                                </div>
                            </div>
                        </dd>
                    </dl>
                    <div v-if="certificateView.auditPresent">
                        <audit-tag :certificateId="certificateView.id" :csrId="certificateView.csrId" showLinks="false" :title="$t('ca3SApp.certificate.audit')"></audit-tag>
                    </div>
                </div>

                <!--
        certificateAdminData
    -->
                <form name="editForm" role="form" novalidate>
                    <div>
                        <div v-if="isTrustable()" class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.pipeline.trusted')" for="certificate-trusted"></label>
                            <input type="checkbox" class="form-check-inline" name="trusted" id="certificate-trusted" v-model="trusted" />
                        </div>

                        <div v-if="isEditable()" v-for="attr in certificateAdminData.arAttributes" :key="attr.name" class="form-group">
                            <label class="form-control-label"  :for="'cert-ar-'+attr.name">{{attr.name}}</label>
                            <input type="text" class="form-control" :name="'cert-ar-'+attr.name" :id="'cert-ar-'+attr.name" v-model="attr.value" />
                        </div>

                        <div v-if="isRevocable()" class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.notification.blocked')" for="certificate-notification-blocked"></label>
                            <input type="checkbox" class="form-check-inline" name="trusted" id="certificate-notification-blocked" v-model="certificateView.notificationBlocked"
                                   v-b-modal.blockNotificationForCertificate/>
                        </div>

                        <div v-if="isRevocable()" class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.certificate.revocationReason')" for="cert-revocationReason"></label> <help-tag target="ca3SApp.certificate.download.revocationReason"/>
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

                        <div class="form-group">
                            <label class="form-control-label" v-text="$t('ca3SApp.certificate.comment')" for="comment"></label> <help-tag target="ca3SApp.certificate.comment"/>

                            <textarea v-if="isRAOrAdmin() || isOwnCertificate()" class="form-control" name="content" id="comment"
                                      autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                      v-model="comment" />
                            <textarea v-else class="form-control" name="content" id="comment"
                                      autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                      readonly="true"
                                      v-model="comment" />
                        </div>

                        <b-alert :show="dismissCountDown"
                                 dismissible
                                 :variant="alertType"
                                 @dismissed="dismissCountDown=0"
                                 @dismiss-count-down="countDownChanged">
                            {{alertMessage}}
                        </b-alert>

                        <button type="submit"
                                v-on:click.prevent="previousState()"
                                class="btn btn-info">
                            <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"></span>
                        </button>

                        <button type="button" id="edit" v-if="isOwnCertificate() && (!isRAOrAdmin())"
                                class="btn btn-secondary" v-on:click="selfAdministerCertificate()">
                            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.update')"></span>
                        </button>

                        <button type="button" id="update" v-if="isRAOrAdmin() && isValuesChanged()"
                                class="btn btn-secondary" v-on:click="updateCertificate()">
                            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.update')"></span>
                        </button>

                        <button type="button" id="updateCrl" v-if="isRAOfficer()"
                                class="btn btn-secondary" v-on:click="updateCRL()">
                            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.updateCrl')"></span>
                        </button>

                        <button type="button" id="removeFromCRL" v-if="isRemovableFromCRL()"
                                class="btn btn-secondary" v-on:click="removeCertificateFromCRL()">
                            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.removeCertificateFromCRL')"></span>
                        </button>

                        <!--button type="button" id="revoke" v-if="isRAOfficer() && !isOwnCertificate() && isRevocable()" class="btn btn-secondary" v-on:click="revokeCertificate()">
                            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.revoke')">Revoke</span>
                        </button>

                        <button type="button" id="withdraw" v-if="isOwnCertificate() && isRevocable()" class="btn btn-secondary" v-on:click="withdrawCertificate()">
                            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.withdraw')">Withdraw</span>
                        </button-->

                        <b-button v-if="(isRAOfficer() || isOwnCertificate()) && isRevocable()"
                                  variant="danger"
                                  class="btn"
                                  id="revoke"
                                  v-b-modal.revokeCertificate>
                            <font-awesome-icon icon="times"></font-awesome-icon>
                            <span class="d-none d-md-inline" v-text="$t('entity.action.revoke')"></span>
                        </b-button>

                    </div>
                </form>

            </div>
        </div>

        <b-modal ref="revokeCertificate" id="revokeCertificate" >
            <span slot="modal-title"><span id="ca3SApp.certificate.revoke.question" v-text="$t('entity.revoke.title')"></span></span>
            <div class="modal-body">

                <div v-if="certificateView.ca" class="alert alert-warning" role="alert">
                    <p v-text="$t('ca3SApp.certificate.ca.hint')"></p>
                </div>
                <div v-if="certificateView.issuingActiveCertificates" class="alert alert-danger" role="alert">
                    <p v-text="$t('ca3SApp.certificate.no.revocation.active.issued')"></p>
                </div>
                <p id="jhi-revoke-certificate-heading" v-text="$t('ca3SApp.certificate.revoke.question', {'id': certificateView.id})"></p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()"></button>
                <button v-if="!certificateView.issuingActiveCertificates"
                        type="button" class="btn btn-primary" id="confirm-revoke-certificate" v-text="$t('entity.action.revoke')" v-on:click="revokeCertificateAndClose()"></button>
            </div>
        </b-modal>

        <b-modal ref="blockNotificationForCertificate" id="blockNotificationForCertificate" >
            <span slot="modal-title"><span id="ca3SApp.certificate.block.notification.question" v-text="$t('entity.block.notification.title')"></span></span>
            <div class="modal-body">

                <div v-if="certificateView.notificationBlocked" class="alert alert-warning" role="alert">
                    <p v-text="$t('ca3SApp.certificate.block.notification.hint')"></p>
                </div>
                <p v-if="certificateView.notificationBlocked" id="jhi-revoke-certificate-heading" v-text="$t('ca3SApp.certificate.block.notification.question', {'id': certificateView.id})"></p>

                <p v-if="!certificateView.notificationBlocked" id="jhi-revoke-certificate-heading" v-text="$t('ca3SApp.certificate.unblock.notification.question', {'id': certificateView.id})"></p>

            </div>
            <div v-if="certificateView.notificationBlocked" slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="certificateView.notificationBlocked = false;certificateAdminData.notificationBlocked = false;closeDialogNotificationBlock()"></button>
                <button type="button" class="btn btn-primary" id="confirm-block-notification-certificate" v-text="$t('entity.action.block.notification')" v-on:click="certificateView.notificationBlocked = true;certificateAdminData.notificationBlocked = true;closeDialogNotificationBlock()"></button>
            </div>
            <div v-if="!certificateView.notificationBlocked" slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="certificateView.notificationBlocked = true;certificateAdminData.notificationBlocked = true;closeDialogNotificationBlock()"></button>
                <button type="button" class="btn btn-primary" id="confirm-unblock-notification-certificate" v-text="$t('entity.action.unblock.notification')" v-on:click="certificateView.notificationBlocked = false;certificateAdminData.notificationBlocked = false;closeDialogNotificationBlock()"></button>
            </div>
        </b-modal>

    </div>
</template>

<style scoped>
</style>

<script lang="ts" src="./cert-info.component.ts">
</script>
