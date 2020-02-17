<template>
    <div class="row justify-content-center">
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

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('pkcsxx.upload.passphrase')" for="upload-passphrase">Passphrase</label>
                        <input type="text" class="form-control" name="passphrase" id="upload-passphrase"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="$v.upload.passphrase.$model" required/>
                    </div>

					<dl class="row jh-entity-details" v-if="isChecked === true && precheckResponse.dataType === 'UNKNOWN'">
						<dt>
							<span v-text="$t('pkcsxx.upload.result.label')">Result</span>
						</dt>
						<dd>
							<span v-text="$t('pkcsxx.upload.result.unknown')">Unknwon content</span>
						</dd>
					</dl>

					<dl class="row jh-entity-details" v-if="isChecked === true && precheckResponse.dataType === 'CSR'">
						<dt>
							<span v-text="$t('pkcsxx.upload.type')">Result</span>
						</dt>
						<dd>
							<span><div v-if="precheckResponse.p10Holder.csrvalid === false">invalid </div>{{precheckResponse.dataType}}</span>
						</dd>

						<div v-if="precheckResponse.publicKeyPresentInDB === true">
							<dt>
								<span v-text="$t('pkcsxx.upload.warning.label')">Warning</span>
							</dt>
							<dd>
								<span v-text="$t('pkcsxx.upload.warning.publicKeyPresent')">Public key already in use</span>
							</dd>
						</div>

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
								<span v-text="$t('pkcsxx.upload.certificate')">X.509 certificate</span>
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

				</div>
                <div v-if="authenticated">
                    <button type="button" id="uploadContent"  class="btn btn-primary" v-on:click="uploadContent">
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
