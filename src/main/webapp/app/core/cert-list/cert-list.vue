<template>
  	<div class="row">

		<div class="col-xs-12 table-responsive">

			<div>
				<div>Certificate list</div>

				<div v-for="(filter, index) in filters.filterList" :key="index">
					<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName">
						<option v-for="certSelectionItem in certSelectionItems" :key="certSelectionItem.itemName">{{certSelectionItem.itemName}}</option>
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

			<certificate :columns="columns" :data="certApiUrl" :per-page="20" name="certificates">
				<template scope="{ row }">
					<tr>
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.id }}</td>
						<td :style="getRevocationStyle(row.revoked)"><router-link :to="{name: 'CertInfo', params: {certificateId: row.id}}" >{{ row.subject }}</router-link></td>
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.issuer }}</td>
						<!--td><router-link :to="{name: 'CertInfo', params: {certificateId: row.id}}" >{{ row.type }}</router-link></td-->
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.keyLength }}</td>
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{(row.serial.length > 12) ? row.serial.substring(0, 6).concat('...', row.serial.substring(row.serial.length - 4, row.serial.length )) : row.serial}}</td>
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ toLocalDate(row.validFrom)}}</td>
						<td ><router-link :style="getValidToStyle(row.validFrom, row.validTo, row.revoked)" :to="{name: 'CertInfo', params: {certificateId: row.id}}" >{{ toLocalDate(row.validTo) }}</router-link></td>
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.hashAlgorithm }}</td>
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.paddingAlgorithm }}</td>
						<!--td><router-link :to="{name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.revoked }}</router-link></td-->
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ toLocalDate(row.revokedSince) }}</td>
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.revocationReason }}</td>
						<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.sans }}</td>
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
			</certificate>

			<section class="pagers-table">
				<certificate-pager type="abbreviated" table="certificates"></certificate-pager>	    	
			</section>
		</div>
  	</div>
</template>

<script lang="ts" src="./cert-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }

</style>
