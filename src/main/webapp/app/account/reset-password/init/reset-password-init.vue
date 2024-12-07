<template>
    <div>
        <div class="row justify-content-center">
            <div class="col-md-8">
                <h1 v-text="$t('reset.request.title')">Reset your password</h1>

                <div class="alert alert-warning" v-if="!success">
                    <p v-text="$t('reset.request.messages.info')">Enter your username.</p>
                </div>

                <div class="alert alert-success" v-if="success">
                    <p v-text="$t('reset.request.messages.success')">Check your emails for details on how to reset your password.</p>
                </div>

                <form v-if="!success" name="form" role="form" v-on:submit.prevent="requestReset()">
                    <div class="form-group">
                        <label class="form-control-label" for="username" v-text="$t('global.form.username.label')">Username</label>
                        <input type="text" class="form-control" id="username" name="username" v-bind:placeholder="$t('global.form.username.placeholder')"
                               :class="{'valid': !$v.resetAccount.username.$invalid, 'invalid': $v.resetAccount.username.$invalid }"
                               v-model="$v.resetAccount.username.$model" minlength=5 maxlength=254  required>
                        <div v-if="$v.resetAccount.username.$anyDirty && $v.resetAccount.username.$invalid">
                            <small class="form-text text-danger" v-if="!$v.resetAccount.username.required"
                                   v-text="$t('global.messages.validate.username.required')">
                                Your email is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.resetAccount.username.email"
                                   v-text="$t('global.messages.validate.username.invalid')">
                                Your email is invalid.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.resetAccount.username.minLength"
                                   v-text="$t('global.messages.validate.username.minlength')">
                                Your email is required to be at least 5 characters.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.resetAccount.username.maxLength"
                                   v-text="$t('global.messages.validate.username.maxlength')">
                                Your username cannot be longer than 100 characters.
                            </small>
                        </div>
                    </div>
                    <button type="submit" :disabled="$v.resetAccount.$invalid" class="btn btn-primary" v-text="$t('reset.request.form.button')">Reset</button>
                </form>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./reset-password-init.component.ts">
</script>
