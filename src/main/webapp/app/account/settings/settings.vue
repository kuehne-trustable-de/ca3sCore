<template>
    <div>
        <div class="row justify-content-center">
            <div class="col-md-8 toastify-container">
                <h2 v-if="username" id="settings-title"><span v-bind:value="$t('settings.title')">User settings for [<b>{{username}}</b>]</span></h2>

                <div class="alert alert-success" role="alert" v-if="success" v-html="$t('settings.messages.success')"></div>

                <div class="alert alert-danger" role="alert" v-if="errorEmailExists" v-html="$t('register.messages.error.emailexists')"></div>

                <!--<jhi-alert-error></jhi-alert-error>-->

                <b v-if="settingsAccount.managedExternally" v-text="$t('settings.form.disabled.managed.externally')"></b>

                <form name="form" id="settings-form" role="form" v-on:submit.prevent="save()" v-if="settingsAccount" novalidate>

                    <div class="form-group">
                        <label class="form-control-label" for="firstName" v-text="$t('settings.form.firstname')"></label>
                        <input type="text" class="form-control" id="firstName" name="firstName" v-bind:placeholder="$t('settings.form.firstname.placeholder')"
                               :disabled="settingsAccount.managedExternally"
                               :class="{'valid': !$v.settingsAccount.firstName.$invalid, 'invalid': $v.settingsAccount.firstName.$invalid }"
                               v-model="$v.settingsAccount.firstName.$model" minlength=1 maxlength=50 required>
                        <div v-if="$v.settingsAccount.firstName.$anyDirty && $v.settingsAccount.firstName.$invalid">
                            <small class="form-text text-danger"
                                   v-if="!$v.settingsAccount.firstName.required" v-text="$t('settings.messages.validate.firstname.required')"></small>
                            <small class="form-text text-danger"
                                   v-if="!$v.settingsAccount.firstName.minLength" v-text="$t('settings.messages.validate.firstname.minlength')"></small>
                            <small class="form-text text-danger"
                                   v-if="!$v.settingsAccount.firstName.maxLength" v-text="$t('settings.messages.validate.firstname.maxlength')"></small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" for="lastName" v-text="$t('settings.form.lastname')"></label>
                        <input type="text" class="form-control" id="lastName" name="lastName" v-bind:placeholder="$t('settings.form.lastname.placeholder')"
                               :disabled="settingsAccount.managedExternally"
                               :class="{'valid': !$v.settingsAccount.lastName.$invalid, 'invalid': $v.settingsAccount.lastName.$invalid }"
                               v-model="$v.settingsAccount.lastName.$model" minlength=1 maxlength=50 required>
                        <div v-if="$v.settingsAccount.lastName.$anyDirty && $v.settingsAccount.lastName.$invalid">
                            <small class="form-text text-danger"
                                   v-if="!$v.settingsAccount.lastName.required" v-text="$t('settings.messages.validate.lastname.required')"></small>
                            <small class="form-text text-danger"
                                   v-if="!$v.settingsAccount.lastName.minLength" v-text="$t('settings.messages.validate.lastname.minlength')"></small>
                            <small class="form-text text-danger"
                                   v-if="!$v.settingsAccount.lastName.maxLength" v-text="$t('settings.messages.validate.lastname.maxlength')"></small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" for="email" v-text="$t('global.form.email.label')"></label>
                        <input type="email" class="form-control" id="email" name="email" v-bind:placeholder="$t('global.form.email.placeholder')"
                               :disabled="settingsAccount.managedExternally"
                               :class="{'valid': !$v.settingsAccount.email.$invalid, 'invalid': $v.settingsAccount.email.$invalid }"
                               v-model="$v.settingsAccount.email.$model" minlength="5" maxlength="254" email required>
                        <div v-if="$v.settingsAccount.email.$anyDirty && $v.settingsAccount.email.$invalid">
                            <small class="form-text text-danger" v-if="!$v.settingsAccount.email.required"
                                   v-text="$t('global.messages.validate.email.required')"></small>
                            <small class="form-text text-danger" v-if="!$v.settingsAccount.email.email"
                                   v-text="$t('global.messages.validate.email.invalid')"></small>
                            <small class="form-text text-danger" v-if="!$v.settingsAccount.email.minLength"
                                   v-text="$t('global.messages.validate.email.minlength')"></small>
                            <small class="form-text text-danger" v-if="!$v.settingsAccount.email.maxLength"
                                   v-text="$t('global.messages.validate.email.maxlength')"></small>
                        </div>
                    </div>

                    <div class="form-group" v-if="languages && Object.keys(languages).length > 1">
                        <label for="langKey" v-text="$t('settings.form.language')"></label>
                        <select class="form-control" id="langKey" name="langKey" v-model="settingsAccount.langKey">
                            <option v-for="(language, key) in languages" :value="key" :key="`lang-${key}`">{{language.name}}</option>
                        </select>
                    </div>
                    <button type="submit" :disabled="$v.settingsAccount.$invalid" class="btn btn-primary" v-text="$t('settings.form.button')"></button>
                </form>


                <!--div class="row wrap" >
                    <div class="col">
                        <label class="form-control-label" for="email" v-text="$t('global.form.client.cert')"></label>
                        <input type="password"
                               class="form-control form-check-inline w-50"
                               :class="(showRequiredWarning(true, secret) ? 'invalid' : ' valid')"
                               name="client-auth-secret" id="client-auth-secret"
                               autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                               required="false"
                               v-model="secret" />
                        <small v-if="showRequiredWarning(true, secret)"
                               class="form-text text-danger" v-text="$t('entity.validation.required')"></small>
                        <small class="form-text text-danger" v-if="showRegExpFieldWarning(secret, regExpSecret())" v-text="$t('ca3SApp.messages.password.requirement.' + regExpSecretDescription())"></small>
                    </div>

                    <div class="col" v-if="!showRegExpFieldWarning(secret, regExpSecret())">
                        <a href="downloadUrl" id="certificate-download" @click.prevent="downloadItem()">FooBarBaz.p12</a>
                    </div>
                </div-->

            </div>
        </div>

    </div>
</template>

<script lang="ts" src="./settings.component.ts">
</script>
