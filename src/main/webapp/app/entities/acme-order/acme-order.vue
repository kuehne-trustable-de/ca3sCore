<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.acmeOrder.home.title')" id="acme-order-heading">Acme Orders</span>
            <router-link :to="{name: 'AcmeOrderCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-acme-order">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.acmeOrder.home.createLabel')">
                    Create a new Acme Order
                </span>
            </router-link>
        </h2>
        <b-alert :show="dismissCountDown"
            dismissible
            :variant="alertType"
            @dismissed="dismissCountDown=0"
            @dismiss-count-down="countDownChanged">
            {{alertMessage}}
        </b-alert>
        <br/>
        <div class="alert alert-warning" v-if="!isFetching && acmeOrders && acmeOrders.length === 0">
            <span v-text="$t('ca3SApp.acmeOrder.home.notFound')">No acmeOrders found</span>
        </div>
        <div class="table-responsive" v-if="acmeOrders && acmeOrders.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.orderId')">Order Id</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.status')">Status</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.expires')">Expires</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.notBefore')">Not Before</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.notAfter')">Not After</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.error')">Error</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.finalizeUrl')">Finalize Url</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.certificateUrl')">Certificate Url</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.csr')">Csr</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.certificate')">Certificate</span></th>
                    <th><span v-text="$t('ca3SApp.acmeOrder.account')">Account</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="acmeOrder in acmeOrders"
                    :key="acmeOrder.id">
                    <td>
                        <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: acmeOrder.id}}">{{acmeOrder.id}}</router-link>
                    </td>
                    <td>{{acmeOrder.orderId}}</td>
                    <td v-text="$t('ca3SApp.AcmeOrderStatus.' + acmeOrder.status)">{{acmeOrder.status}}</td>
                    <td>{{acmeOrder.expires ? $d(Date.parse(acmeOrder.expires), 'short') : ''}}</td>
                    <td>{{acmeOrder.notBefore ? $d(Date.parse(acmeOrder.notBefore), 'short') : ''}}</td>
                    <td>{{acmeOrder.notAfter ? $d(Date.parse(acmeOrder.notAfter), 'short') : ''}}</td>
                    <td>{{acmeOrder.error}}</td>
                    <td>{{acmeOrder.finalizeUrl}}</td>
                    <td>{{acmeOrder.certificateUrl}}</td>
                    <td>
                        <div v-if="acmeOrder.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: acmeOrder.csr.id}}">{{acmeOrder.csr.id}}</router-link>
                        </div>
                    </td>
                    <td>
                        <div v-if="acmeOrder.certificate">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: acmeOrder.certificate.id}}">{{acmeOrder.certificate.id}}</router-link>
                        </div>
                    </td>
                    <td>
                        <div v-if="acmeOrder.account">
                            <router-link :to="{name: 'ACMEAccountView', params: {aCMEAccountId: acmeOrder.account.id}}">{{acmeOrder.account.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: acmeOrder.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'AcmeOrderEdit', params: {acmeOrderId: acmeOrder.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(acmeOrder)"
                                   variant="danger"
                                   class="btn btn-sm"
                                   v-b-modal.removeEntity>
                                <font-awesome-icon icon="times"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
                            </b-button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <b-modal ref="removeEntity" id="removeEntity" >
            <span slot="modal-title"><span id="ca3SApp.acmeOrder.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-acmeOrder-heading" v-text="$t('ca3SApp.acmeOrder.delete.question', {'id': removeId})">Are you sure you want to delete this Acme Order?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-acmeOrder" v-text="$t('entity.action.delete')" v-on:click="removeAcmeOrder()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./acme-order.component.ts">
</script>
