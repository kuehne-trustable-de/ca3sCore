<template>
    <div v-if="crlUrlStatusList">
        <h2 id="crlUrlStatus-page-heading" v-text="$t('crlUrlStatusList.title')"></h2>

        <!--div class="row">
            <div class="col-md-5">
                <h4 v-text="$t('audits.filter.title')">Filter by date</h4>
                <div class="input-group mb-3">
                    <div class="input-group-prepend">
                        <span class="input-group-text" v-text="$t('audits.filter.from')">from</span>
                    </div>
                    <input type="date" class="form-control" name="start" v-model="fromDate" v-on:change="transition()" required/>

                    <div class="input-group-append">
                        <span class="input-group-text" v-text="$t('audits.filter.to')">To</span>
                    </div>
                    <input type="date" class="form-control" name="end" v-model="toDate" v-on:change="transition()" required/>
                </div>
            </div>
        </div-->

        <div class="alert alert-warning" v-if="!isFetching && crlUrlStatusList && crlUrlStatusList.length === 0">
            <span v-text="$t('crlUrlStatusList.notFound')"></span>
        </div>
        <div class="table-responsive" >
            <table class="table table-sm table-striped">
                <thead>
                    <tr>
                        <th>status</th>
                        <th>nextUdate</th>
                        <th>thisUpdate</th>
                        <th>issuer</th>
                        <th>crl url</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="crlUrlStatus in crlUrlStatusList" :key="crlUrlStatus.crlUrl">

                        <td>{{crlUrlStatus.crlEndpointStatus}}</td>
                        <td><span v-if="crlUrlStatus.nextUpdate">{{$d(Date.parse(crlUrlStatus.nextUpdate), 'short') }}</span></td>
                        <td><span v-if="crlUrlStatus.thisUpdate">{{$d(Date.parse(crlUrlStatus.thisUpdate), 'short') }}</span></td>
                        <td><small>{{crlUrlStatus.issuerName}}</small></td>
                        <td>{{crlUrlStatus.crlUrl}}</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div v-show="crlUrlStatusList && crlUrlStatusList.length > 0">
            <div class="row justify-content-center">
                <jhi-item-count :page="page" :total="totalItems" :itemsPerPage="itemsPerPage"></jhi-item-count>
            </div>
            <div class="row justify-content-center">
                <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./crlUrlStatus.component.ts">
</script>
