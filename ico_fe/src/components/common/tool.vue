<template>
<div class="tool">
    <span :class="[active==0 ? 'active' : '']" @click="sendIndex(0)">今日</span>
    <span :class="[active==1 ? 'active' : '']" @click="sendIndex(1)">近7天</span>
    <span :class="[active==2 ? 'active' : '']" @click="sendIndex(2)">近30日</span>
</div>
</template>

<script>
export default {
    props:['getIndex','index'],
    data(){
        return{
            active: 0
        }
    },  
    methods:{
        sendIndex(index){
            this.active = index
            this.getIndex(this.index,index)//echart index索引和按钮的index索引
        }
    }
}
</script>

<style lang="less" scoped>
@height:10.8vh;
.tool{
    z-index: 9999;
    position: absolute;
    right: 5px;
    top: 30px;
    display: flex;
    font-family: 'Noto Sans SC';
    font-weight: 400;
    font-size: 12px;
    line-height: 20px;
    color: #fff;
    span{
        text-align: center;
        width: 54px;
        margin-left: 5px;
        height: (24/@height);
        border: 1px solid #3399FF;
        border-radius: 2px;
        font-family: 'Noto Sans SC';
        font-weight: 400;
        font-size: 12px;
        line-height: (24/@height);
        color: #3399FF;
        background: rgba(0, 0, 51, 0.6);
    }
    .active{
        color: #fff;
        background-color: #3399FF;
    }
    &:hover{
        cursor: pointer;
    }
}
</style>