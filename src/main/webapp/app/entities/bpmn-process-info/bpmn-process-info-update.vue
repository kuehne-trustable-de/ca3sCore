<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="tmpGenApp.bPMNProcessInfo.home.createOrEditLabel"
          data-cy="BPMNProcessInfoCreateUpdateHeading"
          v-text="$t('tmpGenApp.bPMNProcessInfo.home.createOrEditLabel')"
        >
          Create or edit a BPMNProcessInfo
        </h2>
        <div>
          <div class="form-group" v-if="bPMNProcessInfo.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="bPMNProcessInfo.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('tmpGenApp.bPMNProcessInfo.name')" for="bpmn-process-info-name">Name</label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="bpmn-process-info-name"
              data-cy="name"
              :class="{ valid: !$v.bPMNProcessInfo.name.$invalid, invalid: $v.bPMNProcessInfo.name.$invalid }"
              v-model="$v.bPMNProcessInfo.name.$model"
              required
            />
            <div v-if="$v.bPMNProcessInfo.name.$anyDirty && $v.bPMNProcessInfo.name.$invalid">
              <small class="form-text text-danger" v-if="!$v.bPMNProcessInfo.name.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('tmpGenApp.bPMNProcessInfo.version')" for="bpmn-process-info-version"
              >Version</label
            >
            <input
              type="text"
              class="form-control"
              name="version"
              id="bpmn-process-info-version"
              data-cy="version"
              :class="{ valid: !$v.bPMNProcessInfo.version.$invalid, invalid: $v.bPMNProcessInfo.version.$invalid }"
              v-model="$v.bPMNProcessInfo.version.$model"
              required
            />
            <div v-if="$v.bPMNProcessInfo.version.$anyDirty && $v.bPMNProcessInfo.version.$invalid">
              <small class="form-text text-danger" v-if="!$v.bPMNProcessInfo.version.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('tmpGenApp.bPMNProcessInfo.type')" for="bpmn-process-info-type">Type</label>
            <select
              class="form-control"
              name="type"
              :class="{ valid: !$v.bPMNProcessInfo.type.$invalid, invalid: $v.bPMNProcessInfo.type.$invalid }"
              v-model="$v.bPMNProcessInfo.type.$model"
              id="bpmn-process-info-type"
              data-cy="type"
              required
            >
              <option value="CA_INVOCATION" v-bind:label="$t('tmpGenApp.BPMNProcessType.CA_INVOCATION')">CA_INVOCATION</option>
              <option value="REQUEST_AUTHORIZATION" v-bind:label="$t('tmpGenApp.BPMNProcessType.REQUEST_AUTHORIZATION')">
                REQUEST_AUTHORIZATION
              </option>
            </select>
            <div v-if="$v.bPMNProcessInfo.type.$anyDirty && $v.bPMNProcessInfo.type.$invalid">
              <small class="form-text text-danger" v-if="!$v.bPMNProcessInfo.type.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('tmpGenApp.bPMNProcessInfo.author')" for="bpmn-process-info-author">Author</label>
            <input
              type="text"
              class="form-control"
              name="author"
              id="bpmn-process-info-author"
              data-cy="author"
              :class="{ valid: !$v.bPMNProcessInfo.author.$invalid, invalid: $v.bPMNProcessInfo.author.$invalid }"
              v-model="$v.bPMNProcessInfo.author.$model"
              required
            />
            <div v-if="$v.bPMNProcessInfo.author.$anyDirty && $v.bPMNProcessInfo.author.$invalid">
              <small class="form-text text-danger" v-if="!$v.bPMNProcessInfo.author.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('tmpGenApp.bPMNProcessInfo.lastChange')" for="bpmn-process-info-lastChange"
              >Last Change</label
            >
            <div class="d-flex">
              <input
                id="bpmn-process-info-lastChange"
                data-cy="lastChange"
                type="datetime-local"
                class="form-control"
                name="lastChange"
                :class="{ valid: !$v.bPMNProcessInfo.lastChange.$invalid, invalid: $v.bPMNProcessInfo.lastChange.$invalid }"
                required
                :value="convertDateTimeFromServer($v.bPMNProcessInfo.lastChange.$model)"
                @change="updateInstantField('lastChange', $event)"
              />
            </div>
            <div v-if="$v.bPMNProcessInfo.lastChange.$anyDirty && $v.bPMNProcessInfo.lastChange.$invalid">
              <small class="form-text text-danger" v-if="!$v.bPMNProcessInfo.lastChange.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
              <small
                class="form-text text-danger"
                v-if="!$v.bPMNProcessInfo.lastChange.ZonedDateTimelocal"
                v-text="$t('entity.validation.ZonedDateTimelocal')"
              >
                This field should be a date and time.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="$t('tmpGenApp.bPMNProcessInfo.signatureBase64')"
              for="bpmn-process-info-signatureBase64"
              >Signature Base 64</label
            >
            <textarea
              class="form-control"
              name="signatureBase64"
              id="bpmn-process-info-signatureBase64"
              data-cy="signatureBase64"
              :class="{ valid: !$v.bPMNProcessInfo.signatureBase64.$invalid, invalid: $v.bPMNProcessInfo.signatureBase64.$invalid }"
              v-model="$v.bPMNProcessInfo.signatureBase64.$model"
              required
            ></textarea>
            <div v-if="$v.bPMNProcessInfo.signatureBase64.$anyDirty && $v.bPMNProcessInfo.signatureBase64.$invalid">
              <small
                class="form-text text-danger"
                v-if="!$v.bPMNProcessInfo.signatureBase64.required"
                v-text="$t('entity.validation.required')"
              >
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('tmpGenApp.bPMNProcessInfo.bpmnHashBase64')" for="bpmn-process-info-bpmnHashBase64"
              >Bpmn Hash Base 64</label
            >
            <input
              type="text"
              class="form-control"
              name="bpmnHashBase64"
              id="bpmn-process-info-bpmnHashBase64"
              data-cy="bpmnHashBase64"
              :class="{ valid: !$v.bPMNProcessInfo.bpmnHashBase64.$invalid, invalid: $v.bPMNProcessInfo.bpmnHashBase64.$invalid }"
              v-model="$v.bPMNProcessInfo.bpmnHashBase64.$model"
              required
            />
            <div v-if="$v.bPMNProcessInfo.bpmnHashBase64.$anyDirty && $v.bPMNProcessInfo.bpmnHashBase64.$invalid">
              <small
                class="form-text text-danger"
                v-if="!$v.bPMNProcessInfo.bpmnHashBase64.required"
                v-text="$t('entity.validation.required')"
              >
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('tmpGenApp.bPMNProcessInfo.bpmnContent')" for="bpmn-process-info-bpmnContent"
              >Bpmn Content</label
            >
            <textarea
              class="form-control"
              name="bpmnContent"
              id="bpmn-process-info-bpmnContent"
              data-cy="bpmnContent"
              :class="{ valid: !$v.bPMNProcessInfo.bpmnContent.$invalid, invalid: $v.bPMNProcessInfo.bpmnContent.$invalid }"
              v-model="$v.bPMNProcessInfo.bpmnContent.$model"
              required
            ></textarea>
            <div v-if="$v.bPMNProcessInfo.bpmnContent.$anyDirty && $v.bPMNProcessInfo.bpmnContent.$invalid">
              <small
                class="form-text text-danger"
                v-if="!$v.bPMNProcessInfo.bpmnContent.required"
                v-text="$t('entity.validation.required')"
              >
                This field is required.
              </small>
            </div>
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="$v.bPMNProcessInfo.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./bpmn-process-info-update.component.ts"></script>
