<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.pipeline.home.createOrEditLabel" v-text="$t('ca3SApp.pipeline.home.createOrEditLabel')">Create or edit a Pipeline</h2>
                <div>
                    <div class="form-group" v-if="pipeline.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="pipeline.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.name')" for="pipeline-name">Name</label>
                        <input type="text" class="form-control" name="name" id="pipeline-name"
                            :class="{'valid': !$v.pipeline.name.$invalid, 'invalid': $v.pipeline.name.$invalid }" v-model="$v.pipeline.name.$model"  required/>
                        <div v-if="$v.pipeline.name.$anyDirty && $v.pipeline.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.pipeline.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.type')" for="pipeline-type">Type</label>
                        <select class="form-control" name="type" :class="{'valid': !$v.pipeline.type.$invalid, 'invalid': $v.pipeline.type.$invalid }" v-model="$v.pipeline.type.$model" id="pipeline-type"  required>
                            <option value="ACME" v-bind:label="$t('ca3SApp.PipelineType.ACME')">ACME</option>
                            <option value="SCEP" v-bind:label="$t('ca3SApp.PipelineType.SCEP')">SCEP</option>
                            <option value="WEB" v-bind:label="$t('ca3SApp.PipelineType.WEB')">WEB</option>
                        </select>
                        <div v-if="$v.pipeline.type.$anyDirty && $v.pipeline.type.$invalid">
                            <small class="form-text text-danger" v-if="!$v.pipeline.type.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.urlPart')" for="pipeline-urlPart">Url Part</label>
                        <input type="text" class="form-control" name="urlPart" id="pipeline-urlPart"
                            :class="{'valid': !$v.pipeline.urlPart.$invalid, 'invalid': $v.pipeline.urlPart.$invalid }" v-model="$v.pipeline.urlPart.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.decription')" for="pipeline-decription">Decription</label>
                        <input type="text" class="form-control" name="decription" id="pipeline-decription"
                            :class="{'valid': !$v.pipeline.decription.$invalid, 'invalid': $v.pipeline.decription.$invalid }" v-model="$v.pipeline.decription.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.caConnector')" for="pipeline-caConnector">Ca Connector</label>
                        <select class="form-control" id="pipeline-caConnector" name="caConnector" v-model="pipeline.caConnector">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.caConnector && cAConnectorConfigOption.id === pipeline.caConnector.id ? pipeline.caConnector : cAConnectorConfigOption" v-for="cAConnectorConfigOption in allCertGenerators" :key="cAConnectorConfigOption.id">{{cAConnectorConfigOption.name}}</option>

                            <!--option v-bind:value="pipeline.caConnector && cAConnectorConfigOption.id === pipeline.caConnector.id ? pipeline.caConnector : cAConnectorConfigOption" v-for="cAConnectorConfigOption in cAConnectorConfigs" :key="cAConnectorConfigOption.id">{{cAConnectorConfigOption.id}}</option-->
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.processInfo')" for="pipeline-processInfo">Process Info</label>
                        <select class="form-control" id="pipeline-processInfo" name="processInfo" v-model="pipeline.processInfo">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.processInfo && bPNMProcessInfoOption.id === pipeline.processInfo.id ? pipeline.processInfo : bPNMProcessInfoOption" v-for="bPNMProcessInfoOption in bPNMProcessInfos" :key="bPNMProcessInfoOption.id">{{bPNMProcessInfoOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.pipeline.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./pipeline-update.component.ts">
</script>
