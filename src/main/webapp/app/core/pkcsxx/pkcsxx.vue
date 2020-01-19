<template>
    <div class="home row">
        <div class="col-md-9">

			<div id="vue-certificates">
			
			    <div class="row">
			        <div class="col-xs-3">
			            <div class="form-group">
			                <label for="filter">&nbsp;</label>
			                <input type="text" class="form-control" v-model="filter" placeholder="Filter" @keydown="$event.stopImmediatePropagation()">
			            </div>
			        </div>
			    </div>
			
				<div class="col-xs-12 table-responsive">
			    	<datatable :columns="columns" :data="getData">
					    <!--template name="footer" scope="{ page }">
					        <tr>
					            <td :colspan="columns.length">Showing rows {{page.from}} to {{page.to}} of {{page.of}} items.</td>
					        </tr>
					    </template-->			    	
					</datatable>
					
				    <section class="pagers-table">
				        <label>Short</label>
				        <datatable-pager v-model="page" type="short"></datatable-pager>
				
				        <label>Abbreviated</label>
				        <datatable-pager v-model="page" type="abbreviated"></datatable-pager>
				
				        <label>Long</label>
				        <datatable-pager v-model="page" type="long"></datatable-pager>
				    </section>
        		</div>
			
			</div>
        </div>
    </div>
</template>

<script lang="ts" src="./pkcsxx.component.ts">

</script>
