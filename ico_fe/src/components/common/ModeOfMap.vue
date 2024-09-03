<template>
    <div class="model">
        <div :class="[model === 1 ? 'active' : '']" @click="select1">二维</div>
        <div :class="[model === 2 ? 'active' : '']" @click="select2">三维</div>
    </div>
</template>
<script>
export default {
    data(){
        return{
            model:0
        }
    },
    mounted(){
        //全局方便  1:2d 2:3d
        this.select1();
    },
    methods:{
        select1(){
            window.continerModel = 1;
            this.model = continerModel;
          setTimeout(() => {
            this.$bus.emit('turnContiner',continerModel);
            // console.log('综合态势地图选择1',continerModel)
          }, 20);
          // this.$bus.emit('turnContiner',continerModel);
        },
        select2(){
            window.continerModel = 2;
            this.model = continerModel;
          setTimeout(() => {
            this.$bus.emit('turnContiner',continerModel);
            // console.log('综合态势地图选择1',continerModel)
          }, 20);
            // this.$bus.emit('turnContiner',continerModel);
        }
    }
}
</script>
<style lang="less" scoped>
.model{
    position: absolute;
    top: 185px;
    right: 448px;
    height: 36px;
    width: 160px;
    display: flex;
    flex-direction: row;
    background: rgba(120, 242, 255, 0.15);
    border: 1px solid #3399FF;
    backdrop-filter: blur(8px);
    border-radius: 4px;
    z-index: 998;

    justify-content: center;
    align-items: center;
    padding-left: 3px;
    padding-right: 3px;
    .active{
        background: #3399FF;
        cursor:default;
        pointer-events:none;
    }
    >div{
        flex: 1;
        height: 30px;
        //width: 77px;
        color: white;
        font-family: 'Noto Sans SC';
        font-style: normal;
        font-weight: 400;
        font-size: 14px;
        line-height: 30px;
        cursor: pointer;
        border-radius: 4px;
        text-align: center;
        vertical-align: middle;
        pointer-events: auto;
    }

}
</style>
