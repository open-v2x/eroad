<template>
    <div class="crumbs">
        <el-breadcrumb>
            <el-breadcrumb-item>
                <i class="el-icon-document"></i>
                {{pageTitle}}
            </el-breadcrumb-item>
        </el-breadcrumb>
        <el-button v-if="showBackBtn" class="go-back" @click="goBack">返回</el-button>
    </div>
</template>
<script>
export default {
    name: 'pageTitle',
    props: {
        pageTitle: String,
        showBackBtn: {
            type: Boolean,
            default: true
        }
    },
    methods: {
        goBack() {
            this.BACK_MIXIN()
        }
    }
}
</script>
<style scoped>
    .crumbs{
        position: relative;
        line-height: 25px;
    }
    div.el-breadcrumb{
        line-height: none!important;
    }
    .crumbs .go-back{
        position: absolute;
        top: -14px;
        right: 2px;
    }
</style>


