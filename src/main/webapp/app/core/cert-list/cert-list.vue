<template>
  <div class="row">

		<div id="vue-certificates">
		
			<div class="row">

				<div class="col-xs-3">
				</div>

				<div class="col-xs-12 table-responsive">

					<div v-for="(filter, index) in filters" :key="index">
						<select float="left" class="smallSelector" v-model="filter.attributeName">
							<option v-for="certSelectionItem in certSelectionItems" :key="certSelectionItem.itemName">{{certSelectionItem.itemName}}</option>

						</select>

						<select float="left" class="smallSelector" v-model="filter.selector">
							<option v-for="item in getChoices(filter.attributeName)" :key="item">{{item}}</option>
						</select>

						<input float="left" class="largeSelector" v-model="filter.attributeValue"/>

						<button class="addRemoveSelector" v-if="index == 0" v-on:click="addSelector()">+</button>
						<button class="addRemoveSelector" v-if="index > 0" v-on:click="removeSelector(index)">-</button>
					</div>

					<certificate-table :columns="columns" :data="certApiUrl" :per-page="10">
						<!--template name="footer" scope="{ page }">
							<tr>
								<td :colspan="columns.length">Showing rows {{page.from}} to {{page.to}} of {{page.of}} items.</td>
							</tr>
						</template-->			    	
					</certificate-table>
					
					<section class="pagers-table">
						<!--label>Short</label>
						<datatable-pager v-model="page" type="short"></datatable-pager>
				
						<label>Abbreviated</label>
						<datatable-pager v-model="page" type="abbreviated"></datatable-pager>
				
						<label>Long</label>
						<datatable-pager v-model="page" type="long"></datatable-pager-->
					</section>
				</div>
			</div>
		
		</div>
  </div>
</template>

<script lang="ts" src="./cert-list.component.ts">

</script>
