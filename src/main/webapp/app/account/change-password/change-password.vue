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
                <h2 v-if="account" id="password-title"><span v-html="$t('password.title', { 'username': username})"></span></h2>

                <div class="alert alert-success" role="alert" v-if="success" v-html="$t('password.messages.success')">
                    <strong>Password changed!</strong>
                </div>
                <div class="alert alert-danger" role="alert" v-if="error"  v-html="$t('password.messages.error')">
                    <strong>An error has occurred!</strong> The password could not be changed.
                </div>

                <div class="alert alert-danger" role="alert" v-if="doNotMatch" v-text="$t('global.messages.error.dontmatch')">
                    The password and its confirmation do not match!
                </div>

                <form name="form" role="form" id="password-form" v-on:submit.prevent="changePassword()">

                    <div class="form-group">
                        <label class="form-control-label" for="currentPassword" v-text="$t('global.form.currentpassword.label')"></label>
                        <input type="password" class="form-control" id="currentPassword" name="currentPassword"
                               :class="{'valid': !$v.resetPassword.currentPassword.$invalid, 'invalid': $v.resetPassword.currentPassword.$invalid }"
                               v-bind:placeholder="$t('global.form.currentpassword.placeholder')"
                               v-model="$v.resetPassword.currentPassword.$model" required>
                        <div v-if="$v.resetPassword.currentPassword.$anyDirty && $v.resetPassword.currentPassword.$invalid">
                            <small class="form-text text-danger"
                                   v-if="!$v.resetPassword.currentPassword.required" v-text="$t('global.messages.validate.newpassword.required')">
                                Your password is required.
                            </small>
                        </div>
                    </div>

                    <div v-if="credentialChange.credentialUpdateType === 'PASSWORD'" class="form-group">
                        <label class="form-control-label" for="newPassword" v-text="$t('global.form.newpassword.label')"></label>
                        <input type="password" class="form-control" id="newPassword" name="newPassword"
                               v-bind:placeholder="$t('global.form.newpassword.placeholder')"
                               :class="{'valid': !$v.resetPassword.newPassword.$invalid, 'invalid': $v.resetPassword.newPassword.$invalid }"
                               v-model="$v.resetPassword.newPassword.$model" minlength=4 maxlength=50 required>
                        <div>
                            <small class="form-text text-danger"
                                   v-if="!$v.resetPassword.newPassword.required" v-text="$t('global.messages.validate.newpassword.required')">
                            </small>
                            <small class="form-text text-danger" v-if="showRegExpFieldWarning($v.resetPassword.newPassword.$model, regExpSecret())" v-text="$t('ca3SApp.messages.password.requirement.' + regExpSecretDescription())">
                            </small>
                        </div>
                        <!--<jhi-password-strength-bar [passwordToCheck]="newPassword"></jhi-password-strength-bar>-->
                    </div>
                    <div v-if="credentialChange.credentialUpdateType === 'PASSWORD'" class="form-group">
                        <label class="form-control-label" for="confirmPassword" v-text="$t('global.form.confirmpassword.label')"></label>
                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                               :class="{'valid': !$v.resetPassword.confirmPassword.$invalid, 'invalid': $v.resetPassword.confirmPassword.$invalid }"
                               v-bind:placeholder="$t('global.form.confirmpassword.placeholder')"
                               v-model="$v.resetPassword.confirmPassword.$model" minlength=4 maxlength=50 required>
                        <div>
                            <small class="form-text text-danger"
                                   v-if=" $v.resetPassword.newPassword.$model && ($v.resetPassword.newPassword.$model !== $v.resetPassword.confirmPassword.$model)"
                                   v-text="$t('entity.validation.secretRepeat')">
                            </small>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" for="secondFactorRequired" v-text="$t('global.form.secondFactorRequired.label')"></label>
                        <input type="checkbox" class="form-check-inline" id="secondFactorRequired" name="secondFactorRequired"
                               disabled="disabled"
                               readOnly="readOnly"
                               v-model="credentialChange.secondFactorRequired">
                    </div>

                    <button type="submit" :disabled="!canSubmit()" class="btn btn-primary" v-text="$t('password.form.button')">Save</button>
                </form>

                <p></p>
                <h3 id="second-factor-title"><span v-text="$t('global.messages.secondfactor.title')"></span></h3>

                <router-link v-if="canCreateSecondFactor('CLIENT_CERT')" :to="{name: 'EditCredential', params: {credentialType: 'CLIENT_CERT'}}"
                             tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity ">
                    <font-awesome-icon icon="plus"></font-awesome-icon>
                    <span v-text="$t('ca3SApp.form.client.credentials.client-cert.create')"></span>
                </router-link>

                <router-link v-if="canCreateSecondFactor('TOTP')" :to="{name: 'EditCredential', params: {credentialType: 'TOTP'}}"
                             tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity ">
                    <font-awesome-icon icon="plus"></font-awesome-icon>
                    <span v-text="$t('ca3SApp.form.client.credentials.totp.create')"></span>
                </router-link>

                <router-link v-if="hasPhoneNumber() && canCreateSecondFactor('SMS')" :to="{name: 'EditCredential', params: {credentialType: 'SMS'}}"
                             tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity ">
                    <font-awesome-icon icon="plus"></font-awesome-icon>
                    <span v-text="$t('ca3SApp.form.client.credentials.sms.create')"></span>
                </router-link>


                <div class="table-responsive">
                    <table class="table table-striped" id="credential-list">
                        <thead>
                        <tr>
                            <th><span v-text="$t('ca3SApp.form.client.auth.type')"></span></th>
                            <th><span v-text="$t('ca3SApp.form.client.auth.requestedOn')"></span></th>
                            <th><span v-text="$t('ca3SApp.form.client.auth.validTo')"></span></th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="accountCredential in accountCredentialArr"
                            :key="accountCredential.id">

                            <td v-if="accountCredential.relationType === 'CLIENT_CERTIFICATE'" @click="$router.push({name: 'CertInfo', params: {certificateId: accountCredential.id}})">{{accountCredential.relationType}} {{accountCredential.id}}</td>
                            <td v-if="accountCredential.relationType !== 'CLIENT_CERTIFICATE'">{{accountCredential.relationType}} </td>
                            <td>{{toLocalDate(accountCredential.createdOn)}}</td>
                            <td>{{toLocalDate(accountCredential.validTo)}}</td>
                            <td class="text-right">
                                <div class="btn-group">
                                    <b-button v-on:click="prepareRemove(accountCredential)"
                                              variant="danger"
                                              class="btn btn-sm"
                                              v-b-modal.removeEntity>
                                        <font-awesome-icon icon="times"></font-awesome-icon>
                                        <span class="d-none d-md-inline" v-text="$t('entity.action.delete')"></span>
                                    </b-button>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>



        <b-modal ref="removeEntity" id="removeEntity" >
            <span slot="modal-title"><span id="ca3SApp.clientAuthSecret.delete.question" v-text="$t('entity.delete.title')"></span></span>
            <div class="modal-body">
                <p id="jhi-delete-clientAuthSecret-heading" v-bind:title="$t('ca3SApp.clientAuthSecret.delete.question')"></p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-clientAuthSecret" v-text="$t('entity.action.delete')" v-on:click="removeCredential()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./change-password.component.ts">
</script>
