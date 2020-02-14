<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.acmeOrder.home.createOrEditLabel" v-text="$t('ca3SApp.acmeOrder.home.createOrEditLabel')">Create or edit a AcmeOrder</h2>
                <div>
                    <div class="form-group" v-if="acmeOrder.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="acmeOrder.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeOrder.orderId')" for="acme-order-orderId">Order Id</label>
                        <input type="number" class="form-control" name="orderId" id="acme-order-orderId"
                            :class="{'valid': !$v.acmeOrder.orderId.$invalid, 'invalid': $v.acmeOrder.orderId.$invalid }" v-model.number="$v.acmeOrder.orderId.$model"  required/>
                        <div v-if="$v.acmeOrder.orderId.$anyDirty && $v.acmeOrder.orderId.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeOrder.orderId.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.acmeOrder.orderId.number" v-text="$t('entity.validation.number')">
                                This field should be a number.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeOrder.status')" for="acme-order-status">Status</label>
                        <select class="form-control" name="status" :class="{'valid': !$v.acmeOrder.status.$invalid, 'invalid': $v.acmeOrder.status.$invalid }" v-model="$v.acmeOrder.status.$model" id="acme-order-status"  required>
                            <option value="PENDING" v-bind:label="$t('ca3SApp.AcmeOrderStatus.PENDING')">PENDING</option>
                            <option value="READY" v-bind:label="$t('ca3SApp.AcmeOrderStatus.READY')">READY</option>
                            <option value="PROCESSING" v-bind:label="$t('ca3SApp.AcmeOrderStatus.PROCESSING')">PROCESSING</option>
                            <option value="VALID" v-bind:label="$t('ca3SApp.AcmeOrderStatus.VALID')">VALID</option>
                            <option value="INVALID" v-bind:label="$t('ca3SApp.AcmeOrderStatus.INVALID')">INVALID</option>
                        </select>
                        <div v-if="$v.acmeOrder.status.$anyDirty && $v.acmeOrder.status.$invalid">
                            <small class="form-text text-danger" v-if="!$v.acmeOrder.status.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeOrder.expires')" for="acme-order-expires">Expires</label>
                        <div class="d-flex">
                            <input id="acme-order-expires" type="datetime-local" class="form-control" name="expires" :class="{'valid': !$v.acmeOrder.expires.$invalid, 'invalid': $v.acmeOrder.expires.$invalid }"
                            
                            :value="convertDateTimeFromServer($v.acmeOrder.expires.$model)"
                            @change="updateInstantField('expires', $event)"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeOrder.notBefore')" for="acme-order-notBefore">Not Before</label>
                        <div class="d-flex">
                            <input id="acme-order-notBefore" type="datetime-local" class="form-control" name="notBefore" :class="{'valid': !$v.acmeOrder.notBefore.$invalid, 'invalid': $v.acmeOrder.notBefore.$invalid }"
                            
                            :value="convertDateTimeFromServer($v.acmeOrder.notBefore.$model)"
                            @change="updateInstantField('notBefore', $event)"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeOrder.notAfter')" for="acme-order-notAfter">Not After</label>
                        <div class="d-flex">
                            <input id="acme-order-notAfter" type="datetime-local" class="form-control" name="notAfter" :class="{'valid': !$v.acmeOrder.notAfter.$invalid, 'invalid': $v.acmeOrder.notAfter.$invalid }"
                            
                            :value="convertDateTimeFromServer($v.acmeOrder.notAfter.$model)"
                            @change="updateInstantField('notAfter', $event)"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeOrder.error')" for="acme-order-error">Error</label>
                        <input type="text" class="form-control" name="error" id="acme-order-error"
                            :class="{'valid': !$v.acmeOrder.error.$invalid, 'invalid': $v.acmeOrder.error.$invalid }" v-model="$v.acmeOrder.error.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeOrder.finalizeUrl')" for="acme-order-finalizeUrl">Finalize Url</label>
                        <input type="text" class="form-control" name="finalizeUrl" id="acme-order-finalizeUrl"
                            :class="{'valid': !$v.acmeOrder.finalizeUrl.$invalid, 'invalid': $v.acmeOrder.finalizeUrl.$invalid }" v-model="$v.acmeOrder.finalizeUrl.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.acmeOrder.certificateUrl')" for="acme-order-certificateUrl">Certificate Url</label>
                        <input type="text" class="form-control" name="certificateUrl" id="acme-order-certificateUrl"
                            :class="{'valid': !$v.acmeOrder.certificateUrl.$invalid, 'invalid': $v.acmeOrder.certificateUrl.$invalid }" v-model="$v.acmeOrder.certificateUrl.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.acmeOrder.csr')" for="acme-order-csr">Csr</label>
                        <select class="form-control" id="acme-order-csr" name="csr" v-model="acmeOrder.csr">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="acmeOrder.csr && cSROption.id === acmeOrder.csr.id ? acmeOrder.csr : cSROption" v-for="cSROption in cSRS" :key="cSROption.id">{{cSROption.id}}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.acmeOrder.certificate')" for="acme-order-certificate">Certificate</label>
                        <select class="form-control" id="acme-order-certificate" name="certificate" v-model="acmeOrder.certificate">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="acmeOrder.certificate && certificateOption.id === acmeOrder.certificate.id ? acmeOrder.certificate : certificateOption" v-for="certificateOption in certificates" :key="certificateOption.id">{{certificateOption.id}}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.acmeOrder.account')" for="acme-order-account">Account</label>
                        <select class="form-control" id="acme-order-account" name="account" v-model="acmeOrder.account">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="acmeOrder.account && aCMEAccountOption.id === acmeOrder.account.id ? acmeOrder.account : aCMEAccountOption" v-for="aCMEAccountOption in aCMEAccounts" :key="aCMEAccountOption.id">{{aCMEAccountOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.acmeOrder.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./acme-order-update.component.ts">
</script>
