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
                    <span v-text="$t('ca3SApp.scepOrder.subtitle.request.list')">Request List</span>
                </h2>
				<div>
					<div v-for="(filter, index) in filters.filterList" :key="index">
						<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName" name="scepOrderSelectionAttribute">
							<option v-for="scepOrderSelectionItem in scepOrderSelectionItems" :key="scepOrderSelectionItem.itemName" :value="scepOrderSelectionItem.itemName">{{$t(scepOrderSelectionItem.itemName)}}</option>
						</select>

						<select float="left" class="smallSelector fa-1x" v-model="filter.selector" name="scepOrderSelectionChoice">
							<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

						<select v-if="getInputType(filter.attributeName) === 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue" name="scepOrderSelectionSet">
							<option v-for="item in getValueChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>
						<input type="date" v-else-if="getInputType(filter.attributeName) === 'date'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="scepOrderSelectionValueDate" v-on:keydown.enter="updateTable"/>
						<input type="hidden" v-else-if="getInputType(filter.attributeName) === 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="scepOrderSelectionValueBoolean" v-on:keydown.enter="updateTable"/>
						<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="scepOrderSelectionValue" v-on:keydown.enter="updateTable"/>

						<button class="addRemoveSelector" float="right" v-if="index === 0" v-on:click="addSelector()">
							<font-awesome-icon icon="plus"></font-awesome-icon>
						</button>
						<button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
							<font-awesome-icon icon="minus"></font-awesome-icon>
						</button>
                        <!--a v-if="index === 0" href="downloadCSV" @click.prevent="downloadCSV()" float="right"><font-awesome-icon icon="file-csv"></font-awesome-icon></a-->
					</div>
				</div>

                <orders-table :columns="columns" :data="scepOrderApiUrl" :per-page="20" name="orders">
					<template slot-scope="{ row }">
						<tr>
                            <td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})">{{ row.id }}</td>
                            <td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})">{{ row.transId }}</td>
							<td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})" >{{ $t(row.status) }}</td>
							<td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})" >{{ row.realm }}</td>
                            <td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})" >{{ row.pipelineName }}</td>
                            <td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})" >{{ toLocalDate(row.requestedOn) }}</td>
                            <td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})" >{{ row.requestedBy }}</td>
                            <td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})" >{{ row.subject }}</td>
                            <td @click="$router.push({name: 'ScepOrderInfo', params: {id: row.id}})" >{{ row.sans }}</td>
						</tr>
					</template>

                    <template name="no-result">
						<div v-text="$t('list.noContent')">no content</div>
					</template>
				</orders-table>

				<section class="pagers-table">
					<orders-table-pager type="abbreviated" table="orders"></orders-table-pager>
				</section>
			</div>
  	    </div>
	</div>
</template>

<script lang="ts" src="./scep-order-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }
</style>
