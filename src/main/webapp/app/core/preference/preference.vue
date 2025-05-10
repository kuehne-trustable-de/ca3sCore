<template>
    <div>
       <h2 id="page-heading">
            <span v-text="$t('ca3SApp.preference.home.title')" id="preference-heading"></span>
        </h2>
        <b-alert :show="dismissCountDown"
            dismissible
            :variant="alertType"
            @dismissed="dismissCountDown=0"
            @dismiss-count-down="countDownChanged">
            {{alertMessage}}
        </b-alert>
        <br/>

        <div class="row justify-content-center">
            <div class="col-8">
                <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                    <h2 id="ca3SApp.preference.home.createOrEditLabel" v-text="$t('ca3SApp.preference.home.editLabel')"></h2>
                    <div>
                        <div class="form-group">
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-notifyRAOnRequest" v-text="$t('ca3SApp.preference.notifyRAOnRequest')"></label>
                                </div>
                                <div class="col colContent" >
                                    <input type="checkbox" class="form-check-inline" name="preferences-notifyRAOnRequest" id="preferences-notifyRAOnRequest" v-model="preferences.notifyRAOnRequest" />
                                </div>
                            </div>

                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-checkCRL" v-text="$t('ca3SApp.preference.checkCRL')"></label>  <help-tag role="Admin" target="preference.check-crl"/>
                                </div>
                                <div class="col colContent" >
                                    <input type="checkbox" class="form-check-inline" name="preferences-checkCRL" id="preferences-checkCRL" v-model="preferences.checkCRL" />
                                </div>
                            </div>

                            <div class="row" v-if="preferences.checkCRL">
                                <div class="col ">
                                    <label for="preferences-maxNextUpdatePeriodCRLHour" v-text="$t('ca3SApp.preference.maxNextUpdatePeriodCRLHour')"></label>  <help-tag role="Admin" target="preference.max-next-update-crl"/>
                                </div>
                                <div class="col colContent" >
                                    <input type="number" class="form-check-inline" name="preferences-maxNextUpdatePeriodCRLHour" id="preferences-maxNextUpdatePeriodCRLHour" v-model="preferences.maxNextUpdatePeriodCRLHour" />
                                </div>
                            </div>

                            <h3 v-text="$t('ca3SApp.preference.home.edit.keyCreation')"></h3>
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-serverSideKeyCreationAllowed" v-text="$t('ca3SApp.preference.serverSideKeyCreationAllowed')"></label>  <help-tag role="Admin" target="preference.server-side-allowed"/>
                                </div>
                                <div class="col colContent" >
                                    <input type="checkbox" class="form-check-inline" name="preferences-serverSideKeyCreationAllowed" id="preferences-serverSideKeyCreationAllowed" v-model="preferences.serverSideKeyCreationAllowed" />
                                </div>
                            </div>

                            <div v-if="preferences.serverSideKeyCreationAllowed"class="row">
                                <div class="col ">
                                    <label for="preferences-deleteKeyAfterDays" v-text="$t('ca3SApp.preference.deleteKeyAfterDays')"></label>  <help-tag role="Admin" target="preference.delete-key-after-days"/>
                                </div>
                                <div class="col colContent" >
                                    <input type="number" class="form-check-inline" name="preferences-deleteKeyAfterDays" id="preferences-deleteKeyAfterDays" v-model="preferences.deleteKeyAfterDays" />
                                </div>
                            </div>

                            <div v-if="preferences.serverSideKeyCreationAllowed" class="row">
                                <div class="col ">
                                    <label for="preferences-deleteKeyAfterUses" v-text="$t('ca3SApp.preference.deleteKeyAfterUses')"></label>  <help-tag role="Admin" target="preference.delete-key-after-uses"/>
                                </div>
                                <div class="col colContent" >
                                    <input type="number" class="form-check-inline" name="preferences-deleteKeyAfterUses" id="preferences-deleteKeyAfterUses" v-model="preferences.deleteKeyAfterUses" />
                                </div>
                            </div>


                            <h3 v-text="$t('ca3SApp.preference.home.edit.acme')"></h3>
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-acmeHTTP01TimeoutMilliSec" v-text="$t('ca3SApp.preference.acmeHTTP01TimeoutMilliSec')"></label>  <help-tag role="Admin" target="preference.http-01-callback-timeout"/>
                                </div>
                                <div class="col colContent" >
                                    <input type="number" class="form-control" name="preferences-acmeHTTP01TimeoutMilliSec" id="preferences-acmeHTTP01TimeoutMilliSec"
                                           :class="{'valid': !$v.preferences.acmeHTTP01TimeoutMilliSec.$invalid, 'invalid': $v.preferences.acmeHTTP01TimeoutMilliSec.$invalid }"
                                        v-model.number="$v.preferences.acmeHTTP01TimeoutMilliSec.$model" required/>
                                    <small class="form-text text-danger" v-if="$v.preferences.acmeHTTP01TimeoutMilliSec.$invalid" v-text="$t('entity.validation.number')"></small>
                                </div>
                            </div>

							<div class="row">
								<div class="col ">
                                    <label  v-text="$t('ca3SApp.preference.acmeHTTP01CallbackPorts')"></label>  <help-tag role="Admin" target="preference.http-01-callback-ports"/>
								</div>
								<div class="col colContent" v-for="(v, portIndex) in $v.preferences.acmeHTTP01CallbackPortArr.$each.$iter">
                                    <input
                                           type="number" class="form-control form-check-inline valid"
                                           v-model="v.$model"
                                           v-on:input="alignCallbackPortArraySize(portIndex)"
                                    />
                                    <small class="form-text text-danger" v-if="v.$invalid" v-text="$t('entity.validation.number')"></small>
                                </div>
							</div>

                            <h3 v-text="$t('ca3SApp.preference.home.edit.algorithms')"></h3>

                            <div class="row" v-if="preferences.selectedHashes">
                                <div class="col ">
                                    <label class="form-control-label" v-text="$t('ca3SApp.preference.hashes')" for="ca3SApp-preference-hash"></label>  <help-tag role="Admin" target="preference.hash"/>
                                </div>
                                <div class="col colContent">
                                    <select class="form-control" multiple="true" id="ca3SApp-preference-hash" name="ca3SApp-preference-hash" v-model="preferences.selectedHashes">
                                        <option v-bind:value="hash.id" v-for="hash in allHashes" :key="hash.id">{{hash.name}}</option>
                                    </select>
                                </div>
                            </div>

                            <div class="row" v-if="preferences.selectedSigningAlgos">
                                <div class="col ">
                                    <label class="form-control-label" v-text="$t('ca3SApp.preference.algos')" for="ca3SApp-preference-algos"></label><help-tag role="Admin" target="preference.algo"/>
                                </div>
                                <div class="col colContent">
                                    <select class="form-control" multiple="true" id="ca3SApp-preference-algos" name="ca3SApp-preference-algos" v-model="preferences.selectedSigningAlgos">
                                        <option v-bind:value="algo.id" v-for="algo in allSignAlgos" :key="algo.id">{{algo.name}}</option>
                                    </select>
                                </div>
                            </div>

                            <!--
                            <h3 v-text="$t('ca3SApp.preference.home.edit.userAuthentication')"></h3>
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-auth-clientCert" v-text="$t('ca3SApp.preference.auth.clientCert')"></label>
                                </div>
                                <div class="col colContent" >
                                    <input :disabled="!preferences.authClientCertEnabled" type="checkbox" class="form-check-inline" name="preferences-auth-clientCert" id="preferences-auth-clientCert"
                                           v-model="preferences.authClientCert" />
                                </div>
                            </div>
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-auth-totp" v-text="$t('ca3SApp.preference.auth.totp')"></label>
                                </div>
                                <div class="col colContent" >
                                    <input type="checkbox" class="form-check-inline" name="preferences-auth-totp" id="preferences-auth-totp" v-model="preferences.authTotp" />
                                </div>
                            </div>
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-auth-email" v-text="$t('ca3SApp.preference.auth.email')"></label>
                                </div>
                                <div class="col colContent" >
                                    <input type="checkbox" class="form-check-inline" name="preferences-auth-email" id="preferences-auth-email" v-model="preferences.authEmail" />
                                </div>
                            </div>

                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-auth-sms" v-text="$t('ca3SApp.preference.auth.sms')"></label>
                                </div>
                                <div class="col colContent" >
                                    <input type="checkbox" :disabled="!preferences.smsEnabled" class="form-check-inline" name="preferences-auth-totp" id="preferences-auth-sms"
                                           v-model="preferences.sms" />
                                </div>
                            </div>
                            -->
                        </div>
                    </div>
                    <div>
                        <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')"></span>
                        </button>
                        <button type="submit" id="save-entity" class="btn btn-primary">
                            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')"></span>
                        </button>
                    </div>
                </form>
            </div>
        </div>

    </div>
</template>

<script lang="ts" src="./preference.component.ts">
</script>
