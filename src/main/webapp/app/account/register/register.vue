<template>
    <div>
        <div class="row justify-content-center">
            <div class="col-md-8 toastify-container">
                <h1 v-text="$t('register.title')" id="register-title">Registration</h1>

                <div class="alert alert-success" role="alert" v-if="success" v-html="$t('register.messages.success')">
                    <strong>Registration saved!</strong> Please check your email for confirmation.
                </div>

                <div class="alert alert-danger" role="alert" v-if="error" v-html="$t('register.messages.error.fail')">
                    <strong>Registration failed!</strong> Please try again later.
                </div>

                <div class="alert alert-danger" role="alert" v-if="errorUserExists" v-html="$t('register.messages.error.userexists')">
                    <strong>Login name already registered!</strong> Please choose another one.
                </div>

                <div class="alert alert-danger" role="alert" v-if="errorEmailExists" v-html="$t('register.messages.error.emailexists')">
                    <strong>Email is already in use!</strong> Please choose another one.
                </div>

            </div>
        </div>
        <div class="row justify-content-center">
            <div class="col-md-8">
                <form id="register-form" name="registerForm" role="form" v-on:submit.prevent="register()" v-if="!success" no-validate>
                    <div class="form-group">
                        <label class="form-control-label" for="username" v-text="$t('global.form.username.label')">Username</label>
                        <input type="text" class="form-control" v-model="$v.registerAccount.login.$model" id="username" name="login"
                               :class="{'valid': !$v.registerAccount.login.$invalid, 'invalid': $v.registerAccount.login.$invalid }"
                               required minlength="1" maxlength="50" pattern="^[_.@A-Za-z0-9-]*$" v-bind:placeholder="$t('global.form.username.placeholder')">
                        <div v-if="$v.registerAccount.login.$anyDirty && $v.registerAccount.login.$invalid">
                            <small class="form-text text-danger" v-if="!$v.registerAccount.login.required"
                                   v-text="$t('register.messages.validate.login.required')">
                                Your username is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.registerAccount.login.minLength"
                                   v-text="$t('register.messages.validate.login.minlength')">
                                Your username is required to be at least 1 character.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.registerAccount.login.maxLength"
                                   v-text="$t('register.messages.validate.login.maxlength')">
                                Your username cannot be longer than 50 characters.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.registerAccount.login.pattern"
                                   v-text="$t('register.messages.validate.login.pattern')">
                                Your username can only contain letters and digits.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" for="email" v-text="$t('global.form.email.label')"></label>
                        <input type="email" class="form-control" id="email" name="email"
                               :class="{'valid': !$v.registerAccount.email.$invalid, 'invalid': $v.registerAccount.email.$invalid }"
                               v-model="$v.registerAccount.email.$model" minlength=5 maxlength=254 email required  v-bind:placeholder="$t('global.form.email.placeholder')">
                        <div v-if="$v.registerAccount.email.$anyDirty && $v.registerAccount.email.$invalid">
                            <small class="form-text text-danger" v-if="!$v.registerAccount.email.required"
                                   v-text="$t('global.messages.validate.email.required')">
                                Your email is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.registerAccount.email.email"
                                   v-text="$t('global.messages.validate.email.invalid')">
                                Your email is invalid.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.registerAccount.email.minLength"
                                   v-text="$t('global.messages.validate.email.minlength')">
                                Your email is required to be at least 5 characters.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.registerAccount.email.maxLength"
                                   v-text="$t('global.messages.validate.email.maxlength')">
                                Your email cannot be longer than 100 characters.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" for="phone" v-text="$t('global.form.phone.label')"></label>
                        <input type="email" class="form-control" id="phone" name="phone"
                               :class="{'valid': !$v.registerAccount.phone.$invalid, 'invalid': $v.registerAccount.phone.$invalid }"
                               v-model="$v.registerAccount.phone.$model" maxlength=254 >
                        <div v-if="$v.registerAccount.phone.$anyDirty && $v.registerAccount.phone.$invalid">
                            <small class="form-text text-danger" v-if="!$v.registerAccount.phone.maxLength"
                                   v-text="$t('global.messages.validate.phone.maxlength')"></small>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" for="firstPassword" v-text="$t('global.form.newpassword.label')">New password</label>
                        <input type="password" class="form-control" id="firstPassword" name="password"
                               :class="{'valid': !$v.registerAccount.password.$invalid, 'invalid': $v.registerAccount.password.$invalid }"
                               v-model="$v.registerAccount.password.$model" minlength=4 maxlength=50 required v-bind:placeholder="$t('global.form.newpassword.placeholder')">
                        <div>
                            <small class="form-text text-danger"
                                   v-if="!$v.registerAccount.password.required" v-text="$t('global.messages.validate.newpassword.required')">
                                Your password is required.
                            </small>
                            <small class="form-text text-danger" v-if="showRegExpFieldWarning($v.registerAccount.password.$model, regExpSecret())" v-text="$t('ca3SApp.messages.password.requirement.' + regExpSecretDescription())">
                                secret must match RegEx!
                            </small>
                        </div>
                        <!--<jhi-password-strength-bar [passwordToCheck]="registerAccount.password"></jhi-password-strength-bar>-->
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" for="secondPassword" v-text="$t('global.form.confirmpassword.label')">New password confirmation</label>
                        <input type="password" class="form-control" id="secondPassword" name="confirmPasswordInput"
                               :class="{'valid': !$v.confirmPassword.$invalid, 'invalid': $v.confirmPassword.$invalid }"
                               v-model="$v.confirmPassword.$model" minlength=4 maxlength=50 required v-bind:placeholder="$t('global.form.confirmpassword.placeholder')">
                        <div>
                            <small class="form-text text-danger"
                                   v-if="!$v.confirmPassword.required" v-text="$t('global.messages.validate.newpassword.required')">
                                Your password is required.
                            </small>
                            <small class="form-text text-danger" v-if="showRegExpFieldWarning($v.confirmPassword.$model, regExpSecret())" v-text="$t('ca3SApp.messages.password.requirement.' + regExpSecretDescription())">
                                secret must match RegEx!
                            </small>
                            <small class="form-text text-danger" v-if="$v.registerAccount.password.$model !== $v.confirmPassword.$model" v-text="$t('global.messages.error.dontmatch')">
                                The password and its confirmation do not match!
                            </small>
                        </div>
                    </div>

                    <button type="submit" :disabled="$v.registerAccount.$invalid || $v.confirmPassword.$invalid" class="btn btn-primary" v-text="$t('register.form.button')">Register</button>
                </form>
                <p></p>
            </div>
        </div>
    </div>
</template>

<script lang='ts' src='./register.component.ts'>
</script>
