<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.acmeChallenge.home.createOrEditLabel" v-text="$t('ca3SApp.acmeChallenge.home.createOrEditLabel')">Create or edit a AcmeChallenge</h2>
                <div>
                    <div class="form-group" v-if="acmeChallenge.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="acmeChallenge.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeChallenge.challengeId')" for="acme-challenge-challengeId">Challenge Id</label>
                        <input type="number" class="form-control" name="challengeId" id="acme-challenge-challengeId"
                            :class="{'valid': !$v.acmeChallenge.challengeId.$invalid, 'invalid': $v.acmeChallenge.challengeId.$invalid }" v-model.number="$v.acmeChallenge.challengeId.$model"  required/>
                        <div v-if="$v.acmeChallenge.challengeId.$anyDirty && $v.acmeChallenge.challengeId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeChallenge.challengeId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.acmeChallenge.challengeId.numeric" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeChallenge.type')" for="acme-challenge-type">Type</label>
                        <input type="text" class="form-control" name="type" id="acme-challenge-type"
                            :class="{'valid': !$v.acmeChallenge.type.$invalid, 'invalid': $v.acmeChallenge.type.$invalid }" v-model="$v.acmeChallenge.type.$model"  required/>
                        <div v-if="$v.acmeChallenge.type.$anyDirty && $v.acmeChallenge.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeChallenge.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeChallenge.value')" for="acme-challenge-value">Value</label>
                        <input type="text" class="form-control" name="value" id="acme-challenge-value"
                            :class="{'valid': !$v.acmeChallenge.value.$invalid, 'invalid': $v.acmeChallenge.value.$invalid }" v-model="$v.acmeChallenge.value.$model"  required/>
                        <div v-if="$v.acmeChallenge.value.$anyDirty && $v.acmeChallenge.value.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeChallenge.value.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeChallenge.token')" for="acme-challenge-token">Token</label>
                        <input type="text" class="form-control" name="token" id="acme-challenge-token"
                            :class="{'valid': !$v.acmeChallenge.token.$invalid, 'invalid': $v.acmeChallenge.token.$invalid }" v-model="$v.acmeChallenge.token.$model"  required/>
                        <div v-if="$v.acmeChallenge.token.$anyDirty && $v.acmeChallenge.token.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeChallenge.token.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeChallenge.validated')" for="acme-challenge-validated">Validated</label>
                        <div class="d-flex">
                            <input id="acme-challenge-validated" type="datetime-local" class="form-control" name="validated" :class="{'valid': !$v.acmeChallenge.validated.$invalid, 'invalid': $v.acmeChallenge.validated.$invalid }"
                            
                            :value="convertDateTimeFromServer($v.acmeChallenge.validated.$model)"
                            @change="updateInstantField('validated', $event)"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeChallenge.status')" for="acme-challenge-status">Status</label>
                        <select class="form-control" name="status" :class="{'valid': !$v.acmeChallenge.status.$invalid, 'invalid': $v.acmeChallenge.status.$invalid }" v-model="$v.acmeChallenge.status.$model" id="acme-challenge-status"  required>
                            <option value="PENDING" v-bind:label="$t('ca3SApp.ChallengeStatus.PENDING')">PENDING</option>
                            <option value="VALID" v-bind:label="$t('ca3SApp.ChallengeStatus.VALID')">VALID</option>
                            <option value="INVALID" v-bind:label="$t('ca3SApp.ChallengeStatus.INVALID')">INVALID</option>
                            <option value="DEACTIVATED" v-bind:label="$t('ca3SApp.ChallengeStatus.DEACTIVATED')">DEACTIVATED</option>
                            <option value="EXPIRED" v-bind:label="$t('ca3SApp.ChallengeStatus.EXPIRED')">EXPIRED</option>
                            <option value="REVOKED" v-bind:label="$t('ca3SApp.ChallengeStatus.REVOKED')">REVOKED</option>
                        </select>
                        <div v-if="$v.acmeChallenge.status.$anyDirty && $v.acmeChallenge.status.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeChallenge.status.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeChallenge.acmeAuthorization')" for="acme-challenge-acmeAuthorization">Acme Authorization</label>
                        <select class="form-control" id="acme-challenge-acmeAuthorization" name="acmeAuthorization" v-model="acmeChallenge.acmeAuthorization">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="acmeChallenge.acmeAuthorization && acmeAuthorizationOption.id === acmeChallenge.acmeAuthorization.id ? acmeChallenge.acmeAuthorization : acmeAuthorizationOption" v-for="acmeAuthorizationOption in acmeAuthorizations" :key="acmeAuthorizationOption.id">{{acmeAuthorizationOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.acmeChallenge.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./acme-challenge-update.component.ts">
</script>
