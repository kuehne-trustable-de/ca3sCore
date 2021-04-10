
<template>

    <div>
        <audits-table :columns="columns" :data="auditApiUrl" :per-page="20" name="audits">
            <template slot-scope="{ row }">
                <tr>
                    <td>{{ row.actorName }}</td>
                    <td>{{ row.actorRole }}</td>
                    <td>{{ localizedContent(row.contentTemplate, row.plainContent) }}</td>
                    <td>{{ toLocalDate(row.createdOn) }}</td>
                    <td v-if="showLinks">{{ row.links }}</td>
                </tr>
            </template>

            <template name="no-result">
                <div v-text="$t('list.noContent')">no content</div>
            </template>
        </audits-table>

    </div>
</template>

<script lang="ts" src="./audit-tag.component.ts">

</script>

<style></style>
