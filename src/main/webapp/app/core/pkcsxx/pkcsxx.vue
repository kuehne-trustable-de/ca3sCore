<template>
    <div class="row justify-content-center" v-cloak @drop.prevent="catchDroppedFile" @dragover.prevent>
        <div class="col-8">
            <form name="editForm" role="form" novalidate >

				<h2 class="jh-entity-heading"><span v-text="$t('pkcsxx.subtitle')">Upload</span></h2>
				<div >

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('pkcsxx.upload.content')" for="upload-content">Content</label>
                        <textarea class="form-control pem-upload" name="content" id="upload-content"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="$v.upload.content.$model"  required
							v-on:input="notifyChange"/>
                        <div v-if="$v.upload.content.$anyDirty && $v.upload.content.$invalid">
                            <small class="form-text text-danger" v-if="!$v.upload.content.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>

                	<div v-if="authenticated" class="form-group">
                        <label class="form-control-label" v-text="$t('pkcsxx.upload.pipeline')" for="pkcsxx-pipeline">Pipeline</label>
                        <select class="form-control" id="pkcsxx-pipeline" name="pkcsxx-pipeline" v-model="$v.upload.pipelineId.$model">
                            <option v-bind:value="null"></option>
                            <option v-bind:value="upload && webPipeline.id === upload.pipelineId ? upload.pipelineId : webPipeline.id" v-for="webPipeline in allWebPipelines" :key="webPipeline.id">{{webPipeline.name}}</option>
                        </select>
                        <!--label class="form-control-label" >__ {{currentPipelineInfo(upload.pipelineId)}} __</label-->
                    </div>

                    <div class="form-group" v-if="showRequestorCommentsArea()">
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
							<span v-text="$t('pkcsxx.upload.warning.publicKeyPresent')">Public key already in use</span>
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
							<span v-text="$t('pkcsxx.upload.result.certificate')">Certificate</span>
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
					    :disabled="precheckResponse.dataType === 'CONTAINER_REQUIRING_PASSPHRASE' || precheckResponse.certificatePresentInDB || precheckResponse.publicKeyPresentInDB" class="btn btn-primary" v-on:click="uploadContent">
                        <font-awesome-icon icon="upload"></font-awesome-icon>&nbsp;<span v-text="$t('pkcsxx.upload.requestCertificate')">Request certificate</span>
                    </button>
                    <button type="button" id="uploadContent" 
					    v-if="precheckResponse.dataType === 'X509_CERTIFICATE' || precheckResponse.dataType === 'CONTAINER' " 
					    :disabled="precheckResponse.dataType === 'CONTAINER_REQUIRING_PASSPHRASE' || precheckResponse.certificatePresentInDB || precheckResponse.publicKeyPresentInDB" class="btn btn-primary" v-on:click="uploadContent">
                        <font-awesome-icon icon="upload"></font-awesome-icon>&nbsp;<span v-text="$t('pkcsxx.upload.submit')">Upload</span>
                    </button>
                </div>

            </form>

        </div>
    </div>
</template>

<style scoped>
textarea.pem-upload {
  font-family: monospace; 
  height: 10em;
  
}

</style>

<script lang="ts" src="./pkcsxx.component.ts">

</script>
