<template>
  <div class="loading" :class="[full ? 'noPadding' : '']">
    <div class="loadingContent">
        <div class="loadingThree">
            <span></span>
            <span></span>
            <span></span>
            <span></span>
            <span></span>
        </div>
    </div>
  </div>
</template>

<script>
export default {
props:['full']
}
</script>

<style lang="less" scoped>
@height:10.8vh;
.loading{
    box-sizing: border-box;
    position: absolute;
    padding-top: (44/@height);
    height: 100%;
    width: 100%;
    z-index: 100000;
    .loadingContent{
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        // background-color: rgba(0, 0, 0, 0.5);
        backdrop-filter:  blur(.25rem) ;
    }
}
.noPadding{
    padding-top: 0;
}
.loadingThree{
    width: 150px;
    height: 15px;
    display: flex;
    justify-content: center;
}
.loadingThree span{
    display: inline-block;
    width: 15px;
    height: 100%;
    margin-right: 10px;
    background: rgba(26, 159, 255, 0.6);
    animation: load .5s ease infinite;
}
.loadingThree span:last-child{
    margin-right: 0px;
}
@keyframes load{
    0%{
        opacity: 1;
    }
    100%{
        opacity: 0;
    }
}



.loadingThree span:nth-child(2){
    animation-delay:0.05s;
}
.loadingThree span:nth-child(3){
    animation-delay:0.1s;
}
.loadingThree span:nth-child(4){
    animation-delay:0.15s;
}
.loadingThree span:nth-child(5){
    animation-delay:0.2s;
}
</style>
