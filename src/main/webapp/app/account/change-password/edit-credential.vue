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
        <div class="row row-gap-3 justify-content-center">
            <div class="col-md-8 toastify-container">
                <h2 v-if="account" id="password-title"><span v-html="$t('password.title', { 'username': username})"></span>
                </h2>

                <h3 v-if="credentialChange.credentialUpdateType === 'CLIENT_CERT'" id="client-cert-title"><span
                    v-text="$t('global.messages.client.cert.title')"></span></h3>
                <h3 v-if="credentialChange.credentialUpdateType === 'TOTP'" id="totp-title"><span
                    v-text="$t('global.messages.totp.title')"></span></h3>
                <h3 v-if="credentialChange.credentialUpdateType === 'SMS'" id="sms-title"><span
                    v-text="$t('global.messages.sms.title')"></span></h3>
                <h3 v-if="credentialChange.credentialUpdateType === 'API_TOKEN'" id="api-token-title"><span
                    v-text="$t('global.messages.api.token.title')"></span></h3>
                <h3 v-if="credentialChange.credentialUpdateType === 'EST_TOKEN'" id="est-token-title"><span
                    v-text="$t('global.messages.est.token.title')"></span></h3>
                <h3 v-if="credentialChange.credentialUpdateType === 'SCEP_TOKEN'" id="scep-token-title"><span
                    v-text="$t('global.messages.scep.token.title')"></span></h3>
                <h3 v-if="credentialChange.credentialUpdateType === 'EAB_PASSWORD'" id="eab.password-title"><span
                    v-text="$t('global.messages.eab.password.title')"></span></h3>

                <div class="alert alert-danger" role="alert" v-if="oldPasswordMismatch" v-text="$t('global.messages.error.oldPasswordMismatch')"></div>

                <form name="form" role="form" id="password-form" v-on:submit.prevent="saveCredentials()">

                    <div class="row row-gap-3 justify-content-start" v-if="!settingsAccount.managedExternally">
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
                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'CLIENT_CERT'">
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

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.clientAuthCertId !== 0">
                        <div class="col-sm">
                            <label class="form-control-label"
                                   v-text="$t('ca3SApp.form.client.cert.install')"
                                   for="jhi-test-clientAuthCert">
                            </label>
                        </div>
                        <div class="col-6"></div>
                        <div class="col"></div>
                    </div>

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'TOTP'">
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

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'TOTP'">
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

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'TOTP'  && !useGivenSeed">
                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.otp.qrcode')"
                                   for="request-otp-qrcode"></label>
                        </div>
                        <div class="col-6">
                            <img :src="qrCodeImgUrl"/>
                        </div>
                        <div class="col"></div>
                    </div>

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'TOTP'">
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

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'SMS'">
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

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'SMS' && smsSent">
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

                    <div class="row row-gap-3 justify-content-start"
                         v-if="credentialChange.credentialUpdateType === 'SCEP_TOKEN' || credentialChange.credentialUpdateType === 'EST_TOKEN' || credentialChange.credentialUpdateType === 'EAB_PASSWORD'">
                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.api.token.validity')"
                                   for="api-token-validity"></label>
                        </div>
                        <div class="col-6">
                            <select
                              class="form-control form-check-inline w-50"
                              id="api-token-validity" name="api-token-validity"
                              v-model="credentialChange.apiTokenValiditySeconds">
                              <option value="86400" v-text="$t('ca3SApp.Interval.DAY')" selected="selected"></option>
                              <option value="604800" v-text="$t('ca3SApp.Interval.WEEK')"></option>
                              <option value="2678400" v-text="$t('ca3SApp.Interval.MONTH')"></option>
                              <option value="31536000" v-text="$t('ca3SApp.Interval.YEAR')"></option>
                            </select>
                        </div>
                        <div class="col"></div>
                    </div>

                  <div class="row row-gap-3 justify-content-start"
                       v-if="credentialChange.credentialUpdateType === 'SCEP_TOKEN' || credentialChange.credentialUpdateType === 'EST_TOKEN' || credentialChange.credentialUpdateType === 'EAB_PASSWORD'">
                    <div class="col-sm">
                        <label class="form-control-label" v-text="$t('ca3SApp.form.client.pipeline')"
                               for="pipeline"></label>
                    </div>

                    <div class="col-6">
                      <select
                        class="form-control form-check-inline w-50"
                        id="pipeline" name="pipeline"
                        v-model="credentialChange.pipelineId">
                        <option v-for="pipeline in pipelineViewArr"
                                v-bind:value="pipeline.id"
                                :key="pipeline.id">{{ pipeline.name }}
                        </option>
                      </select>
                    </div>

                    <div class="col"></div>
                  </div>

                  <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'API_TOKEN' || credentialChange.credentialUpdateType === 'SCEP_TOKEN' || credentialChange.credentialUpdateType === 'EST_TOKEN'">

                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.api.token.value')"
                                   for="api-token-value"></label>
                        </div>
                        <div class="col-6">
                            <input type="text"
                                   class="form-control form-check-inline w-50"
                                   name="api-token-value"
                                   id="api-token-value"
                                   autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                   readonly
                                   v-model="credentialChange.apiTokenValue" />
                            <CopyClipboardButton contentElementId="api-token-value"/>

                        </div>

                        <div class="col">
                        </div>
                    </div>

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'EAB_PASSWORD'">

                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.api.kid.value')"
                                   for="api-kid-value"></label>
                        </div>

                        <div class="col-6">
                            <input type="text"
                                   class="form-control form-check-inline w-100"
                                   name="api-kid-value"
                                   id="api-kid-value"
                                   autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                   readonly
                                   v-model="credentialChange.eabKid" />
                            <CopyClipboardButton contentElementId="eab-kid"/>
                        </div>

                        <div class="col">
                        </div>
                    </div>

                    <div class="row row-gap-3 justify-content-start" v-if="credentialChange.credentialUpdateType === 'EAB_PASSWORD'">

                        <div class="col-sm">
                            <label class="form-control-label" v-text="$t('ca3SApp.form.client.api.hmac.key')"
                                   for="api-token-value"></label>
                        </div>

                        <div class="col-6">
                            <input type="text"
                                   class="form-control form-check-inline w-100"
                                   name="api-token-value"
                                   id="api-token-value"
                                   autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                   readonly
                                   v-model="credentialChange.apiTokenValue" />
                            <CopyClipboardButton contentElementId="credentialChange.apiTokenValue"/>
                        </div>


                        <div class="col">
                        </div>
                    </div>


                    <div>
                        <button type="submit"
                                v-on:click.prevent="previousState()"
                                class="btn btn-info" :id="updateCounter">
                            <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span
                            v-text="$t('entity.action.back')"></span>
                        </button>

                        <button type="submit" :disabled="!canSubmit()" class="btn btn-primary"
                                v-text="$t('password.form.button')"></button>
                    </div>
                </form>

            </div>
        </div>

    </div>
</template>

<script lang="ts" src="./edit-credential.component.ts">
</script>
