<template>
  <div>
    <h2 id="page-heading" data-cy="BPMNProcessInfoHeading">
      <span v-text="$t('tmpGenApp.bPMNProcessInfo.home.title')" id="bpmn-process-info-heading">BPMN Process Infos</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('tmpGenApp.bPMNProcessInfo.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'BPMNProcessInfoCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-bpmn-process-info"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('tmpGenApp.bPMNProcessInfo.home.createLabel')"> Create a new BPMN Process Info </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && bPMNProcessInfos && bPMNProcessInfos.length === 0">
      <span v-text="$t('tmpGenApp.bPMNProcessInfo.home.notFound')">No bPMNProcessInfos found</span>
    </div>
    <div class="table-responsive" v-if="bPMNProcessInfos && bPMNProcessInfos.length > 0">
      <table class="table table-striped" aria-describedby="bPMNProcessInfos">
        <thead>
          <tr>
            <th scope="row"><span v-text="$t('global.field.id')">ID</span></th>
            <th scope="row"><span v-text="$t('tmpGenApp.bPMNProcessInfo.name')">Name</span></th>
            <th scope="row"><span v-text="$t('tmpGenApp.bPMNProcessInfo.version')">Version</span></th>
            <th scope="row"><span v-text="$t('tmpGenApp.bPMNProcessInfo.type')">Type</span></th>
            <th scope="row"><span v-text="$t('tmpGenApp.bPMNProcessInfo.author')">Author</span></th>
            <th scope="row"><span v-text="$t('tmpGenApp.bPMNProcessInfo.lastChange')">Last Change</span></th>
            <th scope="row"><span v-text="$t('tmpGenApp.bPMNProcessInfo.signatureBase64')">Signature Base 64</span></th>
            <th scope="row"><span v-text="$t('tmpGenApp.bPMNProcessInfo.bpmnHashBase64')">Bpmn Hash Base 64</span></th>
            <th scope="row"><span v-text="$t('tmpGenApp.bPMNProcessInfo.bpmnContent')">Bpmn Content</span></th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="bPMNProcessInfo in bPMNProcessInfos" :key="bPMNProcessInfo.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'BPMNProcessInfoView', params: { bPMNProcessInfoId: bPMNProcessInfo.id } }">{{
                bPMNProcessInfo.id
              }}</router-link>
            </td>
            <td>{{ bPMNProcessInfo.name }}</td>
            <td>{{ bPMNProcessInfo.version }}</td>
            <td v-text="$t('tmpGenApp.BPMNProcessType.' + bPMNProcessInfo.type)">{{ bPMNProcessInfo.type }}</td>
            <td>{{ bPMNProcessInfo.author }}</td>
            <td>{{ bPMNProcessInfo.lastChange ? $d(Date.parse(bPMNProcessInfo.lastChange), 'short') : '' }}</td>
            <td>{{ bPMNProcessInfo.signatureBase64 }}</td>
            <td>{{ bPMNProcessInfo.bpmnHashBase64 }}</td>
            <td>{{ bPMNProcessInfo.processId }}</td>
            <td class="text-right">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'BPMNProcessInfoView', params: { bPMNProcessInfoId: bPMNProcessInfo.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'BPMNProcessInfoEdit', params: { bPMNProcessInfoId: bPMNProcessInfo.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(bPMNProcessInfo)"
                  variant="danger"
                  class="btn btn-sm"
                  data-cy="entityDeleteButton"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <b-modal ref="removeEntity" id="removeEntity">
      <span slot="modal-title"
        ><span
          id="tmpGenApp.bPMNProcessInfo.delete.question"
          data-cy="bPMNProcessInfoDeleteDialogHeading"
          v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-bPMNProcessInfo-heading" v-text="$t('tmpGenApp.bPMNProcessInfo.delete.question', { id: removeId })">
          Are you sure you want to delete this BPMN Process Info?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-bPMNProcessInfo"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeBPMNProcessInfo()"
        >
          Delete
        </button>
      </div>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./bpmn-process-info.component.ts"></script>
