<template>
    <div class="row justify-content-center" v-cloak @drop.prevent="catchDroppedFile" @dragover.prevent>
        <div class="col-8">
            <form name="editForm" role="form" novalidate >

				<h2 class="jh-entity-heading"><span v-text="$t('pkcsxx.subtitle')">Upload</span></h2>
				<div >

                	<div class="form-group">
                        <label class="form-control-label" v-text="$t('pkcsxx.upload.pipeline')" for="pkcsxx-pipeline">Pipeline</label>
                        <select class="form-control" id="pkcsxx-pipeline" name="pkcsxx-pipeline" v-model="$v.upload.pipelineId.$model" required v-on:change="updatePipelineRestrictions($event)">
                            <option v-bind:value="upload && webPipeline.id === upload.pipelineId ? upload.pipelineId : webPipeline.id" v-for="webPipeline in allWebPipelines" :key="webPipeline.id">{{webPipeline.name}}</option>
                        </select>

						<span>{{selectPipelineInfo}}</span>

						<!--span>upload.pipelineId.id  = {{upload.pipelineId.id}}</span>
                        <div v-if="upload.pipelineId.id < 1110">
                            <small class="form-text text-danger" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div-->
                        <!--label class="form-control-label" >__ {{currentPipelineInfo(upload.pipelineId)}} __</label-->
                    </div>

					<div class="form-group">
						<label class="form-control-label" v-text="$t('pkcsxx.upload.creationMode.selection')" for="pkcsxx-key-creation">Creation mode</label>
						<select class="form-control" id="pkcsxx-key-creation" name="pkcsxx-key-creation" v-model="creationMode" >
							<option value="csrAvailable" v-text="$t('pkcsxx.upload.creationMode.csrAvailable')" selected="selected">csrAvailable</option>
							<option value="commandLineTool" v-text="$t('pkcsxx.upload.creationMode.commandLineTool')">csr generation command line</option>
							<option value="serversideKeyCreation" v-text="$t('pkcsxx.upload.creationMode.serversideKeyCreation')">serverside key creation</option>
						</select>
					</div>

                    <div class="form-group" v-if="creationMode === 'commandLineTool'">
						<label class="form-control-label" v-text="$t('pkcsxx.upload.creationTool.selection')" for="pkcsxx-key-tool">Creation tool</label>
						<select class="form-control" id="pkcsxx-key-tool" name="pkcsxx-key-tool" v-model="creationTool" >
							<option value="keytool" v-text="$t('pkcsxx.upload.creationTool.keytool')" selected="selected">keytool</option>
							<option value="openssl" v-text="$t('pkcsxx.upload.creationTool.openssl')" >openssl</option>
						</select>
					</div>

                	<div class="form-group" v-if="(creationMode === 'commandLineTool') || (creationMode === 'serversideKeyCreation')">
                        <label class="form-control-label" v-text="$t('pkcsxx.upload.certificateParams')" for="pkcsxx-pipeline">certificateParams</label>

                    	<div class="row">
                        	<div class="col">
								<label class="form-control-label" v-text="$t('pkcsxx.upload.key-length')" for="pkcsxx.upload.key-length">Key length</label>
							</div>
                        	<div class="col colContent">
								<select class="form-control" id="pkcsxx.upload.key-length" name="pkcsxx.upload.key-length" v-model="keyAlgoLength">
									<option value="RSA-2048" selected="selected">RSA-2048</option>
									<option value="RSA-4096">RSA-4096</option>
								</select>
							</div>
                		</div>

                    	<div class="row" v-if="pipelineRestrictions.cn.cardinality !== 'NOT_ALLOWED'">
                        	<div class="col ">
								<label class="form-control-label" v-text="$t('pkcsxx.upload.cn')" for="pkcsxx.upload.cn">CN</label>
							</div>
                        	<div class="col colContent">
								<input type="text" class="form-check-inline" name="pkcsxx.upload.cn" id="pkcsxx.upload.cn" v-model="upload.certificateAttributes[1].values[0]" v-bind:required="pipelineRestrictions.cn.required"/>
							</div>
                		</div>
                    	<div class="row" v-if="pipelineRestrictions.c.cardinality !== 'NOT_ALLOWED'">
                        	<div class="col">
								<label class="form-control-label" v-text="$t('pkcsxx.upload.c')" for="pkcsxx.upload.c">C</label>
							</div>
                        	<div class="col colContent">
								<input type="text" class="form-check-inline" name="pkcsxx.upload.c" id="pkcsxx.upload.c" v-model="upload.certificateAttributes[0].values[0]"  v-bind:readonly="pipelineRestrictions.c.readOnly"/>
							</div>
                		</div>
                    	<div class="row" v-if="pipelineRestrictions.o.cardinality !== 'NOT_ALLOWED'">
                        	<div class="col">
								<label class="form-control-label" v-text="$t('pkcsxx.upload.o')" for="pkcsxx.upload.o">O</label>
							</div>
                        	<div class="col colContent">
								<input type="text" class="form-check-inline" name="pkcsxx.upload.o" id="pkcsxx.upload.o" v-model="upload.certificateAttributes[2].values[0]" />
							</div>
                		</div>
                    	<div class="row" v-if="pipelineRestrictions.ou.cardinality !== 'NOT_ALLOWED'">
                        	<div class="col">
								<label class="form-control-label" v-text="$t('pkcsxx.upload.ou')" for="pkcsxx.upload.ou">OU</label>
							</div>
                        	<div class="col colContent">
								<input type="text" class="form-check-inline" name="pkcsxx.upload.ou" id="pkcsxx.upload.ou" v-model="upload.certificateAttributes[3].values[0]" />
							</div>
                		</div>
                    	<div class="row" v-if="pipelineRestrictions.l.cardinality !== 'NOT_ALLOWED'">
                        	<div class="col">
								<label class="form-control-label" v-text="$t('pkcsxx.upload.l')" for="pkcsxx.upload.l">L</label>
							</div>
                        	<div class="col colContent">
								<input type="text" class="form-check-inline" name="pkcsxx.upload.l" id="pkcsxx.upload.l" v-model="upload.certificateAttributes[4].values[0]" />
							</div>
                		</div>
                    	<div class="row" v-if="pipelineRestrictions.st.cardinality !== 'NOT_ALLOWED'">
                        	<div class="col">
								<label class="form-control-label" v-text="$t('pkcsxx.upload.st')" for="pkcsxx.upload.st">ST</label>
							</div>
                        	<div class="col colContent">
								<input type="text" class="form-check-inline" name="pkcsxx.upload.st" id="pkcsxx.upload.st" v-model="upload.certificateAttributes[5].values[0]"/>
							</div>
                		</div>
                    	<div class="row" v-if="pipelineRestrictions.san.cardinality !== 'NOT_ALLOWED'">
                        	<div class="col">
								<label class="form-control-label" v-text="$t('pkcsxx.upload.san')" for="pkcsxx.upload.san">SAN</label>
							</div>
                        	<div class="col colContent">
								<input v-for="(item, index) in upload.certificateAttributes[6].values" :key="index" type="text" class="form-check-inline" name="pkcsxx.upload.san" id="pkcsxx.upload.san" v-model="upload.certificateAttributes[6].values[index]"/>
							</div>
                		</div>
                    </div>

                    <div class="form-group" v-if="creationMode === 'commandLineTool'">
						<label class="form-control-label" v-text="$t('pkcsxx.upload.creationTool.cmdline')" for="pkcsxx-cmdline">Command line</label>
						<textarea class="form-control cmd-content" name="certContent" id="pkcsxx-cmdline"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" readonly
							v-model="commandLine" />
					</div>

                    <div class="form-group" v-if="(creationMode === 'csrAvailable') || (creationMode === 'commandLineTool')">
                        <!--label class="form-control-label" v-text="$t('pkcsxx.upload.content')" for="upload-content">Content</label-->
						<div>
                        	<label class="form-control-label" v-text="$t('pkcsxx.upload.fileSelector')" for="fileSelector">Select a file</label>
							<input type="file" id="fileSelector" ref="fileSelector" name="fileSelector" @change="notifyFileChange" />
						</div>
                        <textarea class="form-control pem-content draggable" name="content" id="upload-content"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="$v.upload.content.$model"  required
							v-on:input="notifyChange"/>
                        <div v-if="$v.upload.content.$anyDirty && $v.upload.content.$invalid">
                            <small class="form-text text-danger" v-if="!$v.upload.content.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>

                    <div class="form-group" v-if="showCSRRelatedArea()">
                        <label class="form-control-label" v-text="$t('pkcsxx.upload.requestorComment')" for="upload-requestor-comment">Requestor Comment</label>
                        <textarea type="text" class="form-control" name="requestor-comment" id="upload-requestor-comment"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="$v.upload.requestorcomment.$model" v-on:input="notifyChange"/>
                    </div>

                    <div class="form-group" v-if="precheckResponse.dataType === 'CONTAINER_REQUIRING_PASSPHRASE'">
                        <label class="form-control-label" v-text="$t('pkcsxx.upload.passphrase')" for="upload-passphrase">Passphrase</label>
                        <input type="text" class="form-control" name="passphrase" id="upload-passphrase"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="$v.upload.passphrase.$model" v-on:input="notifyChange"/>
                    </div>

					<dl class="row jh-entity-details" v-if="responseStatus > 0">
						<dt>
							<span v-text="$t('pkcsxx.upload.result.error')">Error</span>
						</dt>
						<dd>
							<span v-if="responseStatus == 400" v-text="$t('pkcsxx.upload.result.content.not.parseable')">Ccontent not parseable</span>
							<span v-else-if="responseStatus == 409" v-text="$t('pkcsxx.upload.result.certificate.already.exists')">Certificate already exists</span>
							<span v-else-if="responseStatus == 201" v-text="$t('pkcsxx.upload.result.upload.successful')">Upload successful</span>
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
					
                    <button type="button" id="uploadContent"
					    v-if="precheckResponse.dataType === 'CSR'" 
					    :disabled="precheckResponse.csrPublicKeyPresentInDB || precheckResponse.dataType === 'CONTAINER_REQUIRING_PASSPHRASE' || precheckResponse.certificatePresentInDB || precheckResponse.publicKeyPresentInDB" class="btn btn-primary" v-on:click="uploadContent">
                        <font-awesome-icon icon="upload"></font-awesome-icon>&nbsp;<span v-text="$t('pkcsxx.upload.requestCertificate')">Request certificate</span>
                    </button>
                    <button type="button" id="uploadContent" 
					    v-if="showCertificateUpload()" 
					    :disabled="precheckResponse.dataType === 'CONTAINER_REQUIRING_PASSPHRASE' || precheckResponse.certificatePresentInDB || precheckResponse.publicKeyPresentInDB" class="btn btn-primary" v-on:click="uploadContent">
                        <font-awesome-icon icon="upload"></font-awesome-icon>&nbsp;<span v-text="$t('pkcsxx.upload.submit')">Upload</span>
                    </button>
                </div>

            </form>

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

.draggable {
	background: url("../../../content/images/uploadIcon.png") no-repeat center
		center;
	background-size: 80px 80px;
}
</style>
