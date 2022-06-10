<template>
  <div id="app">
    <ribbon></ribbon>
    <div id="app-header">
      <jhi-navbar></jhi-navbar>
    </div>
    <div class="container-fluid">
      <div class="card jh-card">
        <router-view></router-view>
      </div>
      <b-modal id="login-page" hide-footer lazy>
        <span slot="modal-title" id="login-title" v-text="$t('login.title')">Sign in</span>
        <login-form></login-form>
      </b-modal>

      <jhi-footer></jhi-footer>
    </div>
  </div>
</template>

<script lang="ts" src="./app.component.ts">
</script>


<style >

#vue-certificates nav>.pagination>li>span {
	border: none;
	background: none;
	color: inherit;
	padding: 0;
	margin: 0
}

#vue-certificates.tutorial-pager-types .pagers-table {
	display: grid;
	grid-template-columns: 1fr 1fr;
	margin-top: 15px
}

#vue-certificates.tutorial-pager-types .pagers-table ul {
	padding: 0;
	margin: 0 0 5px
}

#vue-certificates .pagination-no-margin .pagination {
	margin: 0
}

dl.inlined-elements {
	display: table
}

dl.inlined-elements>div {
	display: table-row
}

dl.inlined-elements>div>dt,
dl.inlined-elements>div>dd {
	display: table-cell
}

dl.inlined-elements>div>dt::after {
	content: " :\00A0"
}

.alert {
	padding: 10px;
	margin: 10px 0;
	border: 1px solid transparent;
	border-radius: 10px;
	box-shadow: 0 0 4px rgba(0, 0, 0, 0.25)
}

.alert.alert-warning {
	background-color: rgba(254, 209, 56, 0.4);
	border-color: #fed035
}

.alert.alert-info {
	background-color: rgba(0, 132, 255, 0.4);
	border-color: #0082fc
}
</style>
