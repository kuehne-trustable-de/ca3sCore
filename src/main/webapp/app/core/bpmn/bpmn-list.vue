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

  	    <div class="row">

			<div class="col-xs-12 table-responsive">
                <h2 class="jh-entity-heading">
                    <span v-text="$t('ca3SApp.bPNMProcessInfo.subtitle.process.list')">BPMN Process List</span>

                     <router-link :to="{name: 'BpmnNew'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-bpnm-process-info">
                        <font-awesome-icon icon="plus"></font-awesome-icon>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.home.createLabel')">Create a new BPNM Process Info</span>
                    </router-link>

                </h2>

                <bpmn-table :columns="columns" :data="bpmnApiUrl" :per-page="20" name="bpmn-table">
					<template slot-scope="{ row }">
						<tr>
							<td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})">{{ row.id }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ row.name }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ row.type }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ row.version }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ row.author }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ toLocalDate(row.lastChange) }}</td>
                            <td class="text-right">
                                <div class="btn-group">
                                    <b-button v-on:click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id, interactionMode: 'TEST'}})""
                                              variant="primary"
                                              class="btn btn-sm" >
                                        <font-awesome-icon icon="times"></font-awesome-icon>
                                        <span class="d-none d-md-inline" v-text="$t('entity.action.test')"></span>
                                    </b-button>
                                    <b-button v-on:click="prepareRemove(row)"
                                              variant="danger"
                                              class="btn btn-sm"
                                              v-b-modal.removeEntity>
                                        <font-awesome-icon icon="times"></font-awesome-icon>
                                        <span class="d-none d-md-inline" v-text="$t('entity.action.delete')"></span>
                                    </b-button>
                                </div>
                            </td>
						</tr>
					</template>

					<template name="no-result">
						<div v-text="$t('list.noContent')">no content</div>
					</template>
				</bpmn-table>

				<section class="pagers-table">
					<bpmn-table-pager type="abbreviated" table="bpmn-table"></bpmn-table-pager>
				</section>
			</div>
  	    </div>

        <b-modal ref="removeEntity" id="removeEntity" >
            <span slot="modal-title"><span id="ca3SApp.bpmn.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-bpmn-heading" v-text="$t('ca3SApp.bpmn.delete.question', {'id': removeId})">Are you sure you want to delete this BPMN process?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-pipeline" v-text="$t('entity.action.delete')" v-on:click="removeBPMNProcess()">Delete</button>
            </div>
            <div v-if="deletionWarningMessage">
              <span id="ca3SApp.bpmn.delete.error">{{deletionWarningMessage}}</span>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./bpmn-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }
</style>
