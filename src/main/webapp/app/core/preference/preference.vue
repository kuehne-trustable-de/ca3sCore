<template>
    <div>
       <h2 id="page-heading">
            <span v-text="$t('ca3SApp.preference.home.title')" id="preference-heading">Preferences</span>
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
                    <h2 id="ca3SApp.preference.home.createOrEditLabel" v-text="$t('ca3SApp.preference.home.editLabel')">Edit a Preference</h2>
                    <div>
                        <div class="form-group">
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-checkCRL" v-text="$t('ca3SApp.preference.checkCRL')">Check CRLs (to update certificate state)</label>
                                </div>
                                <div class="col colContent" >
                                    <input type="checkbox" class="form-check-inline" name="preferences-checkCRL" id="preferences-checkCRL" v-model="preferences.checkCRL" />
                                </div>
                            </div>

                            <div class="row" v-if="preferences.checkCRL">
                                <div class="col ">
                                    <label for="preferences-maxNextUpdatePeriodCRLHour" v-text="$t('ca3SApp.preference.maxNextUpdatePeriodCRLHour')">Store CRLs for up to N hours</label>
                                </div>
                                <div class="col colContent" >
                                    <input type="number" class="form-check-inline" name="preferences-maxNextUpdatePeriodCRLHour" id="preferences-maxNextUpdatePeriodCRLHour" v-model="preferences.maxNextUpdatePeriodCRLHour" />
                                </div>
                            </div>
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-serverSideKeyCreationAllowed" v-text="$t('ca3SApp.preference.serverSideKeyCreationAllowed')">Server side key creation allowed</label>
                                </div>
                                <div class="col colContent" >
                                    <input type="checkbox" class="form-check-inline" name="preferences-serverSideKeyCreationAllowed" id="preferences-serverSideKeyCreationAllowed" v-model="preferences.serverSideKeyCreationAllowed" />
                                </div>
                            </div>
                            <div class="row">
                                <div class="col ">
                                    <label for="preferences-acmeHTTP01TimeoutMilliSec" v-text="$t('ca3SApp.preference.acmeHTTP01TimeoutMilliSec')">ACME HTTP01 callback timeout (milli sec)</label>
                                </div>
                                <div class="col colContent" >
                                    <input type="number" class="form-control" name="preferences-acmeHTTP01TimeoutMilliSec" id="preferences-acmeHTTP01TimeoutMilliSec"
                                           :class="{'valid': !$v.preferences.acmeHTTP01TimeoutMilliSec.$invalid, 'invalid': $v.preferences.acmeHTTP01TimeoutMilliSec.$invalid }"
                                        v-model.number="$v.preferences.acmeHTTP01TimeoutMilliSec.$model" required/>
                                    <small class="form-text text-danger" v-if="$v.preferences.acmeHTTP01TimeoutMilliSec.$invalid" v-text="$t('entity.validation.number')">
                                        This field should be a number.
                                    </small>
                                </div>
                            </div>

                            <!--label for="preferences-acmeHTTP01CallbackPorts" v-text="$t('ca3SApp.preference.acmeHTTP01CallbackPorts')">ACME HTTP01 callback ports</label>
                            <input type="text" class="form-control" name="preferences-acmeHTTP01CallbackPorts" id="preferences-acmeHTTP01CallbackPorts"
                                v-model.number="preferences.acmeHTTP01CallbackPorts"  required/-->

							<div class="row">
								<div class="col ">
                                    <label  v-text="$t('ca3SApp.preference.acmeHTTP01CallbackPorts')">ACME HTTP01 callback ports</label>
								</div>
								<div class="col colContent" v-for="(v, portIndex) in $v.preferences.acmeHTTP01CallbackPortArr.$each.$iter">
                                    <input
                                           type="number" class="form-control form-check-inline valid"
                                           v-model="v.$model"
                                           v-on:input="alignCallbackPortArraySize(portIndex)"
                                    />
                                    <small class="form-text text-danger" v-if="v.$invalid" v-text="$t('entity.validation.number')">
                                        This field should be a valid port number.
                                    </small>
                                </div>
							</div>

                            <div class="row" v-if="preferences.selectedHashes">
                                <div class="col ">
                                    <label class="form-control-label" v-text="$t('ca3SApp.preference.hashes')" for="ca3SApp-preference-hash">HashAlgos</label>
                                </div>
                                <div class="col colContent">
                                    <select class="form-control" multiple="true" id="ca3SApp-preference-hash" name="ca3SApp-preference-hash" v-model="preferences.selectedHashes">
                                        <option v-bind:value="hash.id" v-for="hash in allHashes" :key="hash.id">{{hash.name}}</option>
                                    </select>
                                </div>
                            </div>

                            <div class="row" v-if="preferences.selectedSigningAlgos">
                                <div class="col ">
                                    <label class="form-control-label" v-text="$t('ca3SApp.preference.algos')" for="ca3SApp-preference-algos">SigningAlgos</label>
                                </div>
                                <div class="col colContent">
                                    <select class="form-control" multiple="true" id="ca3SApp-preference-algos" name="ca3SApp-preference-algos" v-model="preferences.selectedSigningAlgos">
                                        <option v-bind:value="algo.id" v-for="algo in allSignAlgos" :key="algo.id">{{algo.name}}</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div>
                        <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                        </button>
                        <button type="submit" id="save-entity" class="btn btn-primary">
                            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>

    </div>
</template>

<script lang="ts" src="./preference.component.ts">
</script>
