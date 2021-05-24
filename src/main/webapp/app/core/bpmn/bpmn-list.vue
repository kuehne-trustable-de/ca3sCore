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
				<div>
                    <!--
					<div v-for="(filter, index) in filters.filterList" :key="index">
						<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName" name="csrSelectionAttribute">
							<option v-for="csrSelectionItem in csrSelectionItems" :key="csrSelectionItem.itemName" :value="csrSelectionItem.itemName">{{$t(csrSelectionItem.itemName)}}</option>
						</select>

						<select float="left" class="smallSelector fa-1x" v-model="filter.selector" name="csrSelectionChoice">
							<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

						<select v-if="getInputType(filter.attributeName) == 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionSet">
							<option v-for="item in getValueChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>
						<input type="date" v-else-if="getInputType(filter.attributeName) == 'date'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionValueDate" v-on:keydown.enter="updateTable"/>
						<input type="hidden" v-else-if="getInputType(filter.attributeName) == 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionValueBoolean" v-on:keydown.enter="updateTable"/>
						<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionValue" v-on:keydown.enter="updateTable"/>

						<button class="addRemoveSelector" float="right" v-if="index == 0" v-on:click="addSelector()">
							<font-awesome-icon icon="plus"></font-awesome-icon>
						</button>
						<button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
							<font-awesome-icon icon="minus"></font-awesome-icon>
						</button>
					</div>
					-->
				</div>

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
                                    <b-button v-on:click="prepareRemove(row)"
                                           variant="danger"
                                           class="btn btn-sm"
                                           v-b-modal.removeEntity>
                                        <font-awesome-icon icon="times"></font-awesome-icon>
                                        <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
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
