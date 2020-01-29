<template>
    <div class="home row">
			
		<!--div>
			<div class="alert alert-success" v-if="authenticated">
				<span v-if="username" v-text="$t('home.logged.message', { 'username': username})">You are logged in as user "{{username}}"</span>
			</div>

			<div class="alert alert-warning" v-if="!authenticated">
				<span v-text="$t('global.messages.info.authenticated.prefix')">If you want to </span>
				<a class="alert-link" v-on:click="openLogin()" v-text="$t('global.messages.info.authenticated.link')">sign in</a><span v-html="$t('global.messages.info.authenticated.suffix')">, you can try the default accounts:<br/>- Administrator (login="admin" and password="admin") <br/>- User (login="user" and password="user").</span>
			</div>
			<div class="alert alert-warning" v-if="!authenticated">
				<span v-text="$t('global.messages.info.register.noaccount')">You don't have an account yet?</span>&nbsp;
				<router-link class="alert-link" to="/register" v-text="$t('global.messages.info.register.link')">Register a new account</router-link>
			</div>
		</div-->

		<div id="vue-certificates">
		
			<div class="row">
				<div class="col-xs-3">
					<div class="form-group">
						<label for="filter">&nbsp;</label>
						<input type="text" class="form-control" v-model="filter" placeholder="Filter" @keydown="$event.stopImmediatePropagation()">
					</div>
				</div>
		
				<div class="col-xs-12 table-responsive">
					<datatable :columns="columns" :data="getData" :filter="filter" :per-page="10">
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

<script lang="ts" src="./home.component.ts">

</script>
