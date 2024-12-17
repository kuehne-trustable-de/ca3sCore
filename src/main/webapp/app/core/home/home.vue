<template>
    <div>
        <div>
            <div class="wideColumn">
                <div class="box">
                    <div><h4 v-text="$t('home.dashboard.expiringCertificatesByDate')"></h4></div>
                    <bar-chart :chart-data="expiringCertificatesByDate" :options="options"></bar-chart>
                </div>
            </div>
        </div>
        <div>
            <div class="column">
                <div class="box">
                    <div><h4 v-text="$t('home.dashboard.activeCertificatesByKeyAlgo')"></h4></div>
                    <pie-chart :chart-data="activeCertificatesByKeyAlgo"></pie-chart>
                </div>
            </div>
            <div class="column">
                <div class="box">
                    <div><h4 v-text="$t('home.dashboard.activeCertificatesByKeyLength')"></h4></div>
                    <pie-chart :chart-data="activeCertificatesByKeyLength"></pie-chart>
                </div>
            </div>
            <div class="column">
                <div class="box">
                    <div><h4 v-text="$t('home.dashboard.activeCertificatesByHashAlgo')"></h4></div>
                    <pie-chart :chart-data="activeCertificatesByHashAlgo"></pie-chart>
                </div>
            </div>
        </div>
        <div>
            <div class="wideColumn">
                <div class="box">
                    <h4 v-text="$t('home.dashboard.requestsByMonth')"></h4>
                    <div class="row">
                        <div class="col colContent">

                            <select id="requests-years" name="requests-years" v-model="requestsYears" required
                                    v-on:change="updateRequestsByMonth()">
                                <option value="1" v-text="$t('home.dashboard.years', {years: 1})"></option>
                                <option value="2" v-text="$t('home.dashboard.years', {years: 2})"></option>
                                <option value="3" v-text="$t('home.dashboard.years', {years: 3})"></option>
                            </select>
                        </div>
                        <div class="col colContent">

                            <label class="form-control-label" v-text="$t('ca3SApp.PipelineType.ACME')" for="requests-acme"></label>
                            <input type="checkbox" class="form-check" name="requests-acme" id="requests-acme"
                                   v-on:change="updateRequestsByMonth()" v-model="requestsAcme"/>
                        </div>
                        <div class="col colContent">
                            <label class="form-control-label" v-text="$t('ca3SApp.PipelineType.SCEP')" for="requests-scep"></label>
                            <input type="checkbox" class="form-check" name="requests-scep" id="requests-scep"
                                   v-on:change="updateRequestsByMonth()" v-model="requestsScep"/>
                        </div>
                        <div class="col colContent">
                            <label class="form-control-label" v-text="$t('ca3SApp.PipelineType.WEB')" for="requests-web"></label>
                            <input type="checkbox" class="form-check" name="requests-web" id="requests-web"
                                   v-on:change="updateRequestsByMonth()" v-model="requestsWeb"/>
                        </div>
                    </div>
                    <bar-chart :chart-data="requestsByMonthComputed" :options="options"></bar-chart>
                </div>
            </div>
        </div>

    </div>
</template>

<script>
import LineChart from './LineChart.js'
import BarChart from './BarChart.js'
import PieChart from './PieChart.js'

import axios from 'axios';

export default {

    components: {
        LineChart, BarChart, PieChart
    },
    data() {
        return {
            requestsYears: 3,
            requestsAcme: true,
            requestsScep: true,
            requestsWeb: true,
            expiringCertificatesByDate: {},
            activeCertificatesByHashAlgo: {},
            activeCertificatesByKeyAlgo: {},
            activeCertificatesByKeyLength: {},
            requestsByMonth: {},
            options: {
                scales: {
                    yAxes: [
                        {
                            ticks: {
                                beginAtZero: true
                            }
                        }]
                },
                responsive: true,
                maintainAspectRatio: false
            }
        }
    },
    mounted() {
        this.fillData()
    },
    computed: {
        requestsByMonthComputed: function() {
            return this.requestsByMonth;
        }
    },
    methods: {
        __fillData() {
            this.expiringCertificatesByDate = {
                labels: ["Mi", "Do", "Fr", "Sa", "So", "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So", "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So", "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So", "Mo", "Di", "Mi", "Do"],
                datasets: [
                    {
                        label: 'D0',
                        backgroundColor: '#FF0000',
                        data: [0, 0, 0, 0, 0, 0, 0, 1, 3, 1, 4, 14, 0, 4, 0, 0, 0, 0, 0, 2, 2, 2, 1, 0, 0, 0, 2, 0, 0, 2]
                    }
                ]
            }
        },
        fillData() {
            window.console.info('calling fillData ');
            const self = this;

            axios({
                method: 'get',
                url: 'publicapi/expiringCertificatesByDate',
                responseType: 'stream'
            })
                .then(function (response) {
                    window.console.info('expiringCertificatesByDate returns ' + response.data);
                    self.expiringCertificatesByDate = response.data;
                });

            axios({
                method: 'get',
                url: 'publicapi/activeCertificatesByHashAlgo',
                responseType: 'stream'
            })
                .then(function (response) {
                    window.console.info('activeCertificatesByHashAlgo returns ' + response.data);
                    self.activeCertificatesByHashAlgo = response.data;
                });

            axios({
                method: 'get',
                url: 'publicapi/activeCertificatesByKeyAlgo',
                responseType: 'stream'
            })
                .then(function (response) {
                    window.console.info('activeCertificatesByKeyAlgo returns ' + response.data);
                    self.activeCertificatesByKeyAlgo = response.data;
                });

            axios({
                method: 'get',
                url: 'publicapi/activeCertificatesByKeyLength',
                responseType: 'stream'
            })
                .then(function (response) {
                    window.console.info('activeCertificatesByKeyLength returns ' + response.data);
                    self.activeCertificatesByKeyLength = response.data;
                });

            this.loadDataRequestsByMonth();

        },
        loadDataRequestsByMonth(){
            const self = this;
/*
            requestsYears: 3,
                requestsAcme: true,
                requestsScep: true,
                requestsWeb: true,
*/
            axios
                .get('publicapi/requestsByMonth', {
                    params:
                        {
                            years: this.requestsYears,
                            requestsAcme: this.requestsAcme,
                            requestsScep: this.requestsScep,
                            requestsWeb: this.requestsWeb
                        }
                })
                .then(function (response) {
                    window.console.info('requestsByMonth returns ' + response.data);
                    self.requestsByMonth = response.data;
                });

        },
        updateRequestsByMonth(){
            this.loadDataRequestsByMonth();
        }
    }
}
</script>

<style>
.wide {
    width: 100%;
    height: 500px;
    max-height: 500px;
    max-width: 500px;
    margin: 130px auto;
}

.wideColumn {
    float: left;
    padding: 10px;
    width: 100%;
    height: 500px;
    max-height: 500px;
}

.box {
    border: 2px solid #a0a0a0;
    border-radius: 5px;
    padding: 10px;
}

.column {
    float: left;
    padding: 10px;
    width: 33.33%;
}
</style>
