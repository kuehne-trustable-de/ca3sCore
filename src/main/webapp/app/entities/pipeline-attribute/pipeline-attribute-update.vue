<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.pipelineAttribute.home.createOrEditLabel" v-text="$t('ca3SApp.pipelineAttribute.home.createOrEditLabel')">Create or edit a PipelineAttribute</h2>
                <div>
                    <div class="form-group" v-if="pipelineAttribute.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="pipelineAttribute.id" readonly />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipelineAttribute.name')" for="pipeline-attribute-name">Name</label>
                        <input type="text" class="form-control" name="name" id="pipeline-attribute-name"
                            :class="{'valid': !$v.pipelineAttribute.name.$invalid, 'invalid': $v.pipelineAttribute.name.$invalid }" v-model="$v.pipelineAttribute.name.$model"  required/>
                        <div v-if="$v.pipelineAttribute.name.$anyDirty && $v.pipelineAttribute.name.$invalid">
                            <small class="form-text text-danger" v-if="!$v.pipelineAttribute.name.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipelineAttribute.value')" for="pipeline-attribute-value">Value</label>
                        <input type="text" class="form-control" name="value" id="pipeline-attribute-value"
                            :class="{'valid': !$v.pipelineAttribute.value.$invalid, 'invalid': $v.pipelineAttribute.value.$invalid }" v-model="$v.pipelineAttribute.value.$model"  required/>
                        <div v-if="$v.pipelineAttribute.value.$anyDirty && $v.pipelineAttribute.value.$invalid">
                            <small class="form-text text-danger" v-if="!$v.pipelineAttribute.value.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-bind:value="$t('ca3SApp.pipelineAttribute.pipeline')" for="pipeline-attribute-pipeline">Pipeline</label>
                        <select class="form-control" id="pipeline-attribute-pipeline" name="pipeline" v-model="pipelineAttribute.pipeline">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipelineAttribute.pipeline && pipelineOption.id === pipelineAttribute.pipeline.id ? pipelineAttribute.pipeline : pipelineOption" v-for="pipelineOption in pipelines" :key="pipelineOption.id">{{pipelineOption.id}}</option>
                        </select>
                    </div>
                </div>
                <div>
                    <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" id="save-entity" :disabled="$v.pipelineAttribute.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script lang="ts" src="./pipeline-attribute-update.component.ts">
</script>
