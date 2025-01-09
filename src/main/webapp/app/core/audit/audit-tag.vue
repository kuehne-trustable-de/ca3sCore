
<template>

    <div>
        <div v-if="collapsed">
            <button type="button" id="showHideAudit" class="addRemoveSelector" v-on:click="setCollapsed(false)">
                <font-awesome-icon icon="plus"></font-awesome-icon>
            </button> <b>{{titleContent}}</b>
            <p/>
        </div>
        <div v-if="!collapsed">
            <button type="button" class="addRemoveSelector" v-on:click="setCollapsed(true)">
                <font-awesome-icon icon="minus"></font-awesome-icon>
            </button> <b>{{titleContent}}</b>
        </div>

        <div v-if="!collapsed">
            <audits-table :columns="columns" :data="auditApiUrl" :per-page="10" name="audits">
                <template slot-scope="{ row }">
                    <tr>
                        <td>{{ row.actorName }}</td>
                        <td>{{ row.actorRole }}</td>
                        <td :value="row.contentTemplate">{{ localizedContent(row.contentTemplate, row.plainContent) }}</td>
                        <td>{{ toLocalDate(row.createdOn) }}</td>
                        <td v-if="showLinks">{{ row.links }}</td>
                    </tr>
                </template>

                <template name="no-result">
                    <div v-text="$t('list.noContent')">no content</div>
                </template>
            </audits-table>

            <section class="pagers-table">
                <audits-table-pager type="abbreviated" table="audits"></audits-table-pager>
            </section>

        </div>
    </div>
</template>

<script lang="ts" src="./audit-tag.component.ts">

</script>

<style></style>
