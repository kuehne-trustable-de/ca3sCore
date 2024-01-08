<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.requestProxyConfig.home.createOrEditLabel" v-text="$t('ca3SApp.requestProxyConfig.home.createOrEditLabel')">Create or edit a RequestProxyConfig</h2>
                <div>
                    <div class="form-group" v-if="requestProxyConfig.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>  <help-tag role="Admin" target="requestProxyConfig.id"/>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="requestProxyConfig.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.requestProxyConfig.name')" for="request-proxy-config-name">Name</label>  <help-tag role="Admin" target="requestProxyConfig.name"/>
                        <input type="text" class="form-control" name="name" id="request-proxy-config-name"
                            :class="{'valid': !$v.requestProxyConfig.name.$invalid, 'invalid': $v.requestProxyConfig.name.$invalid }" v-model="$v.requestProxyConfig.name.$model"  required/>
                        <div v-if="$v.requestProxyConfig.name.$anyDirty && $v.requestProxyConfig.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.requestProxyConfig.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.requestProxyConfig.requestProxyUrl')" for="request-proxy-config-requestProxyUrl">Request Proxy Url</label>  <help-tag role="Admin" target="requestProxyConfig.proxy-url"/>
                        <input type="text" class="form-control" name="requestProxyUrl" id="request-proxy-config-requestProxyUrl"
                            :class="{'valid': !$v.requestProxyConfig.requestProxyUrl.$invalid, 'invalid': $v.requestProxyConfig.requestProxyUrl.$invalid }" v-model="$v.requestProxyConfig.requestProxyUrl.$model"  required/>
                        <div v-if="$v.requestProxyConfig.requestProxyUrl.$anyDirty && $v.requestProxyConfig.requestProxyUrl.$invalid">
                            <small class="form-text text-danger" v-if="!$v.requestProxyConfig.requestProxyUrl.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.requestProxyConfig.active')" for="request-proxy-config-active">Active</label>  <help-tag role="Admin" target="requestProxyConfig.active"/>
                        <input type="checkbox" class="form-check" name="active" id="request-proxy-config-active"
                            :class="{'valid': !$v.requestProxyConfig.active.$invalid, 'invalid': $v.requestProxyConfig.active.$invalid }" v-model="$v.requestProxyConfig.active.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.requestProxyConfig.secret')" for="request-proxy-config-secret">Secret</label>  <help-tag role="Admin" target="requestProxyConfig.secret"/>
                        <input type="text" class="form-control" name="request-proxy-config-secret" id="request-proxy-config-secret"
                               :class="{'valid': !$v.requestProxyConfig.plainSecret.$invalid, 'invalid': $v.requestProxyConfig.plainSecret.$invalid }" v-model="$v.requestProxyConfig.plainSecret.$model"  required/>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.requestProxyConfig.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./request-proxy-config-update.component.ts">
</script>
