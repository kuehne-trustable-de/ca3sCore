<template>
    <div>
        <b-alert :show="dismissCountDown"
                 dismissible
                 :variant="alertType"
                 @dismissed="dismissCountDown=0"
                 @dismiss-count-down="countDownChanged">
            {{alertMessage}}
        </b-alert>
        <br/>
        <div class="row justify-content-center">
            <div class="col-md-8 toastify-container">
                <h2 v-if="account" id="password-title"><span v-html="$t('password.title', { 'username': username})"></span>
                </h2>

                <h3 v-if="credentialChange.credentialUpdateType === 'CLIENT_CERT'" id="client-cert-title"><span
                    v-text="$t('global.messages.client.cert.title')"></span></h3>
                <h3 v-if="credentialChange.credentialUpdateType === 'TOTP'" id="totp-title"><span
                    v-text="$t('global.messages.totp.title')"></span></h3>
                <h3 v-if="credentialChange.credentialUpdateType === 'SMS'" id="sms-title"><span
                    v-text="$t('global.messages.totp.title')"></span></h3>

                <div class="alert alert-danger" role="alert" v-if="oldPasswordMismatch" v-text="$t('global.messages.error.oldPasswordMismatch')">
                    The password and its confirmation do not match!
                </div>

                <form name="form" role="form" id="password-form" v-on:submit.prevent="saveCredentials()">

                    <div class="row justify-content-start">
                        <div class="col-sm">
                            <label class="form-control-label" for="currentPassword"
                                   v-text="$t('global.form.currentpassword.label')"></label>
                        </div>
                        <div class="col-6">

                            <input type="password"
                                   class="form-control form-check-inline w-50"
                                   id="currentPassword" name="currentPassword"
                                   :class="{'valid': !$v.resetPassword.currentPassword.$invalid, 'invalid': $v.resetPassword.currentPassword.$invalid }"
                                   v-bind:placeholder="$t('global.form.currentpassword.placeholder')"
                                   v-model="$v.resetPassword.currentPassword.$model"
                                   required>
                            <small v-if="showRequiredWarning(true, $v.resetPassword.currentPassword.$model)"
                                   class="form-text text-danger" v-text="$t('entity.validation.required')"></small>
                        </div>
                        <div class="col">
                        </div>
                    </div>
                    <div class="row justify-content-start" v-if="credentialChange.credentialUpdateType === 'CLIENT_CERT'">
                        <div class="col-sm">
                            <label class="form-control-label" for="client-auth-secret"
                                   v-text="$t('global.form.client.cert')"></label>
                        </div>
                        <div class="col-6">
                            <input type="password"
                                   class="form-control form-check-inline w-50"
                                   :class="(showRequiredWarning(true, $v.clientAuthSecret.$model) ? 'invalid' : ' valid')"
                                   name="client-auth-secret" id="client-auth-secret"
                                   autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                   required
                                   v-bind:placeholder="$t('global.form.keystorepassword.placeholder')"
                                   v-model="$v.clientAuthSecret.$model"/>
                            <small v-if="showRequiredWarning(true, $v.clientAuthSecret.$model)"
                                   class="form-text text-danger"
                                   v-text="$t('entity.validation.required')"></small>
                            <small class="form-text text-danger"
                                   v-if="showRegExpFieldWarning($v.clientAuthSecret.$model, regExpSecret())"
                                   v-text="$t('ca3SApp.messages.password.requirement.' + regExpSecretDescription())"></small>

                        </div>
                        <div class="col">
                            <button :disabled="!canCreateCertificate()"
                                    class="btn btn-primary"
                                    @click.prevent="downloadPersonalCertificate($v.clientAuthSecret.$model)"
                                    v-text="$t('ca3SApp.form.create.client.certificate')">
                                <font-awesome-icon icon="plus"></font-awesome-icon>
                            </button>
                        </div>
                    </div>

                    <!--div class="row"
                         v-if="!showRegExpFieldWarning($v.clientAuthSecret.$model, regExpSecret())" >
                        <div class="col">
                            <label class="form-control-label"
                                   v-text="$t('ca3SApp.form.client.cert.download')"
                                   for="personal-certificate-download">
                            </label>
                        </div>
                        <div class="col colContent">
                            <button :disabled="!canCreateCertificate()"
                                    class="btn btn-primary"
                                    @click.prevent="downloadPersonalCertificate($v.clientAuthSecret.$model)"
                                    v-text="$t('ca3SApp.form.create.client.certificate')">
                                <font-awesome-icon icon="plus"></font-awesome-icon>
                            </button>
                        </div>
                    </div-->

                    <div class="row justify-content-start" v-if="credentialChange.clientAuthCertId !== 0">
                        <div class="col-sm">
                            <label class="form-control-label"
                                   v-text="$t('ca3SApp.form.client.cert.install')"
                                   for="jhi-test-clientAuthCert">
                            </label>
                        </div>
                        <div class="col-6"></div>
                        <div class="col"></div>
                    </div>

                    <div class="row justify-content-start" v-if="credentialChange.credentialUpdateType === 'TOTP'">
                        <div class="col-sm">
                            <label class="form-control-label"
                                   v-text="$t('ca3SApp.form.client.otp.useGivenSeed')"
                                   for="otp-useGivenSeed"></label>
                        </div>
                        <div class="col-6">
                            <input type="checkbox" class="form-check-inline" name="otp-useGivenSeed"
                                   id="otp-useGivenSeed"
                                   v-on:change="updateSeedMode()"
                                   v-model="useGivenSeed"/>
                        </div>
                        <div class="col"></div>
                    </div>

                    <div class="row justify-content-start" v-if="credentialChange.credentialUpdateType === 'TOTP'">
                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.otp.seed')"
                                   for="otp-seed"></label>
                        </div>
                        <div class="col-6">
                            <input type="text"
                                   class="form-control form-check-inline w-50"
                                   :disabled="!useGivenSeed"
                                   name="otp-seed" id="otp-seed"
                                   autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                   v-model="credentialChange.seed"/>

                            <small class="form-text text-danger"
                                   v-if="showBase32RegExpFieldWarning(credentialChange.seed)"
                                   v-text="$t('ca3SApp.messages.seed.base32Expected')">
                            </small>
                        </div>
                        <div class="col"></div>
                    </div>

                    <div class="row justify-content-start" v-if="credentialChange.credentialUpdateType === 'TOTP'  && !useGivenSeed">
                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.otp.qrcode')"
                                   for="request-otp-qrcode"></label>
                        </div>
                        <div class="col-6">
                            <img :src="qrCodeImgUrl"/>
                        </div>
                        <div class="col"></div>
                    </div>

                    <div class="row justify-content-start" v-if="credentialChange.credentialUpdateType === 'TOTP'">
                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.otp.value')"
                                   for="otp-test-value"></label>
                        </div>
                        <div class="col-6">
                            <input type="text" class="form-check-inline" name="otp-test-value"
                                   id="otp-test-value"
                                   autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                   required
                                   :class="(showTOTPExpFieldWarning(credentialChange.otpTestValue) ? 'invalid' : ' valid')"
                                   v-model="credentialChange.otpTestValue"/>
                            <small class="form-text text-danger"
                                   v-if="showTOTPExpFieldWarning(credentialChange.otpTestValue)"
                                   v-text="$t('ca3SApp.messages.sixNumbersExpected')">
                            </small>
                        </div>
                        <div class="col"></div>
                    </div>

                    <div class="row justify-content-start" v-if="credentialChange.credentialUpdateType === 'SMS'">
                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.sms.test.label')"
                                   for="sms-test-request"></label>
                        </div>
                        <div class="col-6">
                            <button type="submit" class="btn btn-primary"
                                    v-text="$t('ca3SApp.form.client.credentials.sms.test')"
                                    @click.prevent="sendSMS()">
                            </button>
                        </div>
                        <div class="col"></div>
                    </div>

                    <div class="row justify-content-start" v-if="credentialChange.credentialUpdateType === 'SMS' && smsSent">
                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.sms.value')"
                                   for="sms-test-value"></label>
                        </div>
                        <div class="col-6">
                            <input type="text" class="form-check-inline" name="sms-test-value"
                                   id="sms-test-value"
                                   autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                   required
                                   :class="(showTOTPExpFieldWarning(credentialChange.otpTestValue) ? 'invalid' : ' valid')"
                                   v-model="credentialChange.otpTestValue"/>
                            <small class="form-text text-danger"
                                   v-if="showTOTPExpFieldWarning(credentialChange.otpTestValue)"
                                   v-text="$t('ca3SApp.messages.sixNumbersExpected')">
                            </small>
                        </div>
                        <div class="col"></div>
                    </div>

                    <div>
                        <button type="submit"
                                v-on:click.prevent="previousState()"
                                class="btn btn-info" :id="updateCounter">
                            <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span
                            v-text="$t('entity.action.back')"> Back</span>
                        </button>

                        <button type="submit" :disabled="!canSubmit()" class="btn btn-primary"
                                v-text="$t('password.form.button')">Save
                        </button>
                    </div>
                </form>

            </div>
        </div>

    </div>
</template>

<script lang="ts" src="./edit-credential.component.ts">
</script>
