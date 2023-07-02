<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.new.title')">BPMN Info</span> </h2>

            <div>
                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.fileSelectorBPMN')" for="fileSelector">Select a BPMN file</label>
                <input type="file" id="fileSelector" ref="fileSelector" name="fileSelector" @change="notifyFileChange" />

                <small class="form-text text-danger" v-if="warningMessage" v-text="$t('entity.validation.required')">{{warningMessage}}</small>
            </div>


            <div class="form-group" v-if="bpmnFileUploaded">
                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.name')" >Process name</label> <help-tag target="bpmn.new.name"/>
                <input type="text" class="form-control form-check-inline valid" name="bpmn.new.name'" id="bpmn.new.name"
                       required="true"
                       v-model="bpmnUpload.name" />

                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.type')" for="bpmn.new.type">Process type</label>  <help-tag target="bpmn.new.type"/>
                <select class="form-control" id="bpmn.new.type" name="bpmn.new.type" v-model="bpmnUpload.type" >
                    <option value="CA_INVOCATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.CA_INVOCATION')" selected="selected">CA_INVOCATION</option>
                    <option value="REQUEST_AUTHORIZATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.REQUEST_AUTHORIZATION')" >REQUEST_AUTHORIZATION</option>
                    <option value="BATCH" v-text="$t('ca3SApp.bPNMProcessInfo.type.BATCH')" >BATCH</option>
                </select>

            </div>

            <form name="editForm" role="form" novalidate>
                <div>
                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                    </button>

                    <button v-if="bpmnFileUploaded" type="button" id="save" class="btn btn-secondary" v-on:click="saveBpmn()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>

            </form>

            <vue-bpmn v-if="bpmnFileUploaded"
                      :url="bpmnUrl"
                      :options="getOptions()"
                      v-on:error="handleError"
                      v-on:shown="handleShown"
                      v-on:loading="handleLoading"
            ></vue-bpmn>

        </div>
    </div>
</template>

<script lang="ts" src="./bpmn-new.component.ts">
</script>
