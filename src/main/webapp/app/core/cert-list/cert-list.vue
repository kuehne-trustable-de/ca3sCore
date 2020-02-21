<template>
  <div class="row">

		<div id="vue-certificates">
		
			<div class="row">

				<div class="col-xs-3">
				</div>

				<div class="col-xs-12 table-responsive">

					<div>

						<div v-for="(filter, index) in filters" :key="index">
							<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName">
								<option v-for="certSelectionItem in certSelectionItems" :key="certSelectionItem.itemName">{{certSelectionItem.itemName}}</option>

							</select>

							<select float="left" class="smallSelector fa-1x" v-model="filter.selector">
								<option v-for="item in getChoices(filter.attributeName)" :key="item">{{item}}</option>
							</select>

							<input type="date" v-if="getInputType(filter.attributeName) == 'date'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue"/>
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

					<certificate-table :columns="columns" :data="certApiUrl" :per-page="20">
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
