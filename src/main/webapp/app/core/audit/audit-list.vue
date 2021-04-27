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
                    <span v-text="$t('ca3SApp.audit.subtitle.audit.list')">Audit Trace List</span>
                </h2>

				<div>
					<div v-for="(filter, index) in filters.filterList" :key="index">
						<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName" name="certSelectionAttribute">
							<option v-for="certSelectionItem in certSelectionItems" :key="certSelectionItem.itemName" :value="certSelectionItem.itemName">{{$t(certSelectionItem.itemName)}}</option>
						</select>

						<select float="left" class="smallSelector fa-1x" v-model="filter.selector" name="certSelectionChoice">
							<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

						<select v-if="getInputType(filter.attributeName) == 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue" name="certSelectionSet">
							<option v-for="item in getValueChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>
						<input type="date" v-else-if="getInputType(filter.attributeName) == 'date'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="certSelectionValueDate" v-on:keydown.enter="updateTable"/>
						<input type="hidden" v-else-if="getInputType(filter.attributeName) == 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="certSelectionValueBoolean" v-on:keydown.enter="updateTable"/>
						<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="certSelectionValue" v-on:keydown.enter="updateTable"/>

						<button class="addRemoveSelector" float="right" v-if="index == 0" v-on:click="addSelector()">
							<font-awesome-icon icon="plus"></font-awesome-icon>
						</button>
						<button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
							<font-awesome-icon icon="minus"></font-awesome-icon>
						</button>
					</div>
				</div>


                <audit-table :columns="columns" :data="auditListUrl" :per-page="20" name="audit-table">
                    <template slot-scope="{ row }">
                        <tr>
                            <td >{{ toLocalDate(row.createdOn) }}</td>
                            <td >{{ row.actorName }}</td>
                            <td >{{ row.actorRole }}</td>
                            <td>{{ localizedContent(row.contentTemplate, row.plainContent) }}</td>
                            <td >{{ links(row.csrId, row.certificateId, row.pipelineId, row.caConnectorId, row.processInfoId) }}</td>

                        </tr>
                    </template>

                    <template name="no-result">
                        <div v-text="$t('list.noContent')">no content</div>
                    </template>
                </audit-table>

                <section class="pagers-table">
                    <audit-table-pager type="abbreviated" table="audit-table"></audit-table-pager>
                </section>

			</div>
		</div>
	</div>
</template>

<script lang="ts" src="./audit-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }

</style>
