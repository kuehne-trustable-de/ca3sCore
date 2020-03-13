<template>
  <div>
    <div >
      <!--bar-chart :chart-data="expiringCertificatesByDate"></bar-chart-->
      <bar-chart class="wideColumn" :chart-data="expiringCertificatesByDate"></bar-chart>
    </div>
    <div >
      <pie-chart class="column" :chart-data="activeCertificatesByHashAlgo"></pie-chart>
      <pie-chart class="column" :chart-data="activeCertificatesByKeyAlgo"></pie-chart>
      <pie-chart class="column" :chart-data="activeCertificatesByKeyLength"></pie-chart>
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
    data () {
      return {
        expiringCertificatesByDate: {},
        activeCertificatesByHashAlgo: {},
        activeCertificatesByKeyAlgo: {},
        activeCertificatesByKeyLength: {},
      }
    },
    mounted () {
      this.fillData()
    },
    methods: {
      __fillData(){
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
      fillData(){
        window.console.info('calling fillData ');
        const self = this;

        axios({
          method: 'get',
          url: 'publicapi/expiringCertificatesByDate',
          responseType: 'stream'
        })
        .then(function (response) {
          window.console.info('expiringCertificatesByDate returns ' + response.data );
          self.expiringCertificatesByDate = response.data;
        });

        axios({
          method: 'get',
          url: 'publicapi/activeCertificatesByHashAlgo',
          responseType: 'stream'
        })
        .then(function (response) {
          window.console.info('activeCertificatesByHashAlgo returns ' + response.data );
          self.activeCertificatesByHashAlgo = response.data;
        });

        axios({
          method: 'get',
          url: 'publicapi/activeCertificatesByKeyAlgo',
          responseType: 'stream'
        })
        .then(function (response) {
          window.console.info('activeCertificatesByKeyAlgo returns ' + response.data );
          self.activeCertificatesByKeyAlgo = response.data;
        });

        axios({
          method: 'get',
          url: 'publicapi/activeCertificatesByKeyLength',
          responseType: 'stream'
        })
        .then(function (response) {
          window.console.info('activeCertificatesByKeyLength returns ' + response.data );
          self.activeCertificatesByKeyLength = response.data;
        });


        
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
    margin:  130px auto;
  }

  .wideColumn {
    text-align: center;
    padding: 20px;
    height: 500px;
    max-height: 500px;
  }
  
  .column {
    float: left;
    width: 33.33%;
  }
</style>