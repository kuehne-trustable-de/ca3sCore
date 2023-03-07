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
                    <span v-text="$t('ca3SApp.cSR.subtitle.request.list')">Request List</span>
                </h2>
				<div>
					<div v-for="(filter, index) in filters.filterList" :key="index">
						<select float="left" class="smallSelector fa-1x" v-model="filter.attributeName" name="csrSelectionAttribute">
							<option v-for="csrSelectionItem in csrSelectionItems" :key="csrSelectionItem.itemName" :value="csrSelectionItem.itemName">{{$t(csrSelectionItem.itemName)}}</option>
						</select>

						<select float="left" class="smallSelector fa-1x" v-model="filter.selector" name="csrSelectionChoice">
							<option v-for="item in getSelectorChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
						</select>

                        <select v-if="getInputType(filter.attributeName) === 'set'" float="left" class="smallSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionSet">
                            <option v-for="item in getValueChoices(filter.attributeName)" :key="item" :value="item">{{$t(item)}}</option>
                        </select>
                        <select v-else-if="getInputType(filter.attributeName) === 'pipelineList'" float="left"  style="height: calc(2em + .75rem + 2px)" class="largeSelector"
                                v-model="filter.attributeValueArr" multiple="true" name="pipelineId">
                            <option v-for="item in pipelines" :key="item.id" :value="item.id">{{item.name}}</option>
                        </select>

                        <input v-else-if="(getInputType(filter.attributeName) === 'date') && (filter.selector === 'ON')" type="date" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionValueDate" v-on:keydown.enter="updateTable"/>
                        <input v-else-if=" getInputType(filter.attributeName) === 'date'" type="datetime-local" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionValueDate" v-on:keydown.enter="updateTable"/>

                        <input type="hidden" v-else-if="getInputType(filter.attributeName) === 'boolean'" float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionValueBoolean" v-on:keydown.enter="updateTable"/>
						<input v-else float="left" class="largeSelector fa-1x" v-model="filter.attributeValue" name="csrSelectionValue" v-on:keydown.enter="updateTable"/>

						<button class="addRemoveSelector" float="right" v-if="index === 0" v-on:click="addSelector()">
							<font-awesome-icon icon="plus"></font-awesome-icon>
						</button>
						<button class="addRemoveSelector" float="right" v-if="index > 0" v-on:click="removeSelector(index)">
							<font-awesome-icon icon="minus"></font-awesome-icon>
						</button>
                        <a v-if="index === 0" href="downloadCSV" @click.prevent="downloadCSV()" float="right"><font-awesome-icon icon="file-csv"></font-awesome-icon></a>
					</div>
				</div>

				<requests-table :columns="columns" :data="csrApiUrl" :per-page="20" name="requests">
					<template slot-scope="{ row }">
						<tr>
							<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})">{{ row.id }}</td>
							<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ $t(row.status) }} <router-link v-if="row.certificateId" :to="{name: 'CertInfo', params: {certificateId: row.certificateId}}" >&nbsp;<font-awesome-icon icon="id-card" /></router-link></td>
							<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.subject }}</td>
							<td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ toLocalDate(row.requestedOn) }}</td>
                            <td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.requestedBy }}</td>
                            <td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.acceptedBy }}</td>
                            <td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.pipelineName }}</td>
                            <!--td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.pipelineId }}</td-->
                            <td @click="$router.push({name: 'CsrInfo', params: {csrId: row.id}})" >{{ row.pipelineType }}</td>

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

					<template name="no-result">
						<div v-text="$t('list.noContent')">no content</div>
					</template>
				</requests-table>

				<section class="pagers-table">
					<requests-table-pager type="abbreviated" table="requests"></requests-table-pager>
				</section>
			</div>
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
