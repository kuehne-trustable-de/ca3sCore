<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" >
                <h2 id="ca3SApp.pipeline.home.createOrEditLabel" v-text="$t('ca3SApp.pipeline.home.createOrEditLabel')">Create or edit a Pipeline</h2>
                <div>
                    <!--div class="form-group" v-if="pipeline.id">
                        <label for="id" v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" id="id" name="id"
                               v-model="pipeline.id" readonly />
                    </div-->
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
                    <div class="form-group" v-if="$v.pipeline.type.$model !== 'WEB'">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.urlPart')" for="pipeline-urlPart">Url Part</label>
                        <input type="text" class="form-control" name="urlPart" id="pipeline-urlPart"
                            :class="{'valid': !$v.pipeline.urlPart.$invalid, 'invalid': $v.pipeline.urlPart.$invalid }" v-model="$v.pipeline.urlPart.$model" />
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.description')" for="pipeline-description">Description</label>
                        <textarea type="text" class="form-control" name="description" id="pipeline-description"
                            :class="{'valid': !$v.pipeline.description.$invalid, 'invalid': $v.pipeline.description.$invalid }" v-model="$v.pipeline.description.$model" />
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.caConnector')" for="pipeline-caConnector">Ca Connector</label>
                        <select class="form-control" id="pipeline-caConnector" name="caConnector" v-model="pipeline.caConnectorName">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.caConnectorName && cAConnectorConfigOption.name === pipeline.caConnectorName ? pipeline.caConnectorName : cAConnectorConfigOption.name" v-for="cAConnectorConfigOption in allCertGenerators" :key="cAConnectorConfigOption.id">{{cAConnectorConfigOption.name}}</option>

                            <!--option v-bind:value="pipeline.caConnector && cAConnectorConfigOption.id === pipeline.caConnector.id ? pipeline.caConnector : cAConnectorConfigOption" v-for="cAConnectorConfigOption in cAConnectorConfigs" :key="cAConnectorConfigOption.id">{{cAConnectorConfigOption.id}}</option-->
                        </select>
                    </div>

                    <div class="container">
                        <div class="row">
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.cn.cardinality')" for="pipeline-cn-cardinality">Cardinality 'CN'</label>
                                <select class="form-control" id="pipeline-cn-cardinality" name="pipeline-cn-cardinality" v-model="pipeline.restriction_CN.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>
                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')" for="pipeline-cn-template">Template</label>
                                <input type="text" class="form-control" name="pipeline-cn-template" id="pipeline-cn-template" v-model="pipeline.restriction_CN.contentTemplate" />
                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')" for="pipeline-cn-regExMatch">Regular Expression</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-cn-regExMatch" id="pipeline-cn-regExMatch" v-model="pipeline.restriction_CN.regExMatch" />
                            </div>
                        </div>

                        <div class="row">
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.c.cardinality')" for="pipeline-c-cardinality">Cardinality 'C'</label>
                                <select class="form-control" id="pipeline-c-cardinality" name="pipeline-c-cardinality" v-model="pipeline.restriction_C.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')" for="pipeline-c-template">Template</label>
                                <input type="text" class="form-control" name="pipeline-c-template" id="pipeline-c-template" v-model="pipeline.restriction_C.contentTemplate" />
                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')" for="pipeline-c-regExMatch">Regular Expression</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-c-regExMatch" id="pipeline-c-regExMatch" v-model="pipeline.restriction_C.regExMatch" />
                            </div>
                        </div>


                        <div class="row">
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.o.cardinality')" for="pipeline-o-cardinality">Cardinality 'O'</label>
                                <select class="form-control" id="pipeline-o-cardinality" name="pipeline-o-cardinality" v-model="pipeline.restriction_O.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')" for="pipeline-o-template">Template</label>
                                <input type="text" class="form-control" name="pipeline-o-template" id="pipeline-o-template" v-model="pipeline.restriction_O.contentTemplate" />

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')" for="pipeline-o-regExMatch">Regular Expression</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-o-regExMatch" id="pipeline-o-regExMatch" v-model="pipeline.restriction_O.regExMatch" />

                            </div>
                        </div>


                        <div class="row">
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ou.cardinality')" for="pipeline-ou-cardinality">Cardinality 'OU'</label>
                                <select class="form-control" id="pipeline-ou-cardinality" name="pipeline-ou-cardinality" v-model="pipeline.restriction_OU.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')" for="pipeline-ou-template">Template</label>
                                <input type="text" class="form-control" name="pipeline-ou-template" id="pipeline-ou-template" v-model="pipeline.restriction_OU.contentTemplate" />

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')" for="pipeline-ou-regExMatch">Regular Expression</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-ou-regExMatch" id="pipeline-ou-regExMatch" v-model="pipeline.restriction_OU.regExMatch" />
                            </div>
                        </div>

                        <div class="row">
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.l.cardinality')" for="pipeline-l-cardinality">Cardinality 'L'</label>
                                <select class="form-control" id="pipeline-l-cardinality" name="pipeline-l-cardinality" v-model="pipeline.restriction_L.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')" for="pipeline-l-template">Template</label>
                                <input type="text" class="form-control" name="pipeline-l-template" id="pipeline-l-template" v-model="pipeline.restriction_L.contentTemplate" />

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')" for="pipeline-l-regExMatch">Regular Expression</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-l-regExMatch" id="pipeline-l-regExMatch" v-model="pipeline.restriction_L.regExMatch" />
                            </div>
                        </div>
                        <div class="row">
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.s.cardinality')" for="pipeline-s-cardinality">Cardinality 'S'</label>
                                <select class="form-control" id="pipeline-s-cardinality" name="pipeline-s-cardinality" v-model="pipeline.restriction_S.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')" for="pipeline-s-template">Template</label>
                                <input type="text" class="form-control" name="pipeline-s-template" id="pipeline-s-template" v-model="pipeline.restriction_S.contentTemplate" />

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')" for="pipeline-s-regExMatch">Regular Expression</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-s-regExMatch" id="pipeline-s-regExMatch" v-model="pipeline.restriction_S.regExMatch" />
                            </div>
                        </div>
                        <div class="row">
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.san.cardinality')" for="pipeline-san-cardinality">Cardinality SAN</label>
                                <select class="form-control" id="pipeline-san-cardinality" name="pipeline-san-cardinality" v-model="pipeline.restriction_SAN.cardinalityRestriction">
                                    <option value="NOT_ALLOWED">NOT_ALLOWED</option>
                                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                                    <option value="ONE">ONE</option>
                                    <option value="ZERO_OR_MANY">ZERO_OR_MANY</option>
                                    <option value="ONE_OR_MANY">ONE_OR_MANY</option>
                                </select>

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')" for="pipeline-san-template">Template</label>
                                <input type="text" class="form-control" name="pipeline-san-template" id="pipeline-san-template" v-model="pipeline.restriction_SAN.contentTemplate" />

                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')" for="pipeline-san-regExMatch">Regular Expression</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-san-regExMatch" id="pipeline-san-regExMatch" v-model="pipeline.restriction_SAN.regExMatch" />
                            </div>
                        </div>
                    </div>

                    <!-- Additional Request Attributes -->
                    <div class="container">

                        <div class="row" v-for="(item, index) in pipeline.araRestrictions" :key="index" >
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ara.name')" for="pipeline-ara-name">Name</label>
                                <input type="text" class="form-control" name="pipeline-ara-name" id="pipeline-ara-name" v-model="pipeline.araRestrictions[index].name" v-on:input="alignARAArraySize(index)"/>
                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.template')" for="pipeline-ara-template">Template</label>
                                <input type="text" class="form-control" name="pipeline-ara-template" id="pipeline-ara-template" v-model="pipeline.araRestrictions[index].contentTemplate" />
                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.regExMatch')" for="pipeline-ara-regExMatch">Regular Expression</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-ara-regExMatch" id="pipeline-ara-regExMatch" v-model="pipeline.araRestrictions[index].regExMatch" />
                            </div>
                            <div class="col">
                                <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ara.required')" for="pipeline-ara-required">Required</label>
                                <input type="checkbox" class="form-check-inline" name="pipeline-ara-required" id="pipeline-ara-required" v-model="pipeline.araRestrictions[index].required" />
                            </div>

                        </div>
                    </div>


                    <div v-if="$v.pipeline.type.$model === 'ACME' || $v.pipeline.type.$model === 'SCEP'" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.toPendingOnFailedRestrictions')" for="pipeline-toPendingOnFailedRestrictions">Forward failed req. to 'Pending'</label>
                        <input type="checkbox" class="form-check-inline" name="toPendingOnFailedRestrictions" id="pipeline-toPendingOnFailedRestrictions" v-model="pipeline.acmeConfigItems.toPendingOnFailedRestrictions" />
                    </div>


                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.approvalRequired')" for="pipeline-approvalRequired">Approval Required</label>
                        <input type="checkbox" class="form-check-inline" name="approvalRequired" id="pipeline-approvalRequired"
                            :class="{'valid': !$v.pipeline.approvalRequired.$invalid, 'invalid': $v.pipeline.approvalRequired.$invalid }" v-model="$v.pipeline.approvalRequired.$model" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ipAsSubjectAllowed')" for="pipeline-ipAsSubjectAllowed">IP as Subject Allowed</label>
                        <input type="checkbox" class="form-check-inline" name="ipAsSubjectAllowed" id="pipeline-ipAsSubjectAllowed"
                            :class="{'valid': !$v.pipeline.ipAsSubjectAllowed.$invalid, 'invalid': $v.pipeline.ipAsSubjectAllowed.$invalid }" v-model="$v.pipeline.ipAsSubjectAllowed.$model" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.ipAsSanAllowed')" for="pipeline-ipAsSanAllowed">IP as SAN Allowed</label>
                        <input type="checkbox" class="form-check-inline" name="ipAsSanAllowed" id="pipeline-ipAsSanAllowed"
                            :class="{'valid': !$v.pipeline.ipAsSanAllowed.$invalid, 'invalid': $v.pipeline.ipAsSanAllowed.$invalid }" v-model="$v.pipeline.ipAsSanAllowed.$model" />
                    </div>

<!--
                    <div class="form-group" v-if="$v.pipeline.approvalRequired.$model">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.approvalInfo')" for="pipeline-approvalInfo1">Approval Info</label>

                        <input type="text" class="form-control" name="urlPart" id="pipeline-approvalInfo1"
                            :class="{'valid': !$v.pipeline.approvalInfo1.$invalid, 'invalid': $v.pipeline.approvalInfo1.$invalid }" v-model="$v.pipeline.approvalInfo1.$model" />
                    </div>
-->

                    <div v-if="$v.pipeline.type.$model === 'ACME'" class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.allowChallengeHTTP01')" for="pipeline-allowChallengeHTTP01">Challenge HTTP01</label>
                        <input type="checkbox" class="form-check-inline" name="allowChallengeHTTP01" id="pipeline-allowChallengeHTTP01" v-model="pipeline.acmeConfigItems.allowChallengeHTTP01" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.allowChallengeDNS')" for="pipeline-allowChallengeDNS">Challenge DNS</label>
                        <input type="checkbox" class="form-check-inline" name="allowChallengeDNS" id="pipeline-allowChallengeDNS" v-model="pipeline.acmeConfigItems.allowChallengeDNS" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.allowWildcards')" for="pipeline-allowWildcards">Allow Wildcards</label>
                        <input type="checkbox" class="form-check-inline" name="allowWildcards" id="pipeline-allowWildcards" v-model="pipeline.acmeConfigItems.allowWildcards" />

                    </div>
                    <div v-if="$v.pipeline.type.$model === 'ACME'" class="form-inline">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.checkCAA')" for="pipeline-checkCAA">Challenge CAA</label>
                        <input type="checkbox" class="form-check-inline" name="checkCAA" id="pipeline-checkCAA" v-model="pipeline.acmeConfigItems.checkCAA" />

                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.caNameCAA')" for="pipeline-caNameCAA">CA Name in CAA</label>
                        <input type="text" class="form-control" name="caNameCAA" id="pipeline-caNameCAA" v-model="pipeline.acmeConfigItems.caNameCAA" />

                    </div>




                    <!--div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.pipeline.processInfo')" for="pipeline-processInfo">Process Info</label>
                        <select class="form-control" id="pipeline-processInfo" name="processInfo" v-model="pipeline.processInfo">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="pipeline.processInfo && bPNMProcessInfoOption.id === pipeline.processInfo.id ? pipeline.processInfo : bPNMProcessInfoOption" v-for="bPNMProcessInfoOption in bPNMProcessInfos" :key="bPNMProcessInfoOption.id">{{bPNMProcessInfoOption.id}}</option>
                        </select>
                    </div-->
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

<style>
.container {
	padding-left: 0;
	margin-left: 0;
}
.row {
	padding-top: 5px;
	padding-bottom: 5px;
}
</style>
