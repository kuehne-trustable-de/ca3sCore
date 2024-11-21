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
                    <span v-text="$t('ca3SApp.user.subtitle.user.list')">User List</span>
                    <div class="d-flex justify-content-end">
                        <router-link custom v-slot="{ navigate }" :to="{ name: 'JhiUserCreate' }">
                            <button @click="navigate" class="btn btn-primary jh-create-entity">
                                <font-awesome-icon icon="plus"></font-awesome-icon>
                                <span v-text="$t('userManagement.home.createLabel')">Create a new User</span>
                            </button>
                        </router-link>
                    </div>
                </h2>


                <div>
					<div v-for="(filter, index) in filters.filterList" :key="index">
						<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName" name="userSelectionAttribute">
							<option v-for="userSelectionItem in userSelectionItems" :key="userSelectionItem.itemName" :value="userSelectionItem.itemName">{{$t(userSelectionItem.itemName)}}</option>
						</select>

						<select float="left" class="smallSelector fa-1x" v-model="filter.selector" name="userSelectionChoice">
							<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

						<select v-if="getInputType(filter.attributeName) === 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue" name="userSelectionSet">
							<option v-for="item in getValueChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

                        <input v-else-if="(getInputType(filter.attributeName) === 'date') && (filter.selector === 'ON')" type="date" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="userSelectionValueDate" v-on:keydown.enter="updateTable"/>
                        <input v-else-if=" getInputType(filter.attributeName) === 'date'" type="datetime-local" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="userSelectionValueDate" v-on:keydown.enter="updateTable"/>

                        <input v-else-if="getInputType(filter.attributeName) === 'serial'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="userSelectionValueSerial" v-on:keydown.enter="updateTable"/>

						<input type="hidden" v-else-if="getInputType(filter.attributeName) === 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="userSelectionValueBoolean" v-on:keydown.enter="updateTable"/>
						<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="userSelectionValue" v-on:keydown.enter="updateTable"/>

						<button class="addRemoveSelector" float="right" v-if="index === 0" v-on:click="addSelector()">
							<font-awesome-icon icon="plus"></font-awesome-icon>
						</button>
                        <button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
                            <font-awesome-icon icon="minus"></font-awesome-icon>
                        </button>
                        <!--a v-if="index === 0" href="downloadCSV" @click.prevent="downloadCSV()" float="right"><font-awesome-icon icon="file-csv"></font-awesome-icon></a-->
                    </div>
				</div>

				<users-table :columns="columns" :data="userApiUrl" :per-page="pageSize" name="users">
					<template slot-scope="{ row }">
						<tr>
							<td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.id }}</td>
							<td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" :style="getLoginStyle(row.blockedUntilDate, row.activated)">{{ row.login }}</td>
							<td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.firstName }}</td>
							<td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.lastName }}</td>
                            <td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.email}}</td>
                            <td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.activated}}</td>
                            <td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.langKey}}</td>
							<td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ toLocalDate(row.createdDate)}}</td>
                            <!--td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.lastModifiedBy }}</td>
                            <td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.managedExternally }}</td-->
                            <td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.failedLogins }}</td>
                            <td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ toLocalDate(row.blockedUntilDate)}}</td>
                            <!--td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ toLocalDate(row.credentialsValidToDate) }}</td-->
                            <td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.tenantName }}</td>
                            <td @click="$router.push({name: 'JhiUserEdit', params: {userId: row.login}})" >{{ row.authorities.join(', ') }}</td>
						</tr>
                    </template>
				</users-table>

                <section class="pagers-table">
                    <users-table-pager type="abbreviated" table="users" v-model="page"></users-table-pager>
                </section>

                <!--div>
                    <select float="right" class="smallSelector fa-1x" name="pageSize">
                        <option key="10" value="10">10</option>
                        <option key="20" value="20" selected="selected">20</option>
                        <option key="50" value="50">50</option>
                    </select>
                </div-->
			</div>
		</div>
	</div>
</template>

<script lang="ts" src="./user-list.component.ts">

</script>

<style>
  table {
    width: 100%;
  }

</style>
