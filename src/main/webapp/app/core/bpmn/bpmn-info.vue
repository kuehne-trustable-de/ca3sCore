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
            <div class="col-8">
                <div v-if="bPNMProcessInfo">
                    <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.detail.title')">BPMN Info</span> {{bPNMProcessInfo.id}}</h2>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.name')" >Process name</label> <help-tag role="Admin" target="bpmn.name"/>
                        <input type="text" class="form-control form-check-inline valid" name="bpmn.new.name'" id="bpmn.new.name"
                               required="true"
                               :readOnly="interactionMode == 'TEST'"
                               v-model="bpmnUpload.name" />

                        <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.type')" for="bpmn.new.type">Process type</label>  <help-tag role="Admin" target="bpmn.type"/>
                        <select class="form-control" id="bpmn.new.type" name="bpmn.new.type"
                                :readOnly="interactionMode == 'TEST'"
                                :disabled="interactionMode == 'TEST'"
                                v-model="bpmnUpload.type" >
                            <option value="CERTIFICATE_CREATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.CERTIFICATE_CREATION')" selected="selected"></option>
                            <option value="CERTIFICATE_NOTIFY" v-text="$t('ca3SApp.bPNMProcessInfo.type.CERTIFICATE_NOTIFY')" selected="selected"></option>
                            <option value="CERTIFICATE_REVOCATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.CERTIFICATE_REVOCATION')" selected="selected"></option>
                            <option value="SEND_SMS" v-text="$t('ca3SApp.bPNMProcessInfo.type.SEND_SMS')" selected="selected"></option>
                            <option value="ACME_ACCOUNT_AUTHORIZATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.ACME_ACCOUNT_AUTHORIZATION')" ></option>

                            <!--option value="BATCH" v-text="$t('ca3SApp.bPNMProcessInfo.type.BATCH')" ></option-->
                        </select>

                        <label v-if="interactionMode == 'EDIT'" class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.fileSelectorBPMN')" for="fileSelector">Select a BPMN file</label>  <help-tag role="Admin" target="bpmn.upload"/>
                        <input v-if="interactionMode == 'EDIT'" type="file" id="fileSelector" ref="fileSelector" name="fileSelector" @change="notifyFileChange" />
                        <small class="form-text text-danger" v-if="warningMessage" v-text="$t('entity.validation.required')">{{warningMessage}}</small>

                    </div>

                    <div class="row">
                        <div v-if="bpmnFileUploaded" class="col colContent">
                            <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.version')"></label>
                            <input type="text" class="form-control form-check-inline valid" name="bpmn.new.version'" id="bpmn.new.version"
                                   required="true"
                                   :readOnly="interactionMode == 'TEST'"
                                   v-model="bpmnUploadedVersion" />
                            <small class="form-text text-danger" v-if="showSemVerRegExpFieldWarning(bpmnUploadedVersion)" v-text="$t('ca3SApp.messages.semver.requirement')">
                                version must match RegEx!
                            </small>
                        </div>

                        <div v-if="!bpmnFileUploaded" class="col colContent">
                            <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.version')"></label>
                            <span>{{bPNMProcessInfo.version}}</span>&nbsp;
                            <span v-if="bPNMProcessInfo.lastChange" v-text="$t('ca3SApp.bPNMProcessInfo.createdOn')">created on</span>&nbsp;
                            <span v-if="bPNMProcessInfo.lastChange">{{$d(Date.parse(bPNMProcessInfo.lastChange), 'long') }}</span>
                        </div>

                    </div>

                    <div class="row">
                        <div class="col colContent">
                            <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.author')"></label>
                            <span>{{bPNMProcessInfo.author}}</span>
                        </div>
                    </div>


                    <p></p>
                    <h3 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.attribute.title')"></span></h3>
                    <div class="row" v-for="(bpa, index) in bpmnUpload.bpmnProcessAttributes" :key="index" >
                        <div class="col colContent">
                            <label v-if="index == 0" class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.attribute.name')"></label>
                            <input
                                type="text"
                                class="form-control form-check-inline"
                                autocomplete="false"
                                :name="'ca3SApp.bPNMProcessInfo.attribute.name.' + bpa.name" :id="'ca3SApp.bPNMProcessInfo.attribute.name.' + bpa.name"
                                :readOnly="interactionMode == 'TEST'"
                                v-model="bpa.name"
                                required="true"
                                v-on:input="alignBPAArraySize(index)"/>
                        </div>
                        <div class="col colContent">
                            <label v-if="index == 0" class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.attribute.value')"></label>
                            <input
                                :type="bpa.protectedContent ? 'password':'text'"
                                class="form-control form-check-inline"
                                autocomplete="false"
                                :name="'ca3SApp.bPNMProcessInfo.attribute.value.' + bpa.name" :id="'ca3SApp.bPNMProcessInfo.attribute.value.' + bpa.name"
                                :readOnly="interactionMode == 'TEST'"
                                v-model="bpa.value" />

                        </div>
                        <div class="col colContent">
                            <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.attribute.protected')"></label>
                            <input type="checkbox" class="form-check-inline"
                                   :name="'ca3SApp.bPNMProcessInfo.protected.' + bpa.name" :id="'ca3SApp.bPNMProcessInfo.protected.' + bpa.name"
                                   :readOnly="interactionMode == 'TEST'"
                                   v-model="bpa.protectedContent" />
                        </div>
                    </div>
                    <p></p>

                </div>

                <div v-if="interactionMode == 'TEST'">
                    <h3 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.check.subtitle')">Check BPMN</span></h3>
                </div>

                <div class="form-group" v-if="(interactionMode == 'TEST') && bpmnUpload.type === 'CERTIFICATE_CREATION'">
                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.csrId')" >CSR ID</label>
                    <input type="text" class="form-control form-check-inline valid" name="bpmn.check.csrId" id="bpmn.check.csrId"
                           required="true"
                           v-model="csrId" />
                </div>

                <div class="form-group" v-if="(interactionMode == 'TEST') && ( bpmnUpload.type === 'CERTIFICATE_REVOCATION' || bpmnUpload.type === 'CERTIFICATE_NOTIFY')">
                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.certificateId')" >Certificate ID</label>
                    <input type="text" class="form-control form-check-inline valid" name="bpmn.check.certificateId" id="bpmn.check.certificateId"
                           required="true"
                           v-model="certificateId" />
                </div>

                <div class="form-group" v-if="(interactionMode == 'TEST') && bpmnUpload.type === 'REQUEST_AUTHORIZATION'">
                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.csrId')" >CSR ID</label>
                    <input type="text" class="form-control form-check-inline valid" name="bpmn.check.csrId'" id="bpmn.check.csrId"
                           required="true"
                           v-model="csrId" />
                </div>

                <div class="form-group" v-if="(interactionMode == 'TEST') && bpmnUpload.type === 'SEND_SMS'">
                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.phone')" ></label>
                    <input type="text" class="form-control form-check-inline valid" name="bpmn.check.phone'" id="bpmn.check.phone"
                           required="true"
                           v-model="phone" />
                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.msg')" ></label>
                    <input type="text" class="form-control form-check-inline valid" name="bpmn.check.msg'" id="bpmn.check.msg"
                           required="true"
                           v-model="msg" />
                </div>

                <div class="form-group" v-if="(interactionMode == 'TEST') && bpmnUpload.type === 'ACME_ACCOUNT_AUTHORIZATION'">
                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.mailto')" ></label>
                    <input type="text" class="form-control form-check-inline valid" name="bpmn.check.mailto'" id="bpmn.check.mailto"
                           required="true"
                           v-model="mailto" />
                </div>


                ca3SApp.bPNMProcessInfo.type.ACME_ACCOUNT_AUTHORIZATION

                <div v-if="(interactionMode == 'TEST')" class="form-group">
                    <button type="button"
                            class="btn btn-info"
                            v-on:click="checkBpmn">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.bPNMProcessInfo.check.check')">Check</span>
                    </button>
                    <help-tag role="Admin" target="bpmn.checkBpmn"/>
                </div>

                <div v-if="bpmnCheckResult.status && (interactionMode == 'TEST')">

                    <dl class="row jh-entity-details">
                        <dt>
                            <span v-text="$t('ca3SApp.bPNMProcessInfo.result.status')">Status</span>
                        </dt>
                        <dd>
                            <span>{{bpmnCheckResult.status}}</span>
                        </dd>

                        <dt>
                            <span v-text="$t('ca3SApp.bPNMProcessInfo.result.failureReason')">Failure reason</span>
                        </dt>
                        <dd>
                            <span>{{bpmnCheckResult.failureReason}}</span>
                        </dd>
                    </dl>
                    <dl class="row jh-entity-details">
                        <div v-for="(val, valueIndex) in bpmnCheckResult.responseAttributes" :key="valueIndex">
                            <dt>
                                <span>{{Object.keys(bpmnCheckResult.responseAttributes[valueIndex])[0]}}</span>
                            </dt>
                            <dd>
                                <span>{{bpmnCheckResult.responseAttributes[valueIndex][Object.keys(bpmnCheckResult.responseAttributes[valueIndex])[0]]}}</span>
                            </dd>
                        </div>
                    </dl>

                    <dl class="row jh-entity-details">
                        <div v-for="(val, valueIndex) in bpmnCheckResult.csrAttributes" :key="valueIndex">
                            <dt>
                                <span>{{Object.keys(bpmnCheckResult.csrAttributes[valueIndex])[0]}}</span>
                            </dt>
                            <dd>
                                <span>{{bpmnCheckResult.csrAttributes[valueIndex][Object.keys(bpmnCheckResult.csrAttributes[valueIndex])[0]]}}</span>
                            </dd>
                        </div>
                    </dl>

                </div>


                <form name="editForm" role="form" novalidate>
                    <div>
                        <button type="submit"
                                v-on:click.prevent="previousState()"
                                class="btn btn-info">
                            <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                        </button>

                        <button type="button" id="save" class="btn btn-secondary"
                                v-if="interactionMode == 'EDIT'"
                                :disabled="showSemVerRegExpFieldWarning(bpmnUploadedVersion)"
                                v-on:click="saveBpmn()">
                            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                        </button>

                    </div>
                </form>


                <vue-bpmn v-if="bpmnBlobUrl"
                    :url="bpmnBlobUrl"
                    :options="getOptions()"
                    v-on:error="handleError"
                    v-on:shown="handleShown"
                    v-on:loading="handleLoading"
                ></vue-bpmn>

            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./bpmn-info.component.ts">
</script>
