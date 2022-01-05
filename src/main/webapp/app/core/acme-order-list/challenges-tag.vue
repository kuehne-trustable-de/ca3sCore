
<template>

    <div>
        <challenges-table :columns="columns" :data="challengesApiUrl" :per-page="20" name="challenges">
            <template slot-scope="{ row }">
                <tr>
                    <!--td>{{ row.id}}</td-->
                    <td>{{ row.status }}</td>
                    <!--td>{{ row.authorizationType }}</td-->
                    <!--td>{{ row.authorizationValue }}</td-->
                    <td>{{ row.type }}</td>
                    <td>{{ row.value }}</td>
                    <td>{{ toLocalDate(row.validated) }}</td>
                </tr>
            </template>

            <template name="no-result">
                <div v-text="$t('list.noContent')">no content</div>
            </template>
        </challenges-table>

    </div>
</template>

<script lang="ts" src="./challenges-tag.component.ts">

</script>

<style></style>
