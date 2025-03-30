<template>
    <div class="row justify-content-center">

        <b-alert :show="dismissCountDown"
                 dismissible
                 :variant="alertType"
                 @dismissed="dismissCountDown=0"
                 @dismiss-count-down="countDownChanged">
            {{alertMessage}}
        </b-alert>
        <br/>

        <div class="col-8">
            <form name="editForm" role="form" novalidate v-on:submit.prevent="save()" v-if="userAccount">
                <h2 id="myUserLabel" v-text="$t('userManagement.home.createOrEditLabel')">Create or edit a User</h2>
                <div>
                    <div class="form-group" :hidden="!userAccount.id">
                        <label v-text="$t('global.field.id')">ID</label>
                        <input type="text" class="form-control" name="id" v-model="userAccount.id" readonly/>
                    </div>

                    <div v-if="!userAccount.managedExternally" class="form-group">
                        <label class="form-control-label" v-text="$t('userManagement.login')">Login</label>
                        <input
                            type="text"
                            class="form-control"
                            name="login"
                            :class="{ valid: !$v.userAccount.login.$invalid, invalid: $v.userAccount.login.$invalid }"
                            v-model="$v.userAccount.login.$model"
                        />

                        <div v-if="$v.userAccount.login.$anyDirty && $v.userAccount.login.$invalid">
                            <small class="form-text text-danger" v-if="!$v.userAccount.login.required"
                                   v-text="$t('entity.validation.required')">
                                This field is required.
                            </small>

                            <small
                                class="form-text text-danger"
                                v-if="!$v.userAccount.login.maxLength"
                                v-text="$t('entity.validation.maxlength', { max: 50 })"
                            >
                                This field cannot be longer than 50 characters.
                            </small>

                            <small class="form-text text-danger" v-if="!$v.userAccount.login.pattern"
                                   v-text="$t('entity.validation.patternLogin')">
                                This field can only contain letters, digits and e-mail addresses.
                            </small>
                        </div>
                    </div>

                    <div v-if="userAccount.managedExternally" class="form-group">
                        <label class="form-control-label" v-text="$t('userManagement.managedExternally')">Externally
                            managed user</label>
                        <input
                            type="text"
                            class="form-control"
                            name="login"
                            disabled
                            v-model="$v.userAccount.login.$model"
                        />
                    </div>

                    <div class="form-group">
                        <label class="form-control-label" for="firstName" v-text="$t('userManagement.firstName')">First
                            Name</label>
                        <input
                            type="text"
                            class="form-control"
                            id="firstName"
                            name="firstName"
                            v-bind:placeholder="$t('settings.form.firstname.placeholder')"
                            :class="{ valid: !$v.userAccount.firstName.$invalid, invalid: $v.userAccount.firstName.$invalid }"
                            :disabled="userAccount.managedExternally"
                            v-model="$v.userAccount.firstName.$model"
                        />
                        <div v-if="$v.userAccount.firstName.$anyDirty && $v.userAccount.firstName.$invalid">
                            <small
                                class="form-text text-danger"
                                v-if="!$v.userAccount.firstName.maxLength"
                                v-text="$t('entity.validation.maxlength', { max: 50 })"
                            >
                                This field cannot be longer than 50 characters.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" for="lastName" v-text="$t('userManagement.lastName')">Last
                            Name</label>
                        <input
                            type="text"
                            class="form-control"
                            id="lastName"
                            name="lastName"
                            v-bind:placeholder="$t('settings.form.lastname.placeholder')"
                            :class="{ valid: !$v.userAccount.lastName.$invalid, invalid: $v.userAccount.lastName.$invalid }"
                            :disabled="userAccount.managedExternally"
                            v-model="$v.userAccount.lastName.$model"
                        />
                        <div v-if="$v.userAccount.lastName.$anyDirty && $v.userAccount.lastName.$invalid">
                            <small
                                class="form-text text-danger"
                                v-if="!$v.userAccount.lastName.maxLength"
                                v-text="$t('entity.validation.maxlength', { max: 50 })"
                            >
                                This field cannot be longer than 50 characters.
                            </small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" for="email" v-text="$t('userManagement.email')">Email</label>
                        <input
                            type="email"
                            class="form-control"
                            id="email"
                            name="email"
                            v-bind:placeholder="$t('global.form.email.placeholder.')"
                            :class="{ valid: !$v.userAccount.email.$invalid, invalid: $v.userAccount.email.$invalid }"
                            :disabled="userAccount.managedExternally"
                            v-model="$v.userAccount.email.$model"
                            email
                            required
                        />
                        <div v-if="$v.userAccount.email.$anyDirty && $v.userAccount.email.$invalid">
                            <small
                                class="form-text text-danger"
                                v-if="!$v.userAccount.email.required"
                                v-text="$t('global.messages.validate.email.required')"
                            >
                                Your email is required.
                            </small>
                            <small class="form-text text-danger" v-if="!$v.userAccount.email.email"
                                   v-text="$t('global.messages.validate.email.invalid')">
                                Your email is invalid.
                            </small>
                            <small
                                class="form-text text-danger"
                                v-if="!$v.userAccount.email.minLength"
                                v-text="$t('global.messages.validate.email.minlength')"
                            >
                                Your email is required to be at least 5 characters.
                            </small>
                            <small
                                class="form-text text-danger"
                                v-if="!$v.userAccount.email.maxLength"
                                v-text="$t('global.messages.validate.email.maxlength')"
                            ></small>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-control-label" for="phone" v-text="$t('userManagement.phone')"></label>
                        <input
                            type="text"
                            class="form-control"
                            id="phone"
                            name="phone"
                            :class="{ valid: !$v.userAccount.phone.$invalid, invalid: $v.userAccount.phone.$invalid }"
                            :disabled="userAccount.managedExternally"
                            v-model="$v.userAccount.phone.$model"
                        />
                        <div v-if="$v.userAccount.phone.$anyDirty && $v.userAccount.phone.$invalid">
                            <small
                                class="form-text text-danger"
                                v-if="!$v.userAccount.phone.maxLength"
                                v-text="$t('global.messages.validate.phone.maxlength')"
                            >
                            </small>
                        </div>
                    </div>

                    <div class="form-check">
                        <label class="form-check-label" for="activated">
                            <input
                                class="form-check-input"
                                :disabled="userAccount.id === null"
                                type="checkbox"
                                id="activated"
                                name="activated"
                                v-model="userAccount.activated"
                            />
                            <span v-text="$t('userManagement.activated')">Activated</span>
                        </label>
                    </div>

                    <div class="form-check">
                        <label class="form-check-label" for="secondFactorRequired">
                            <input
                                class="form-check-input"
                                :disabled="userAccount.id === null"
                                type="checkbox"
                                id="activated"
                                name="activated"
                                v-model="userAccount.secondFactorRequired"
                            />
                            <span v-text="$t('userManagement.secondFactorRequired')"></span>
                        </label>
                    </div>

                    <div v-if="!userAccount.managedExternally" class="form-group">
                        <label class="form-control-label" for="failedLogins" v-text="$t('userManagement.failedLogins')">failedLogins</label>
                        <input
                            class="form-control"
                            disabled
                            type="number"
                            id="failedLogins"
                            name="failedLogins"
                            v-model="userAccount.failedLogins"
                        />
                    </div>

                    <div v-if="!userAccount.managedExternally && userAccount.blockedUntilDate" class="form-group">
                        <label class="form-control-label" for="blockedUntilDate" v-text="$t('userManagement.blockedUntilDate')">blockedUntilDate</label>
                        <input
                            class="form-control"
                            disabled
                            type="datetime"
                            id="blockedUntilDate"
                            name="blockedUntilDate"
                            v-model="userAccount.blockedUntilDate"
                        />
                    </div>
                    <div v-if="!userAccount.managedExternally && userAccount.credentialsValidToDate" class="form-group">
                        <label class="form-control-label" for="blockedUntilDate" v-text="$t('userManagement.credentialsValidToDate')">credentialsValidToDate</label>
                        <input
                            class="form-control"
                            disabled
                            type="datetime"
                            id="blockedUntilDate"
                            name="blockedUntilDate"
                            v-model="userAccount.credentialsValidToDate"
                        />
                    </div>

                    <div class="form-group" v-if="languages && Object.keys(languages).length > 0">
                        <label for="langKey" v-text="$t('userManagement.langKey')">Language</label>
                        <select class="form-control" id="langKey" name="langKey" v-model="userAccount.langKey">
                            <option v-for="(language, key) in languages" :value="key" :key="key">{{
                                    language.name
                                }}
                            </option>
                        </select>
                    </div>

                    <!--div class="form-group">
                        <label v-text="$t('userManagement.scnd.factor')">2nd Factor Authentication</label>
                        <select class="form-control" multiple name="scndFactors" v-model="userAccount.scnd-factors">
                            <option v-for="scndFactor of scndFactors" :value="scndFactor" :key="scndFactor">{{
                                    scndFactor
                                }}
                            </option>
                        </select>
                    </div-->

                    <div class="form-group">
                        <label v-text="$t('userManagement.profiles')">Profiles</label>
                        <select class="form-control" multiple name="authority" v-model="userAccount.authorities">
                            <option v-for="authority of authorities" :value="authority" :key="authority">{{
                                    authority
                                }}
                            </option>
                        </select>
                    </div>

                    <div class="form-group" v-if="userAccount.authorities.includes('ROLE_USER')">
                        <label v-text="$t('userManagement.tenant')">Tenant</label>
                        <select class="form-control" name="selectedTenants" v-model="$v.userAccount.tenantId.$model">
                            <option value="" key="0"></option>
                            <option v-for="tenant of tenants" :value="tenant.id" :key="tenant.id">{{
                                    tenant.longname
                                }}
                            </option>
                        </select>
                    </div>

                </div>
                <div>
                    <button type="button" class="btn btn-secondary" v-on:click="previousState()">
                        <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span
                        v-text="$t('entity.action.cancel')">Cancel</span>
                    </button>
                    <button type="submit" :disabled="$v.userAccount.$invalid || isSaving" class="btn btn-primary">
                        <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span
                        v-text="$t('entity.action.save')">Save</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<script lang="ts" src="./user-management-edit.component.ts"></script>
