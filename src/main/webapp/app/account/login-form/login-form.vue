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
                        <b-form-input id="username" type="text" name="username" autofocus v-bind:placeholder="$t('global.form.username.placeholder')" v-model="login">
                        </b-form-input>
                    </b-form-group>
                    <b-form-group v-bind:label="$t('login.form.password')" label-for="password">
                        <b-form-input id="password" type="password" name="password" v-bind:placeholder="$t('login.form.password.placeholder')" v-model="password">
                        </b-form-input>
                    </b-form-group>
                    <!--b-form-checkbox id="rememberMe" name="rememberMe" v-model="rememberMe" checked >
                        <span v-text="$t('login.form.rememberme')">Remember me</span>
                    </b-form-checkbox-->
                    <!--div>
                        <a href="foo" @click.prevent="requestClientCert()">Validate Client Cert</a>
                    </div-->
                    <div>
                        <b-button type="submit"
                                  :disabled="!password || password.trim().length === 0"
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
