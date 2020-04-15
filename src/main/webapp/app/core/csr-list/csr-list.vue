<template>
  	<div class="row">

		<div class="col-xs-12 table-responsive">

			<div>
				<div>Request List</div>

				<div v-for="(filter, index) in filters.filterList" :key="index">
					<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName">
						<option v-for="csrSelectionItem in csrSelectionItems" :key="csrSelectionItem.itemName">{{csrSelectionItem.itemName}}</option>
					</select>

					<select float="left" class="smallSelector fa-1x" v-model="filter.selector">
						<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item">{{item}}</option>
					</select>

					<select v-if="getInputType(filter.attributeName) == 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue">
						<option v-for="item in getValueChoices(filter.attributeName)" :key="item">{{item}}</option>
					</select>
					<input type="date" v-else-if="getInputType(filter.attributeName) == 'date'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue"/>
					<input type="hidden" v-else-if="getInputType(filter.attributeName) == 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue"/>
					<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue"/>

					<button class="addRemoveSelector" float="right" v-if="index == 0" v-on:click="addSelector()">
						<font-awesome-icon icon="plus"></font-awesome-icon>
					</button>
					<button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
						<font-awesome-icon icon="minus"></font-awesome-icon>
					</button>
				</div>
			</div>

			<certificate-table :columns="columns" :data="certApiUrl" :per-page="20" name="requests">
				<template scope="{ row }">
					<tr>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})">{{ row.id }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.status }} <router-link v-if="row.certificateId" :to="{name: 'CertInfo', params: {certificateId: row.certificateId}}" >&nbsp;<font-awesome-icon icon="id-card" /></router-link></td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.subject }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ toLocalDate(row.requestedOn) }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.requestedBy }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.pipelineName }}</td>
						<!--td><router-link :to="{name: 'CsrInfo', params: {csrId: row.id}}" >{{ row.processingCA }}</router-link></td-->
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.x509KeySpec }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.publicKeyAlgorithm }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.signingAlgorithm }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.keyLength }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ toLocalDate(row.rejectededOn) }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.rejectionReason }}</td>
						<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.sans }}</td>
					</tr>
				</template>

				<!--template name="footer" scope="{ rows, columns, pagination }">
					<tr>
						<td :colspan="columns.length">Showing rows {{pagination.from}} to {{pagination.to}} of {{pagination.of}} items.</td>
					</tr>
				</template-->
				<!--template name="footer" scope="{ page }">
					<tr>
						<td :colspan="columns.length">Showing rows {{page.from}} to {{page.to}} of {{page.of}} items.</td>
					</tr>
				</template-->
			</certificate-table>
			
			<section class="pagers-table">

				<datatable-pager table="certificates" type="abbreviated"></datatable-pager>	    	

				<!--label>Short</label>
				<datatable-pager v-model="page" type="short"></datatable-pager>
		
				<label>Abbreviated</label>
				<datatable-pager v-model="page" type="abbreviated"></datatable-pager>
		
				<label>Long</label>
				<datatable-pager v-model="page" type="long"></datatable-pager-->
			</section>
		</div>
  	</div>
</template>

<script lang="ts" src="./csr-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }
</style>
