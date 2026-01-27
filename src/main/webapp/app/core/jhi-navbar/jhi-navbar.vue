<template>

    <b-navbar toggleable="md" type="light" class="jh-navbar" >
        <b-alert :show="dismissCountDown"
             dismissible
             :variant="alertType"
             @dismissed="dismissCountDown=0"
             @dismiss-count-down="countDownChanged">
          {{alertMessage}}
        </b-alert>


        <div class="jh-logo-container float-left">
            <b-navbar-toggle right class="jh-navbar-toggler d-lg-none float-right" href="javascript:void(0);"  data-toggle="collapse" target="header-tabs" aria-expanded="false" aria-label="Toggle navigation">
                <font-awesome-icon icon="bars" />
            </b-navbar-toggle>

            <b-navbar-nav class="ml-auto">
                <b-nav-item-dropdown
                    id="about-menu"
                    active-class="active"
                    class="pointer">

                    <span slot="button-content" class="navbar-dropdown-menu">
                        <img src='/app/resource/logo.png'/>
                    </span>

                    <b-dropdown-item to="/">
                        <font-awesome-icon icon="home" />
                        <span v-text="$t('global.menu.home')"></span>
                    </b-dropdown-item>

                    <b-dropdown-item  to="/core/info">
                        <font-awesome-icon icon="info-circle" />
                        <span v-text="$t('global.menu.admin.info')"></span>
                    </b-dropdown-item>

                    <b-dropdown-item :to="{ name: 'HelpTarget', params: {hash: '', showNavBar: 'false', lang: $store.getters.currentLanguage}}" tag="b-dropdown-item" id="help" >
                        <font-awesome-icon icon="question-circle"/>
                        <span v-text="$t('ca3SApp.help.title')"></span>
                    </b-dropdown-item>

                    <b-dropdown-item
                        v-if="hasAnyAuthority('ROLE_ADMIN')"
                        :to="{ name: 'HelpTargetAdmin', params: {hash: '', showNavBar: 'false', lang: $store.getters.currentLanguage}}" tag="b-dropdown-item" id="help" >

                        <font-awesome-icon icon="question-circle"/>
                        <span v-text="$t('ca3SApp.help.admin.title')"></span>
                    </b-dropdown-item>


                </b-nav-item-dropdown>

            </b-navbar-nav>

            <b-navbar-brand logged_in="true" class="logo float-left" b-link to="/" v-if="authenticated">
                <span v-if="username && hasTenant()" v-text="$t('home.logged.tenant.message', { 'username': friendlyName, 'roles': roles, 'tenant':tenant})" class="navbar-version"></span>
                <span v-if="username && !hasTenant()" v-text="$t('home.logged.message', { 'username': friendlyName, 'roles': roles})" class="navbar-version"></span>
            </b-navbar-brand>
        </div>

        <b-collapse v-if="showNavBar" is-nav id="header-tabs">
            <b-navbar-nav class="ml-auto">

                <b-nav-item v-if="!authenticated" to="/pkcsxx" exact>
                    <span>
                        <font-awesome-icon icon="stethoscope" />
                        <span v-text="$t('global.menu.check')"></span>
                    </span>
                </b-nav-item>

                <b-nav-item v-if="authenticated" to="/pkcsxx" exact>
                    <span>
                        <font-awesome-icon icon="cart-plus" />
                        <span v-text="$t('global.menu.request')"></span>
                    </span>
                </b-nav-item>

                <b-nav-item v-if="authenticated" to="/cert-list" exact>
                    <span>
                        <font-awesome-icon icon="id-card" />
                        <span v-text="$t('global.menu.certificates')"></span>
                    </span>
                </b-nav-item>

                <b-nav-item v-if="authenticated" to="/csr-list" exact>
                    <span>
                        <font-awesome-icon icon="gavel" />
                        <span v-text="$t('global.menu.requests')"></span>
                    </span>
                </b-nav-item>

                <b-nav-item-dropdown
                    id="config-menu"
                    v-if="hasAnyAuthority('ROLE_ADMIN')"
                    active-class="active" class="pointer">

                    <b-dropdown-item to="/preference">
                        <font-awesome-icon icon="edit" />
                        <span v-text="$t('global.menu.admin.preference')"></span>
                    </b-dropdown-item>

                    <span slot="button-content" class="navbar-dropdown-menu">
                        <font-awesome-icon icon="th-list" />
                        <span v-text="$t('global.menu.config.main')"></span>
                    </span>

                    <b-dropdown-item to="/confCaConnector">
                        <font-awesome-icon icon="tools" />
                        <span v-text="$t('global.menu.config.caConnectorConfig')"></span>
                    </b-dropdown-item>

                    <b-dropdown-item to="/confPipeline">
                        <font-awesome-icon icon="train" />
                        <span v-text="$t('global.menu.config.pipeline')"></span>
                    </b-dropdown-item>

                    <b-dropdown-item to="/acme-account-list">
                        <font-awesome-icon icon="asterisk" />
                        <span v-text="$t('global.menu.entities.acmeAccount')"></span>
                    </b-dropdown-item>
                    <b-dropdown-item to="/acme-order-list">
                        <font-awesome-icon icon="asterisk" />
                        <span v-text="$t('global.menu.entities.acmeOrder')"></span>
                    </b-dropdown-item>
                    <b-dropdown-item to="/scep-order-list">
                        <font-awesome-icon icon="asterisk" />
                        <span v-text="$t('global.menu.entities.scepOrder')"></span>
                    </b-dropdown-item>
                    <b-dropdown-item to="/bpmn-list">
                        <font-awesome-icon icon="receipt" />
                        <span v-text="$t('global.menu.entities.bpnmProcessInfo')"></span>
                    </b-dropdown-item>
                    <b-dropdown-item to="/request-proxy-config">
                        <font-awesome-icon icon="map" />
                        <span v-text="$t('global.menu.entities.requestProxyConfig')"></span>
                    </b-dropdown-item>
                </b-nav-item-dropdown>
                <b-nav-item-dropdown
                    id="admin-menu"
                    v-if="hasAnyAuthority('ROLE_ADMIN')"
                    :class="{'router-link-active': subIsActive('/admin')}"
                    active-class="active"
                    class="pointer">

                    <span slot="button-content" class="navbar-dropdown-menu">
                        <font-awesome-icon icon="user-plus" />
                        <span v-text="$t('global.menu.admin.main')"></span>
                    </span>

                    <b-dropdown-item to="/admin/user-list">
                        <font-awesome-icon icon="user" />
                        <span id="admin-user-management" v-text="$t('global.menu.admin.userManagement')"></span>
                    </b-dropdown-item>

                    <!-- replaced with implementation above -->
                    <!--b-dropdown-item to="/admin/user-management">
                        <font-awesome-icon icon="user" />
                        <span v-text="$t('global.menu.admin.userManagement')">User management</span>
                    </b-dropdown-item-->
                    <b-dropdown-item to="/admin/tenant">
                        <font-awesome-icon icon="sitemap" />
                        <span v-text="$t('global.menu.admin.tenantManagement')"></span>
                    </b-dropdown-item>

                    <!--b-dropdown-item to="/admin/jhi-tracker">
                        <font-awesome-icon icon="eye" />
                        <span v-text="$t('global.menu.admin.tracker')">User tracker</span>
                    </b-dropdown-item>
                    <b-dropdown-item  to="/admin/jhi-metrics">
                        <font-awesome-icon icon="tachometer-alt" />
                        <span v-text="$t('global.menu.admin.metrics')">Metrics</span>
                    </b-dropdown-item>
                    <b-dropdown-item to="/admin/jhi-health">
                        <font-awesome-icon icon="heart" />
                        <span v-text="$t('global.menu.admin.health')">Health</span>
                    </b-dropdown-item>
                    <b-dropdown-item  to="/admin/jhi-configuration">
                        <font-awesome-icon icon="list" />
                        <span v-text="$t('global.menu.admin.configuration')">Configuration</span>
                    </b-dropdown-item-->

                    <b-dropdown-item  to="/audit-list">
                        <font-awesome-icon icon="bell" />
                        <span v-text="$t('global.menu.admin.audits')"></span>
                    </b-dropdown-item>

                    <b-dropdown-item  to="/admin/logs">
                        <font-awesome-icon icon="tasks" />
                        <span v-text="$t('global.menu.admin.logs')"></span>
                    </b-dropdown-item>

                    <b-dropdown-item  to="/admin/notification">
                        <font-awesome-icon icon="info-circle" />
                        <span v-text="$t('global.menu.admin.notification')"></span>
                    </b-dropdown-item>

                </b-nav-item-dropdown>

                <b-nav-item-dropdown
                    right
                    href="javascript:void(0);"
                    id="account-menu"
                    :class="{'router-link-active': subIsActive('/account')}"
                    active-class="active"
                    class="pointer">
                    <span id="account-menu-span" slot="button-content" class="navbar-dropdown-menu">
                        <font-awesome-icon icon="user" />
                        <span v-text="$t('global.menu.account.main')"></span>
                    </span>

                    <b-dropdown-item v-if="languages.multiLanguage" v-for="(value, key) in languages" :key="`lang-${key}`" v-on:click="changeLanguage(key);"
                                     :class="{ active: isActiveLanguage(key)}">
                        {{value.name}}
                    </b-dropdown-item>


                    <b-dropdown-item v-if="authenticated" to="/account/settings" tag="b-dropdown-item" id="settings">
                        <font-awesome-icon icon="wrench" />
                        <span v-text="$t('global.menu.account.settings')"></span>
                    </b-dropdown-item>
                    <b-dropdown-item v-if="authenticated" to="/account/password" tag="b-dropdown-item" id="password">
                        <font-awesome-icon icon="lock" />
                        <span id="menu-account-password" v-text="$t('global.menu.account.password')"></span>
                    </b-dropdown-item>
                    <b-dropdown-item v-if="authenticated"  v-on:click="logout()" id="logout">
                        <font-awesome-icon icon="sign-out-alt" />
                        <span v-text="$t('global.menu.account.logout')"></span>
                    </b-dropdown-item>
                    <b-dropdown-item v-if="!authenticated && (ssoProvider.length > 0)" v-on:click="doSSOLogin()" id="oidcLogin">
                        <font-awesome-icon icon="sign-in-alt" />
                        <span v-text="$t('global.menu.account.SSOLogin', {'providerName': uiConfig.ssoProviderName})"></span>
                    </b-dropdown-item>

                    <b-dropdown-item v-if="!authenticated && (ldapLoginDomainName.trim().length > 0)" v-on:click="openLdapLogin()" id="ldapLogin">
                        <font-awesome-icon icon="sign-in-alt" />
                        <span v-text="$t('global.menu.account.LdapLogin', { 'ldapLoginDomainName': ldapLoginDomainName})"></span>
                    </b-dropdown-item>

                    <b-dropdown-item v-if="!authenticated"  v-on:click="openLogin()" id="login">
                        <font-awesome-icon icon="sign-in-alt" />
                        <span v-text="$t('global.menu.account.login')"></span>
                    </b-dropdown-item>
                    <b-dropdown-item to="/register" tag="b-dropdown-item" id="register" v-if="!authenticated">
                        <font-awesome-icon icon="user-plus" />
                        <span v-text="$t('global.menu.account.register')"></span>
                    </b-dropdown-item>
                </b-nav-item-dropdown>
            </b-navbar-nav>
        </b-collapse>
    </b-navbar>
</template>

<script lang="ts" src="./jhi-navbar.component.ts">
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
/* ==========================================================================
    Navbar
    ========================================================================== */
.navbar-version {
  font-size: 10px;
/*    color: #ccc; */
    color: #777;
}

.jh-navbar {
/*    background-color: #353d47; */
/*    background-color: #e6e6ff; */
    background-color: #ffffff;
  padding: 0.2em 1em;
}

.jh-navbar .profile-image {
  margin: -10px 0px;
  height: 40px;
  width: 40px;
  border-radius: 50%;
}

.jh-navbar .dropdown-item.active,
.jh-navbar .dropdown-item.active:focus,
.jh-navbar .dropdown-item.active:hover {
/*    background-color: #353d47; */
    background-color: #353d47;
}

.jh-navbar .dropdown-toggle::after {
  margin-left: 0.15em;
}

.jh-navbar ul.navbar-nav {
  padding: 0.5em;
}

.jh-navbar .navbar-nav .nav-item {
  margin-left: 1.5rem;
}

.jh-navbar a.nav-link {
  font-weight: 400;
}

.jh-navbar .jh-navbar-toggler {
  color: #ccc;
  font-size: 1.5em;
  padding: 10px;
}

.jh-navbar .jh-navbar-toggler:hover {
  color: #fff;
}

@media screen and (min-width: 768px) {
  .jh-navbar-toggler {
    display: none;
  }
}

@media screen and (min-width: 768px) and (max-width: 1150px) {
  span span{
    display:none;
  }
}

@media screen and (max-width: 767px) {
  .jh-logo-container {
    width: 100%;
  }
}

.navbar-title {
  display: inline-block;
  vertical-align: middle;
  color: white;
}
/* waiting for bootstrap fix bug on nav-item-dropdown a:active
https://github.com/bootstrap-vue/bootstrap-vue/issues/2219
*/
nav li.router-link-active .navbar-dropdown-menu {
  cursor: pointer;
  color: #fff;
}

/* ==========================================================================
    Logo styles
    ========================================================================== */
.navbar-brand.logo {
  padding: 5px 15px;
}

.logo .logo-img {
  height: 45px;
  display: inline-block;
  vertical-align: middle;
  width: 70px;
}

.logo-img {
  height: 100%;
  background: url("../../../content/images/logo-jhipster.png") no-repeat center
    center;
  background-size: contain;
  width: 100%;
}
</style>
