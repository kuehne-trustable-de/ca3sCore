<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="bPMNProcessInfo">
        <h2 class="jh-entity-heading" data-cy="bPMNProcessInfoDetailsHeading">
          <span v-text="$t('tmpGenApp.bPMNProcessInfo.detail.title')">BPMNProcessInfo</span> {{ bPMNProcessInfo.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.name')">Name</span>
          </dt>
          <dd>
            <span>{{ bPMNProcessInfo.name }}</span>
          </dd>
          <dt>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.version')">Version</span>
          </dt>
          <dd>
            <span>{{ bPMNProcessInfo.version }}</span>
          </dd>
          <dt>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.type')">Type</span>
          </dt>
          <dd>
            <span v-text="$t('tmpGenApp.BPMNProcessType.' + bPMNProcessInfo.type)">{{ bPMNProcessInfo.type }}</span>
          </dd>
          <dt>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.author')">Author</span>
          </dt>
          <dd>
            <span>{{ bPMNProcessInfo.author }}</span>
          </dd>
          <dt>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.lastChange')">Last Change</span>
          </dt>
          <dd>
            <span v-if="bPMNProcessInfo.lastChange">{{ $d(Date.parse(bPMNProcessInfo.lastChange), 'long') }}</span>
          </dd>
          <dt>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.signatureBase64')">Signature Base 64</span>
          </dt>
          <dd>
            <span>{{ bPMNProcessInfo.signatureBase64 }}</span>
          </dd>
          <dt>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.bpmnHashBase64')">Bpmn Hash Base 64</span>
          </dt>
          <dd>
            <span>{{ bPMNProcessInfo.bpmnHashBase64 }}</span>
          </dd>
          <dt>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.bpmnContent')">Bpmn Content</span>
          </dt>
          <dd>
            <span>{{ bPMNProcessInfo.processId }}</span>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link
          v-if="bPMNProcessInfo.id"
          :to="{ name: 'BPMNProcessInfoEdit', params: { bPMNProcessInfoId: bPMNProcessInfo.id } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./bpmn-process-info-details.component.ts"></script>
