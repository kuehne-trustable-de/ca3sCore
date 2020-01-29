<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <form name="editForm" role="form" novalidate >

				<h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.upload.detail.title')">Upload</span></h2>
				<div >

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.upload.user')" for="upload-user">User</label>
                        <input type="text" class="form-control" name="user" id="upload-user"
                            v-model="$v.upload.user.$model" required/>
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.upload.password')" for="upload-password">Password</label>
                        <input type="text" class="form-control" name="user" id="upload-password"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="$v.upload.password.$model" required/>
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" v-text="$t('ca3SApp.upload.csr')" for="upload-user">CSR</label>
                        <textarea class="form-control pem-upload" name="csr" id="upload-csr"
							autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"
                            v-model="$v.upload.csr.$model"  required
							v-on:change="notifyChange"/>
                        <div v-if="$v.upload.csr.$anyDirty && $v.upload.csr.$invalid">
                            <small class="form-text text-danger" v-if="!$v.upload.csr.required" v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>
                        </div>
                    </div>

                    <div class="form-group" v-if="$v.upload.checkResult.$model">
                        <label class="form-control-label" v-text="$t('ca3SApp.upload.checkResult')" for="upload-checkResult">Result</label>
                        <input type="text" class="form-control" name="user" id="upload-checkResult"
                             v-model="$v.upload.checkResult.$model" required/>
                    </div>
<!--
					<dl class="row jh-entity-details">
						<dt>
							<span v-text="$t('ca3SApp.upload.user')">User</span>
						</dt>
						<dd>
							<span>{{upload.user}}</span>
						</dd>

						<dt>
							<span v-text="$t('ca3SApp.upload.password')">Password</span>
						</dt>
						<dd>
							<span>{{upload.password}}</span>
						</dd>

						<dt>
							<span v-text="$t('ca3SApp.upload.csr')">CSR / Certificate</span>
						</dt>
						<dd>
							<span>{{upload.csr}}</span>
						</dd>

						<dt>
							<span v-text="$t('ca3SApp.upload.checkResult')">check result</span>
						</dt>
						<dd>
							<span>{{upload.checkResult}}</span>
						</dd>

					</dl>
-->
					<!--form class="col-md-12 ng-pristine ng-untouched ng-valid dynamic-form ng-scope ng-valid-required ng-valid-maxlength" 
						template="tab.formTemplate" 
						ng-submit="processForm()" 
						model="tab.formModel">

						
						<label>user <input type="text" name="user" value="User" class=" " required="required" aria-required="false" aria-invalid="false"></label>
						<label>password <input type="password" name="password" value="s3cr3t" class=" " tabindex="0" required="required" aria-required="false" aria-invalid="false"></label>
						<label spellcheck="false">csr <textarea name="csr" value="-----BEGIN CERTIFICATE REQUEST-----
MIICxDCCAawCAQAwQDELMAkGA1UEBhMCREUxFjAUBgNVBAoTDXRydXN0YWJsZSBM
dGQxGTAXBgNVBAMTEHJlcTE1ODAxMzU3NzAwMDAwggEiMA0GCSqGSIb3DQEBAQUA
A4IBDwAwggEKAoIBAQCGZIrlNa7z1snPbpRKOkEeTxU8gumz6HwGWHLuIQP9g/kT
kLkU5FP1Mqa1BziFegaVmBdsrEnDRnKd8yFjwjNw6kAC0uKEbPWXafDGNqmofSoT
YjU700F1FXHqb+dCEA9esMpX/C1uqoRowwEysvM9oNb4/P5BPi54iqY1Vy6wK6zK
+/P3don6Ldsq34Ybkp6Kmk3EtVWqCrlIE8B0KDxlMIwHR8uCdCB/lw9JCSKGtpXI
Y+puuOm+cfSxl4Sxe8BrBdTLVz/IRmoVyEPw5qQdRhulDTPrqxb/vB2LSMTHqELF
1hr9zMTt2NrC4+VkMO+OzJGkXU54vwUAk4S0BwNbAgMBAAGgPzAXBgkqhkiG9w0B
CQcxChMIcGFzc3dvcmQwJAYJKoZIhvcNAQkOMRcwFTATBgNVHSUEDDAKBggrBgEF
BQcDDjANBgkqhkiG9w0BAQUFAAOCAQEAeoFrEq3wWapRBlRQLcSMpUk9utClWAjA
8voEJw9rAGdrPf1dK9Y6Lwp7BaTUsZvMTRzffvMlU56Yh+So/bJQO1+68t8ZtyPC
/gXGweH3Z9xnmj50QFZbPxye0s7c4Cdb2eOAQbWgdNYyNbfDqJSVRIyajfMXiCO6
Q/yhvmiyNkrZci92OYBwbqbM0qCEq2wufW0RVw0/tuMN4WKK7Oggzv6L6qPbN3bm
GatfvH6M4Wg33fHbHpSOw46oP81DBFDdFvqZaHPJn3KBxAIRUG9/ta6Q4q6759zG
n9OJoGqOzYzbJNPTgOeo36OgUd+YNWbLbUyO+4uPhWpmLmyYjnS8HQ==
-----END CERTIFICATE REQUEST-----
" 
						ng-maxlength="16384" ng-change="checkCSR()" class=" " aria-multiline="true" tabindex="0" required="required" aria-required="false" aria-invalid="false"></textarea></label>
						<label onchange="angular.element(this).scope().fileNameChanged(this)">csr file <input type="file" name="csrFile" class=" " tabindex="0" aria-invalid="false"></label>

						<label>csr precheck <input type="text" name="csrCheckResult" class=" " tabindex="0" aria-invalid="false"></label>
						<label ng-show="isCsrValid()" aria-hidden="true" class="ng-hide">csr info <textarea name="csrInfo" ng-model="tab.formModel['listFormProperty']['csrInfo']" ng-maxlength="16384" ng-class="csrInfo" class=" " aria-multiline="true" tabindex="0" aria-invalid="false"></textarea></label>
						<button type="reset" ng-click="removeTab(tab)" tabindex="0">Cancel</button>
						<button type="submit" ng-click="submitTab(tab)" ng-disabled="!isCsrValid()" tabindex="0" aria-disabled="true" disabled="disabled">OK</button>
					</form-->
				</div>
                <div>
					<!-- :disabled="$v.aCMEAccount.$invalid || isSaving" -->
                    <button type="submit" id="save-entity"  class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.upload.upload')">Upload</span>
                    </button>
                </div>

            </form>

        </div>
    </div>
</template>

<style scoped>
textarea.pem-upload {
  font-family: monospace; 
  height: 12em;
  
}

</style>

<script lang="ts" src="./pkcsxx.component.ts">

</script>
