<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="bPNMProcessInfo">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.detail.title')">BPMN Info</span> {{bPNMProcessInfo.id}}</h2>

                <div class="form-group">
                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.name')" >Process name</label> <help-tag target="bpmn.new.name"/>
                    <input type="text" class="form-control form-check-inline valid" name="bpmn.new.name'" id="bpmn.new.name"
                           required="true"
                           v-model="bpmnUpload.name" />

                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.type')" for="bpmn.new.type">Process type</label>  <help-tag target="bpmn.new.type"/>
                    <select class="form-control" id="bpmn.new.type" name="bpmn.new.type" v-model="bpmnUpload.type" >
                        <option value="CERTIFICATE_CREATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.CERTIFICATE_CREATION')" selected="selected">CERTIFICATE_CREATION</option>
                        <option value="CERTIFICATE_REVOCATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.CERTIFICATE_REVOCATION')" selected="selected">CERTIFICATE_REVOCATION</option>
                        <option value="REQUEST_AUTHORIZATION" v-text="$t('ca3SApp.bPNMProcessInfo.type.REQUEST_AUTHORIZATION')" >REQUEST_AUTHORIZATION</option>
                        <option value="BATCH" v-text="$t('ca3SApp.bPNMProcessInfo.type.BATCH')" >BATCH</option>
                    </select>

                    <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.new.fileSelectorBPMN')" for="fileSelector">Select a BPMN file</label>
                    <input type="file" id="fileSelector" ref="fileSelector" name="fileSelector" @change="notifyFileChange" />

                    <small class="form-text text-danger" v-if="warningMessage" v-text="$t('entity.validation.required')">{{warningMessage}}</small>

                </div>

                <dl class="row jh-entity-details">

                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.version')">Version</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.version}}</span>&nbsp;
                        <span v-if="bPNMProcessInfo.lastChange" v-text="$t('ca3SApp.bPNMProcessInfo.createdOn')">created on</span>&nbsp;
                        <span v-if="bPNMProcessInfo.lastChange">{{$d(Date.parse(bPNMProcessInfo.lastChange), 'long') }}</span>
                    </dd>

                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.author')">Author</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.author}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.lastChange')">Last Change</span>
                    </dt>
                    <dd>
                        <span v-if="bPNMProcessInfo.lastChange">{{$d(Date.parse(bPNMProcessInfo.lastChange), 'long') }}</span>
                    </dd>
                    <!--dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.signatureBase64')">Signature Base 64</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.signatureBase64}}</span>
                    </dd-->
                </dl>
            </div>
            <!--form name="editForm" role="form" novalidate>
                <div>
                    <button v-if="bpmnFileUploaded" type="button" id="save" class="btn btn-secondary" v-on:click="saveBpmn()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>

            </form-->


            <div v-if="!bpmnFileUploaded">
                <h3 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.check.subtitle')">Check BPMN</span></h3>
            </div>

            <div class="form-group" v-if="!bpmnFileUploaded && bpmnUpload.type === 'CERTIFICATE_CREATION'">
                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.csrId')" >CSR ID</label>
                <input type="text" class="form-control form-check-inline valid" name="bpmn.check.csrId" id="bpmn.check.csrId"
                       required="true"
                       v-model="csrId" />
            </div>

            <div class="form-group" v-if="!bpmnFileUploaded && bpmnUpload.type === 'CERTIFICATE_REVOCATION'">
                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.certificateId')" >Certificate ID</label>
                <input type="text" class="form-control form-check-inline valid" name="bpmn.check.certificateId" id="bpmn.check.certificateId"
                       required="true"
                       v-model="certificateId" />
            </div>

            <div class="form-group" v-if="!bpmnFileUploaded && bpmnUpload.type === 'REQUEST_AUTHORIZATION'">
                <label class="form-control-label" v-text="$t('ca3SApp.bPNMProcessInfo.check.csrId')" >CSR ID</label>
                <input type="text" class="form-control form-check-inline valid" name="bpmn.new.name'" id="bpmn.check.csrId"
                       required="true"
                       v-model="csrId" />
            </div>

            <div v-if="!bpmnFileUploaded" class="form-group">
                <button type="button"
                        class="btn btn-info"
                        v-on:click="checkBpmn">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.bPNMProcessInfo.check.check')">Check</span>
                </button>
            </div>

            <div v-if="bpmnCheckResult.status">

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

                    <button type="button" id="save" class="btn btn-secondary" v-on:click="saveBpmn()">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
                    </button>

                </div>
            </form>


            <vue-bpmn
                :url="getBpmnUrl()"
                :options="getOptions()"
                v-on:error="handleError"
                v-on:shown="handleShown"
                v-on:loading="handleLoading"
            ></vue-bpmn>

        </div>
    </div>
</template>

<script lang="ts" src="./bpmn-info.component.ts">
</script>
