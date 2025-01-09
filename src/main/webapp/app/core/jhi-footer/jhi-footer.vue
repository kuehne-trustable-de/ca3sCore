<template>
    <!--div id="footer" class="footer">
        <p v-text="$t('footer')">brought to you by trustable solutions</p>
    </div-->
    <div id="spokenTextBox" name="spokenTextBox" class="spokenTextBox">spokenTextBox</div>
</template>

<script lang="ts" src="./jhi-footer.component.ts">
</script>

<style scoped>
    .footer {
        text-align: left;
    }
    .spokenTextBox {
        position: fixed;
        bottom: 0;
        left: 0;
        width: 100%;
        text-align: center;
        background-color: yellow;
        opacity: 0.8;
        display: none;
    }
</style>
