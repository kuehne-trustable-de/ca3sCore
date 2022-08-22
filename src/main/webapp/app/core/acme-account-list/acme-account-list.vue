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
                    <span v-text="$t('ca3SApp.acmeAccount.subtitle.request.list')">Request List</span>
                </h2>
				<div>
					<div v-for="(filter, index) in filters.filterList" :key="index">
						<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName" name="acmeAccountSelectionAttribute">
							<option v-for="acmeAccountSelectionItem in acmeAccountSelectionItems" :key="acmeAccountSelectionItem.itemName" :value="acmeAccountSelectionItem.itemName">{{$t(acmeAccountSelectionItem.itemName)}}</option>
						</select>

						<select float="left" class="smallSelector fa-1x" v-model="filter.selector" name="acmeAccountSelectionChoice">
							<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

						<select v-if="getInputType(filter.attributeName) === 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue" name="acmeAccountSelectionSet">
							<option v-for="item in getValueChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>
						<input type="date" v-else-if="getInputType(filter.attributeName) === 'date'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="acmeAccountSelectionValueDate" v-on:keydown.enter="updateTable"/>
						<input type="hidden" v-else-if="getInputType(filter.attributeName) === 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="acmeAccountSelectionValueBoolean" v-on:keydown.enter="updateTable"/>
						<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="acmeAccountSelectionValue" v-on:keydown.enter="updateTable"/>

						<button class="addRemoveSelector" float="right" v-if="index === 0" v-on:click="addSelector()">
							<font-awesome-icon icon="plus"></font-awesome-icon>
						</button>
						<button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
							<font-awesome-icon icon="minus"></font-awesome-icon>
						</button>
                        <!--a v-if="index === 0" href="downloadCSV" @click.prevent="downloadCSV()" float="right"><font-awesome-icon icon="file-csv"></font-awesome-icon></a-->
					</div>
				</div>

                <account :columns="columns" :data="acmeAccountApiUrl" :per-page="20" name="accounts">
					<template slot-scope="{ row }">
						<tr>
                            <td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})">{{ row.id }}</td>
                            <td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})">{{ row.accountId }}</td>
							<td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})" >{{ $t(row.status) }}</td>
							<td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})" >{{ row.realm }}</td>
							<td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})" >{{ toLocalDate(row.createdOn) }}</td>
							<td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})" >{{ row.termsOfServiceAgreed }}</td>
							<td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})" >{{ row.publicKeyHash }}</td>
                            <td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})" >{{ row.orderCount }}</td>
                            <td @click="$router.push({name: 'ACMEAccountInfo', params: {accountId: row.id}})" >{{ row.contactUrls }}</td>
						</tr>
					</template>

					<template name="no-result">
						<div v-text="$t('list.noContent')">no content</div>
					</template>
				</account>

				<section class="pagers-table">
					<account-pager type="abbreviated" table="accounts"></account-pager>
				</section>

			</div>
  	    </div>
	</div>
</template>

<script lang="ts" src="./acme-account-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }
</style>
