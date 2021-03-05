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

		<div class="row justify-content-center" v-cloak @drop.prevent="catchDroppedFile" @dragover.prevent>
			<div class="col-8" >
				<form name="editForm" role="form" novalidate >

					<h2 class="jh-entity-heading">
                        <span v-if="(authenticated === false) && (creationMode === 'CSR_AVAILABLE')" v-text="$t('pkcsxx.subtitle.check.csr')">Check</span>
                        <span v-else-if="creationMode === 'CSR_AVAILABLE'" v-text="$t('pkcsxx.subtitle.csr')">Upload</span>
                        <span v-else-if="creationMode === 'COMMANDLINE_TOOL'" v-text="$t('pkcsxx.subtitle.tooling')">Upload</span>
                        <span v-else-if="creationMode === 'SERVERSIDE_KEY_CREATION'" v-text="$t('pkcsxx.subtitle.serverside')">Upload</span>
                    </h2>
					<div>
						<div class="form-group" v-if="authenticated">
							<label class="form-control-label" v-text="$t('pkcsxx.upload.pipeline')" for="pkcsxx-pipeline">Pipeline</label> <help-tag target="pkcsxx.upload.pipeline"/>
							<!--select class="form-control" id="pkcsxx-pipeline" name="pkcsxx-pipeline" v-model="$v.upload.pipelineId.$model" required v-on:change="updatePipelineRestrictions($event)"-->
							<select class="form-control" id="pkcsxx-pipeline" name="pkcsxx-pipeline" v-model="upload.pipelineId" required v-on:change="updatePipelineRestrictions($event)">
								<option v-bind:value="upload && webPipeline.id === upload.pipelineId ? upload.pipelineId : webPipeline.id" v-for="webPipeline in allWebPipelines" :key="webPipeline.id">{{webPipeline.name}}</option>
							</select>

							<div class="readonly_comment">{{selectPipelineInfo}}</div>
						</div>

						<div class="form-group">
							<label class="form-control-label" v-text="$t('pkcsxx.upload.creationMode.selection')" for="pkcsxx-key-creation">Creation mode</label> <help-tag target="pkcsxx.upload.creationMode.selection"/>
							<select class="form-control" id="pkcsxx-key-creation" name="pkcsxx-key-creation" v-model="creationMode">

								<option value="CSR_AVAILABLE" v-text="$t('pkcsxx.upload.creationMode.csrAvailable')" selected="selected">csrAvailable</option>
                                <option v-if="preferences.serverSideKeyCreationAllowed && authenticated" value="SERVERSIDE_KEY_CREATION" v-text="$t('pkcsxx.upload.creationMode.serversideKeyCreation')">serverside key creation</option>
								<option value="COMMANDLINE_TOOL" v-text="$t('pkcsxx.upload.creationMode.commandLineTool')">csr generation command line</option>
							</select>
						</div>

						<div class="form-group" v-if="creationMode === 'COMMANDLINE_TOOL'">
							<label class="form-control-label" v-text="$t('pkcsxx.upload.creationTool.selection')" for="pkcsxx-key-tool">Creation tool</label>  <help-tag target="pkcsxx.upload.creationTool.selection"/>
							<select class="form-control" id="pkcsxx-key-tool" name="pkcsxx-key-tool" v-model="creationTool" v-on:change="updateCmdLine()">
								<option value="keytool" v-text="$t('pkcsxx.upload.creationTool.keytool')" selected="selected">keytool</option>
                                <option value="openssl_ge_1.1.1" v-text="$t('pkcsxx.upload.creationTool.openssl_ge_1.1.1')" >openssl (ver. >= 1.1.1)</option>
                                <option value="openssl" v-text="$t('pkcsxx.upload.creationTool.openssl')" >openssl</option>
							</select>
						</div>

						<div class="form-group" v-if="(creationMode === 'COMMANDLINE_TOOL') || (creationMode === 'SERVERSIDE_KEY_CREATION')" >
							<label class="form-control-label" v-text="$t('pkcsxx.upload.certificateParams')" >certificateParams</label>

							<div class="row">
                                <!-- update counter is a hack to re-render the complex list -->
								<div class="col" :key="updateCounter">
									<label class="form-control-label" v-text="$t('pkcsxx.upload.key-length')" for="pkcsxx.upload.key-length">Key length</label>
								</div>
								<div class="col colContent">
									<select class="form-control w-50" id="pkcsxx.upload.key-length" name="pkcsxx.upload.key-length" v-model="keyAlgoLength" v-on:change="updateCmdLine()">
										<option value="RSA_2048" selected="selected">RSA_2048</option>
										<option value="RSA_4096">RSA_4096</option>
									</select>
								</div>
							</div>


							<div class="row" v-for="(rr, index) in rdnRestrictions" :key="index" >
								<div class="col ">
									<label class="form-control-label" v-text="$t('pkcsxx.upload.' + rr.name)" :for="'pkcsxx.upload.' + rr.name">{{rr.name}}</label>
								</div>
								<div class="col colContent">
                                    <input v-for="(val, valueIndex) in upload.certificateAttributes[index].values" :key="valueIndex"
										type="text" class="form-control form-check-inline valid" :name="'pkcsxx.upload.' + rr.name" :id="'pkcsxx.upload.' + rr.name"
										v-model="upload.certificateAttributes[index].values[valueIndex]"
										:readonly="rr.readOnly"
										:required="rr.required"
										v-on:input="alignRDNArraySize(index, valueIndex)"/>
								</div>
							</div>
						</div>

						<!-- Additional Request Attributes -->
						<div class="form-group" v-if="(araRestrictions.length > 0 ) && ( creationMode === 'CSR_AVAILABLE' || creationMode === 'SERVERSIDE_KEY_CREATION')">
							<label class="form-control-label" v-text="$t('pkcsxx.upload.requestParams')" >certificateParams</label>

							<div class="row" v-for="(item, index) in araRestrictions" :key="index" >
								<div class="col">
									<label class="form-control-label" :for="'pkcsxx.upload.ara.' + item.name">{{item.name}}</label>
								</div>
								<div class="col colContent">
									<input type="text" class="form-control form-check-inline valid" :name="'pkcsxx.upload.ara.' + item.name" :id="'pkcsxx.upload.ara.' + item.name"
										:readonly="item.readOnly"
										:required="item.required"
										v-model="upload.arAttributes[index].values[0]" />
								</div>
							</div>
						</div>

						<div class="form-group" v-if="creationMode === 'SERVERSIDE_KEY_CREATION'">
							<div class="row" >
								<div class="col">
									<label class="form-control-label" v-text="$t('pkcsxx.upload.serversideCreation.secret')" for="upload-secret">Secret</label>
								</div>
								<div class="col colContent">
									<input type="password" class="form-control form-check-inline valid w-50" name="upload-secret" id="upload-secret"
										autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                        required="true"
										v-model="secret" />
								</div>
							</div>
							<div class="row" >
								<div class="col">
									<label class="form-control-label" v-text="$t('pkcsxx.upload.serversideCreation.repeat')" for="upload-secret-repeat">Repeat</label>
								</div>
								<div class="col colContent">
									<input type="password" class="form-control form-check-inline valid w-50" name="upload-secret-repeat" id="upload-secret-repeat"
										autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                                           required="true"
                                           v-model="secretRepeat" />
                                    <small class="form-text text-danger" v-if="secret !== secretRepeat" v-text="$t('entity.validation.secretRepeat')">
                                        This field should be a valid port number.
                                    </small>
								</div>
							</div>
						</div>


                        <div class="row wrap" v-if="creationMode === 'COMMANDLINE_TOOL' && cmdline0Required">
                            <div class="col ">
                                <label class="form-control-label" v-text="$t('pkcsxx.upload.creationTool.cmdline0')" for="pkcsxx-reqConf">Command line</label> <help-tag target="pkcsxx.upload.creationTool.cmdline"/>
                            </div>
                            <div class="col colContent">
                                <textarea class="form-control cmd0-content" name="pkcsxx-cmdline0" id="pkcsxx-cmdline0"
                                          autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly v-model="cmdline0" />
                                <CopyClipboardButton contentElementId="cmdline0"/>
                            </div>
                        </div>
                        <div v-if="creationMode === 'COMMANDLINE_TOOL' && cmdline0Required">
                            <label></label>
                        </div>

                        <div class="row wrap" v-if="creationMode === 'COMMANDLINE_TOOL' && reqConfRequired">
                            <div class="col ">
                                <label class="form-control-label" v-text="$t('pkcsxx.upload.creationTool.req.conf')" for="pkcsxx-reqConf">Request config file</label> <help-tag target="pkcsxx.upload.creationTool.req.conf"/>
                            </div>
                            <div class="col colContent">
                                <textarea class="form-control cmd-content" name="pkcsxx-reqConf" id="pkcsxx-reqConf"
                                          autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly v-model="reqConf" />
                                <CopyClipboardButton contentElementId="reqConf"/>
                            </div>
                        </div>
                        <div v-if="creationMode === 'COMMANDLINE_TOOL' && reqConfRequired">
                            <label></label>
                        </div>

                        <div class="row wrap" v-if="creationMode === 'COMMANDLINE_TOOL'">
                           <div class="col ">
                               <label class="form-control-label" v-text="$t('pkcsxx.upload.creationTool.cmdline')" for="pkcsxx-cmdline">Command line</label>   <help-tag target="pkcsxx.upload.creationTool.cmdline"/>
                           </div>
                           <div class="col colContent ">
                                <textarea class="form-control cmd-content" name="pkcsxx-cmdline" id="pkcsxx-cmdline"
                                      autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly v-model="cmdline" />
                               <CopyClipboardButton contentElementId="pkcsxx-cmdline"/>
                           </div>
						</div>

						<div class="form-group" v-if="(creationMode === 'CSR_AVAILABLE') " >
							<!--label class="form-control-label" v-text="$t('pkcsxx.upload.content')" for="upload-content">Content</label-->
							<div>
								<label v-if="(creationMode === 'CSR_AVAILABLE') "
									class="form-control-label" v-text="$t('pkcsxx.upload.fileSelectorCSR')" for="fileSelector">Select a CSR</label>
								<label v-if="(creationMode === 'COMMANDLINE_TOOL') || (creationMode === 'SERVERSIDE_KEY_CREATION')"
									class="form-control-label" v-text="$t('pkcsxx.upload.fileSelectorCertificate')" for="fileSelector">Select a certificate</label>
								<input type="file" id="fileSelector" ref="fileSelector" name="fileSelector" @change="notifyFileChange" />
							</div>
							<textarea class="form-control pem-content draggable" name="content" id="upload-content"
								autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
								v-model="upload.content"  required
								v-on:input="notifyChange"/>
	<!--
									v-model="$v.upload.content.$model"  required
	-->

							<!--div v-if="$v.upload.content.$anyDirty && $v.upload.content.$invalid">
								<small class="form-text text-danger" v-if="!$v.upload.content.required" v-text="$t('entity.validation.required')">
									This field is required.
								</small>
							</div-->
						</div>

						<div class="form-group" v-if="showCSRRelatedArea()">
							<label class="form-control-label" v-text="$t('pkcsxx.upload.requestorComment')" for="upload-requestor-comment">Requestor Comment</label>
							<textarea type="text" class="form-control" name="requestor-comment" id="upload-requestor-comment"
								autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
								v-model="upload.requestorcomment" v-on:input="notifyChange"/>
								<!-- v-model="$v.upload.requestorcomment.$model" v-on:input="notifyChange"/ -->
						</div>

						<div class="form-group" v-if="precheckResponse.dataType === 'CONTAINER_REQUIRING_PASSPHRASE'">
							<label class="form-control-label" v-text="$t('pkcsxx.upload.passphrase')" for="upload-passphrase">Passphrase</label>
							<input type="text" class="form-control" name="passphrase" id="upload-passphrase"
								autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
								v-model="upload.passphrase" v-on:input="notifyChange"/>
								<!-- v-model="$v.upload.passphrase.$model" v-on:input="notifyChange"/-->
						</div>

						<dl class="row jh-entity-details" v-if="responseStatus > 0">
							<dt>
								<span v-text="$t('pkcsxx.upload.result.error')">Error</span>
							</dt>
							<dd>
								<span v-if="responseStatus === 400" v-text="$t('pkcsxx.upload.result.content.not.parseable')">Ccontent not parseable</span>
								<span v-else-if="responseStatus === 409" v-text="$t('pkcsxx.upload.result.certificate.already.exists')">Certificate already exists</span>
								<span v-else-if="responseStatus === 201" v-text="$t('pkcsxx.upload.result.upload.successful')">Upload successful</span>
								<span v-else v-text="$t('pkcsxx.upload.result.general error')">General error</span>
							</dd>
						</dl>

						<dl class="row jh-entity-details" v-if="isChecked === true && precheckResponse.dataType === 'UNKNOWN'">
							<dt>
								<span v-text="$t('pkcsxx.upload.result.label')">Result</span>
							</dt>
							<dd>
								<span v-text="$t('pkcsxx.upload.result.unknown')">Unknown content</span>
							</dd>
						</dl>

						<dl class="row jh-entity-details" v-if="isChecked === true && precheckResponse.dataType === 'CSR'">
							<dt>
								<span v-text="$t('pkcsxx.upload.type')">Result</span>
							</dt>
							<dd>
								<span><div v-if="precheckResponse.p10Holder.csrvalid === false">invalid </div>{{precheckResponse.dataType}}</span>
							</dd>

							<dt v-if="precheckResponse.csrPublicKeyPresentInDB === true">
								<span v-text="$t('pkcsxx.upload.warning.label')">Warning</span>
							</dt>
							<dd v-if="precheckResponse.csrPublicKeyPresentInDB === true">
								<span class="text-danger" v-text="$t('pkcsxx.upload.warning.publicKeyPresent')">Public key already in use</span>
							</dd>

							<dt>
								<span v-text="$t('pkcsxx.upload.subject')">Subject</span>
							</dt>
							<dd>
								<span>{{precheckResponse.p10Holder.subject}}</span>
							</dd>

							<dt>
								<span v-text="$t('pkcsxx.upload.algoname')">Build with</span>
							</dt>
							<dd>
								<span>{{precheckResponse.p10Holder.signingAlgorithmName}}</span>
							</dd>
							<dt v-if="precheckResponse.p10Holder.sans.length > 0">
								<span v-text="$t('pkcsxx.upload.sans')">Subject alternative names</span>
							</dt>
							<dd v-if="precheckResponse.p10Holder.sans.length > 0">
								<ul>
									<li v-for="san in precheckResponse.p10Holder.sans" :key="san">{{san}}</li>
								</ul>
							</dd>
						</dl>

						<dl class="row jh-entity-details" v-if="isChecked === true && precheckResponse.dataType === 'X509_CERTIFICATE'">
							<dt>
								<span v-text="$t('pkcsxx.upload.type')">Result</span>
							</dt>
							<dd>
								<span v-if="precheckResponse.certificates[0].certificatePresentInDB" v-text="$t('pkcsxx.upload.result.certificateInDatabase')">Certificate (already in database)</span>
								<span v-else v-text="$t('pkcsxx.upload.result.certificate')">Certificate</span>
							</dd>

							<dt>
								<span v-text="$t('pkcsxx.upload.subject')">Subject</span>
							</dt>
							<dd>
								<span>{{precheckResponse.certificates[0].subject}}</span>
							</dd>

							<dt>
								<span v-text="$t('pkcsxx.upload.issuer')">Issuer</span>
							</dt>
							<dd>
								<span>{{precheckResponse.certificates[0].issuer}}</span>
							</dd>

							<dt>
								<span v-text="$t('pkcsxx.upload.serial')">Serial</span>
							</dt>
							<dd>
								<span>{{precheckResponse.certificates[0].serial}}</span>
							</dd>

							<dt>
								<span v-text="$t('pkcsxx.upload.certificate.validfrom')">Valid from</span>
							</dt>
							<dd>
								<span>{{precheckResponse.certificates[0].validFrom}}</span>
							</dd>

							<dt>
								<span v-text="$t('pkcsxx.upload.certificate.validto')">Valid to</span>
							</dt>
							<dd>
								<span>{{precheckResponse.certificates[0].validTo}}</span>
							</dd>

							<dt v-if="precheckResponse.certificates[0].sans.length > 0">
								<span v-text="$t('pkcsxx.upload.sans')">Subject alternative names</span>
							</dt>
							<dd v-if="precheckResponse.certificates[0].sans.length > 0">
								<ul>
									<li v-for="san in precheckResponse.certificates[0].sans" :key="san">{{san}}</li>
								</ul>
							</dd>
						</dl>

						<dl class="row jh-entity-details" v-if="precheckResponse.dataType === 'CONTAINER'">
							<dt>
								<span v-text="$t('pkcsxx.upload.type')">Result</span>
							</dt>
							<dd>
								<span v-text="$t('pkcsxx.upload.result.certificate')">Certificate Container</span>
							</dd>

							<dt v-if="precheckResponse.certificates.length === 0">
							</dt>

							<dd v-if="precheckResponse.certificates.length === 0">
								<span v-text="$t('pkcsxx.upload.container.no.certificates')">No certificates contained</span>
							</dd>

							<dt>
								<span v-text="$t('pkcsxx.upload.certificates')">Certificates</span>
							</dt>
							<dd>
								<ul>
									<li v-for="cert in precheckResponse.certificates" :key="cert.serial"><div v-if="cert.certificatePresentInDB">present</div><div v-else>present</div>{{cert.subject}}</li>
								</ul>
							</dd>

						</dl>
					</div>


					<div v-if="authenticated">
						<!--div class="row jh-entity-details" v-if="isChecked === true && precheckResponse.dataType === 'X509_CERTIFICATE' && precheckResponse.csrPublicKeyPresentInDB === false">
							<span v-text="$t('pkcsxx.upload.result.certificate.present')">Certificate.already.present</span>
						</div-->

	<!--
							:disabled="precheckResponse.csrPublicKeyPresentInDB || precheckResponse.dataType === 'CONTAINER_REQUIRING_PASSPHRASE' || precheckResponse.certificatePresentInDB || precheckResponse.publicKeyPresentInDB"
	-->
						<button type="button" id="uploadContent"
							v-if="precheckResponse.dataType === 'CSR' || (creationMode === 'SERVERSIDE_KEY_CREATION')"
							:disabled="disableCertificateRequest()"
							class="btn btn-primary" v-on:click="uploadContent">
							<font-awesome-icon icon="upload"></font-awesome-icon>&nbsp;<span v-text="$t('pkcsxx.upload.requestCertificate')">Request certificate</span>
						</button>
						<button type="button" id="showCSRDetails"
							v-if="showCertificateUpload()"
							:disabled="precheckResponse.dataType === 'CONTAINER_REQUIRING_PASSPHRASE' || precheckResponse.certificatePresentInDB || precheckResponse.publicKeyPresentInDB" class="btn btn-primary" v-on:click="uploadContent">
							<font-awesome-icon icon="upload"></font-awesome-icon>&nbsp;<span v-text="$t('pkcsxx.upload.submit')">Upload</span>
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</template>


<script lang="ts" src="./pkcsxx.component.ts">

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

.colContent{
	flex-grow: 4;
}
.pem-content {
	height: 200px;
	width: 600px;
}

.cmd-content {
    height: 100px;
    font-family: monospace;
    font-size: 0.8rem;
}

.cmd0-content {
    height: 50px;
    font-family: monospace;
    font-size: 0.8rem;
}

.draggable {
	background: url("../../../content/images/uploadIcon.png") no-repeat center
		center;
	background-size: 80px 80px;
}


.readonly_comment {
    background-color: #eee;
    opacity: 1;
    margin-top: 5px;
}
</style>
