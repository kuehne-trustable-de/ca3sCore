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
                    <span v-text="$t('ca3SApp.acmeOrder.subtitle.request.list')">Request List</span>
                </h2>
				<div>
					<div v-for="(filter, index) in filters.filterList" :key="index">
						<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName" name="acmeOrderSelectionAttribute">
							<option v-for="acmeOrderSelectionItem in acmeOrderSelectionItems" :key="acmeOrderSelectionItem.itemName" :value="acmeOrderSelectionItem.itemName">{{$t(acmeOrderSelectionItem.itemName)}}</option>
						</select>

						<select float="left" class="smallSelector fa-1x" v-model="filter.selector" name="acmeOrderSelectionChoice">
							<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

						<select v-if="getInputType(filter.attributeName) === 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue" name="acmeOrderSelectionSet">
							<option v-for="item in getValueChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>
						<input type="date" v-else-if="getInputType(filter.attributeName) === 'date'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="acmeOrderSelectionValueDate" v-on:keydown.enter="updateTable"/>
						<input type="hidden" v-else-if="getInputType(filter.attributeName) === 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="acmeOrderSelectionValueBoolean" v-on:keydown.enter="updateTable"/>
						<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="acmeOrderSelectionValue" v-on:keydown.enter="updateTable"/>

						<button class="addRemoveSelector" float="right" v-if="index === 0" v-on:click="addSelector()">
							<font-awesome-icon icon="plus"></font-awesome-icon>
						</button>
						<button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
							<font-awesome-icon icon="minus"></font-awesome-icon>
						</button>
                        <!--a v-if="index === 0" href="downloadCSV" @click.prevent="downloadCSV()" float="right"><font-awesome-icon icon="file-csv"></font-awesome-icon></a-->
					</div>
				</div>

                <orders-table :columns="columns" :data="acmeOrderApiUrl" :per-page="20" name="orders">
					<template slot-scope="{ row }">
						<tr>
                            <td @click="$router.push({name: 'acmeOrder', params: {acmeOrderId: row.id}})">{{ row.id }}</td>
                            <td @click="$router.push({name: 'acmeOrder', params: {acmeOrderId: row.id}})">{{ row.orderId }}</td>
							<td @click="$router.push({name: 'acmeOrder', params: {acmeOrderId: row.id}})" >{{ $t(row.status) }}</td>
							<td @click="$router.push({name: 'acmeOrder', params: {acmeOrderId: row.id}})" >{{ row.realm }}</td>
                            <td @click="$router.push({name: 'acmeOrder', params: {acmeOrderId: row.id}})" >{{ toLocalDate(row.expires) }}</td>
                            <td @click="$router.push({name: 'acmeOrder', params: {acmeOrderId: row.id}})" >{{ toLocalDate(row.notBefore) }}</td>
                            <td @click="$router.push({name: 'acmeOrder', params: {acmeOrderId: row.id}})" >{{ toLocalDate(row.notAfter) }}</td>
							<td @click="$router.push({name: 'acmeOrder', params: {acmeOrderId: row.id}})" >{{ row.error }}</td>
						</tr>
					</template>

					<template name="no-result">
						<div v-text="$t('list.noContent')">no content</div>
					</template>
				</orders-table>

				<section class="pagers-table">
					<requests-table-pager type="abbreviated" table="accounts"></requests-table-pager>
				</section>
			</div>
  	    </div>
	</div>
</template>

<script lang="ts" src="./acme-order-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }
</style>
