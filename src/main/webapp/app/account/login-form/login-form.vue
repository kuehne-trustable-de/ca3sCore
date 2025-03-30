<template>
    <div class="modal-body">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <b-alert show variant="danger" v-if="authenticationError" v-html="$t('login.messages.error.authentication')"></b-alert>
                <b-alert show variant="danger" v-if="isBlocked" v-html="$t('login.messages.error.blocked', { 'blockedUntil': convertDateTimeFromServer(blockedUntil) })"></b-alert>
            </div>

            <div class="col-md-8">
                <b-form role="form" v-on:submit.prevent="doLogin()">
                    <b-form-group v-bind:label="$t('global.form.username.label')" label-for="username">
                        <b-form-input id="username" type="text" name="username" autofocus v-bind:placeholder="$t('global.form.username.placeholder')" v-model="loginData.username">
                        </b-form-input>

                        <small v-if="showUsernameWarning()"
                               class="form-text text-danger" v-text="$t('entity.validation.required')"></small>
                    </b-form-group>

                    <b-form-group v-bind:label="$t('login.form.password')" label-for="password">
                        <b-form-input id="password" type="password" name="password" v-bind:placeholder="$t('login.form.password.placeholder')" v-model="loginData.password">
                        </b-form-input>
                        <small v-if="showPasswordWarning()"
                               class="form-text text-danger" v-text="$t('entity.validation.required')"></small>
                    </b-form-group>

                    <b-form-group v-bind:label="$t('global.form.second-factor')" label-for="second-factor">
                        <select class="form-control" id="second-factor" name="second-factor" v-model="loginData.authSecondFactor">
                            <option value="NONE" v-bind:label="$t('login.form.second-factor.NONE')"></option>
                            <option value="CLIENT_CERT" v-if="canUseSecondFactor('CLIENT_CERT')" v-bind:label="$t('login.form.second-factor.CLIENT_CERT')">CLIENT_CERT</option>
                            <option value="TOTP" v-if="canUseSecondFactor('TOTP')" v-bind:label="$t('login.form.second-factor.TOTP')">TOTP</option>
                            <!--option value="EMAIL" v-bind:label="$t('login.form.second-factor.EMAIL')">EMAIL</option-->
                            <option value="SMS" v-if="canUseSecondFactor('SMS')" v-bind:label="$t('login.form.second-factor.SMS')">SMS</option>
                        </select>
                    </b-form-group>

                    <b-form-group v-bind:label="$t('global.form.second-factor')" label-for="second-factor" v-if="(loginData.authSecondFactor == 'SMS') && !isSmsSent">
                        <button type="submit" class="btn btn-primary"
                                v-text="$t('login.form.send.sms')"
                                @click.prevent="sendSMS()">
                        </button>
                    </b-form-group>


                    <div v-if="loginData.authSecondFactor == 'TOTP' ||
                            loginData.authSecondFactor == 'EMAIL' ||
                            ( loginData.authSecondFactor == 'SMS' && isSmsSent )">
                        <b-form-input id="" type="password" name="secondSecret" v-bind:placeholder="$t('login.form.password.secondSecret')"
                                      v-model="loginData.secondSecret">
                        </b-form-input>
                        <small v-if="showSecondFactorWarning()"
                               class="form-text text-danger" v-text="$t('entity.validation.required')"></small>
                        <small class="form-text text-danger"
                               v-if="showTOTPExpFieldWarning(loginData.secondSecret)"
                               v-text="$t('ca3SApp.messages.sixNumbersExpected')">
                        </small>

                    </div>

                    <p></p>
                    <div>
                        <b-button type="submit"
                                  :disabled="!loginEnabled()"
                                  variant="primary"
                                  id="login.form.submit"
                                  v-text="$t('login.form.button')"></b-button>
                    </div>
                </b-form>
                <p></p>
                <div>
                    <b-alert show variant="warning">
                        <b-link :to="'/reset/request'" class="alert-link" v-text="$t('login.password.forgot')"></b-link>
                    </b-alert>
                </div>
                <div>
                    <b-alert show variant="warning">
                        <span v-text="$t('global.messages.info.register.noaccount')"></span>
                        <b-link :to="'/register'" class="alert-link" v-text="$t('global.messages.info.register.link')"></b-link>
                    </b-alert>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" src="./login-form.component.ts">
</script>
