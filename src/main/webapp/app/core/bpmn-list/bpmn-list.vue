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
                    <span v-text="$t('ca3SApp.cSR.subtitle.bpmn.process.list')">BPMN Process List</span>
                </h2>
				<div>
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
				</div>

                <requests-table :columns="columns" :data="bpmnApiUrl" :per-page="20" name="bpmn-table">
					<template slot-scope="{ row }">
						<tr>
							<td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})">{{ row.id }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ row.name }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ row.type }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ row.version }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ row.author }}</td>
                            <td @click="$router.push({name: 'BpmnInfo', params: {bpmnId: row.id}})" >{{ toLocalDate(row.lastChange) }}</td>
						</tr>
					</template>

					<template name="no-result">
						<div v-text="$t('list.noContent')">no content</div>
					</template>
				</requests-table>

				<section class="pagers-table">
					<requests-table-pager type="abbreviated" table="requests"></requests-table-pager>
				</section>
			</div>
  	    </div>
	</div>
</template>

<script lang="ts" src="./bpmn-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }
</style>
