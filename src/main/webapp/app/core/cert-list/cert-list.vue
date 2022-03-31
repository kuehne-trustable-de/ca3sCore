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
                    <span v-text="$t('ca3SApp.certificate.subtitle.cert.list')">Certificate List</span>
                </h2>


                <div>
					<div v-for="(filter, index) in filters.filterList" :key="index">
						<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName" name="certSelectionAttribute">
							<option v-for="certSelectionItem in certSelectionItems" :key="certSelectionItem.itemName" :value="certSelectionItem.itemName">{{$t(certSelectionItem.itemName)}}</option>
						</select>

						<select float="left" class="smallSelector fa-1x" v-model="filter.selector" name="certSelectionChoice">
							<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

						<select v-if="getInputType(filter.attributeName) === 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue" name="certSelectionSet">
							<option v-for="item in getValueChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

                        <input v-else-if="(getInputType(filter.attributeName) === 'date') && (filter.selector === 'ON')" type="date" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="certSelectionValueDate" v-on:keydown.enter="updateTable"/>
                        <input v-else-if=" getInputType(filter.attributeName) === 'date'" type="datetime-local" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="certSelectionValueDate" v-on:keydown.enter="updateTable"/>

						<input type="hidden" v-else-if="getInputType(filter.attributeName) === 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="certSelectionValueBoolean" v-on:keydown.enter="updateTable"/>
						<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="certSelectionValue" v-on:keydown.enter="updateTable"/>

						<button class="addRemoveSelector" float="right" v-if="index === 0" v-on:click="addSelector()">
							<font-awesome-icon icon="plus"></font-awesome-icon>
						</button>
                        <button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
                            <font-awesome-icon icon="minus"></font-awesome-icon>
                        </button>
                        <a v-if="index === 0" href="downloadCSV" @click.prevent="downloadCSV()" float="right"><font-awesome-icon icon="file-csv"></font-awesome-icon></a>
                    </div>
				</div>

				<certificate :columns="columns" :data="certApiUrl" :per-page="20" name="certificates">
					<template slot-scope="{ row }">
						<tr>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.id }}</td>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" :style="getSubjectStyle(row.ca, row.selfsigned, row.revoked, row.validTo)">{{ row.subject }}</td>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.issuer }}</td>
							<!--td><router-link :to="{name: 'CertInfo', params: {certificateId: row.id}}" >{{ row.type }}</router-link></td-->
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.keyLength }}</td>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{(row.serialHex.length > 12) ? row.serialHex.substring(0, 6).concat('...', row.serialHex.substring(row.serialHex.length - 4, row.serialHex.length )) : row.serialHex}}</td>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ toLocalDate(row.validFrom)}}</td>
							<td ><router-link :style="getValidToStyle(row.validFrom, row.validTo, row.revoked)" :to="{name: 'CertInfo', params: {certificateId: row.id}}" >{{ toLocalDate(row.validTo) }}</router-link></td>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.hashAlgorithm }}</td>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.paddingAlgorithm }}</td>
							<!--td><router-link :to="{name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.revoked }}</router-link></td-->
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ toLocalDate(row.revokedSince) }}</td>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ $t(row.revocationReason) }}</td>
							<td @click="$router.push({name: 'CertInfo', params: {certificateId: row.id}})" >{{ row.sansString }}</td>
						</tr>
					</template>
				</certificate>

                <div>
                    <section float="left" class="pagers-table">
                        <certificate-pager type="abbreviated" table="certificates" v-model="page"></certificate-pager>
                    </section>
                    <!--select float="right" class="smallSelector fa-1x" name="pageSize">
                        <option key="10" value="10">10</option>
                        <option key="20" value="20" selected="selected">20</option>
                        <option key="50" value="50">50</option>
                    </select-->
                </div>
			</div>
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
