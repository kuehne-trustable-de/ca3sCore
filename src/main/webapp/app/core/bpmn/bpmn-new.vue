<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.new.title')"></span> </h2>

            <div>
                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.fileSelectorBPMN')" for="fileSelector"></label>  <help-tag role="Admin" target="bpmn.upload"/>
                <input type="file" id="fileSelector" ref="fileSelector" name="fileSelector" @change="notifyFileChange" />

                <small class="form-text text-danger" v-if="warningMessage" v-text="$t('entity.validation.required')">{{warningMessage}}</small>
            </div>


            <div class="form-group" v-if="bpmnFileUploaded">
                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.name')" ></label> <help-tag role="Admin" target="bpmn.name"/>
                <input type="text" class="form-control form-check-inline valid" name="bpmn.new.name'" id="bpmn.new.name"
                       required="required"
                       v-model="bpmnUpload.name" />

                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.version')" ></label> <help-tag role="Admin" target="bpmn.version"/>
                <input type="text" class="form-control form-check-inline valid" name="bpmn.new.version'" id="bpmn.new.version"
                       required="required"
                       v-model="bpmnUpload.version" />
                <small class="form-text text-danger" v-if="showSemVerRegExpFieldWarning(bpmnUpload.version)" v-text="$t('ca3SApp.messages.semver.requirement')"></small>

                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.type')" for="bpmn.new.type"></label>  <help-tag role="Admin" target="bpmn.type"/>
                <select class="form-control" id="bpmn.new.type" name="bpmn.new.type" v-model="bpmnUpload.type" >
                    <option value="CERTIFICATE_CREATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.CERTIFICATE_CREATION')" selected="selected"></option>
                    <option value="CERTIFICATE_NOTIFY" v-text="$t('ca3SApp.bPNMProcessInfo.type.CERTIFICATE_NOTIFY')" selected="selected"></option>
                    <option value="CERTIFICATE_REVOCATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.CERTIFICATE_REVOCATION')" selected="selected"></option>
                    <option value="SEND_SMS" v-text="$t('ca3SApp.bPNMProcessInfo.type.SEND_SMS')" selected="selected"></option>
                    <option value="ACME_ACCOUNT_AUTHORIZATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.ACME_ACCOUNT_AUTHORIZATION')" ></option>

                    <!--option value="BATCH" v-text="$t('ca3SApp.bPNMProcessInfo.type.TIMED')" >TIMED</option-->
                </select>
            </div>

            <form name="editForm" role="form" novalidate>
                <div>
                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"></span>
                    </button>

                    <button v-if="bpmnFileUploaded" type="button" id="save" class="btn btn-secondary"
                            :disabled="showSemVerRegExpFieldWarning(bpmnUpload.version)"
                            v-on:click="saveBpmn()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')"></span>
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
