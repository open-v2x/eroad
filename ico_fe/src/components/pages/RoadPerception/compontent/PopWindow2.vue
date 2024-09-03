<template>
  <div v-if="see" class="rankingOuter" >
      <div class="close" @click="close"></div>
      <div class="rankingTop">
          <div class="rankingTopContent">
              <span class="decoration rotate"></span>
              <span class="title">路口拥堵分析</span>
              <span class="decoration "></span>
          </div>
      </div>
      <div class="rankingContent">
        <jamChart />
      </div>
  </div>
</template>

<script>
import jamChart from '../../Tables/components/jamChart/index.vue'
export default {
    components: {
      jamChart
    },
    data(){
        return{
            see:false
        }
    },
    methods:{
        close(){
            this.see = false;
        }
    }
}
</script>

<style lang="less" scoped>
.close{
  position: absolute;
  top: 10px;
  right: 16px;
  height: 16px;
  width: 16px;
  background: url('../../../../assets/img/trafficSituation/Frame\ 427321327.png');
  background-size: 100% 100%;
  &:hover{
    cursor: pointer;
  }
}
.rankingOuter{
  position: absolute;
  backdrop-filter: blur(5px);
  left: 50%;
  top: 50%;
  transform: translate(-50%,-50%);
  padding:20px 10px 10px 10px;
  width: 1200px;
  height: 420px;
  background-color: rgba(0, 0, 51, 0.6);
  border: 1px solid rgba(51, 153, 255, 1);
  border-radius: 4px;
  background-size: 100% 100%;
  z-index: 999;
}
.rankingTop{
  display: flex;
  justify-content: center;
  align-items: center;
  .rankingTopContent{
    width: 100%;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: flex-end;
    &::after{
      content: '';
      position: absolute;
      display: block;
      height: 2px;
      width: 100%;
      background-color: rgba(51, 153, 255, 0.61);
      bottom: -10px;
    }
  }
  .decoration{
    height: 8px;
    width: 300px ;
    background: url('../../../../assets/img/trafficSituation/Frame\ 427322090.png');
    background-size: 100% 100%;
    margin-bottom: 5px;
  }
  .rotate{
    transform: rotateY(180deg);
  }
  .title{
    margin: 0 6px;
    font-size: 16px;
    line-height: 28px;
    font-family: 'Noto Sans SC';
    color: #47EBEB;
  }

}
.rankingContent{
    height: calc(100% - 50px);
    margin-top: 20px;
    overflow:auto;
    overflow-x:hidden;
    position: relative;
  }
/*定义滚动条宽高及背景，宽高分别对应横竖滚动条的尺寸*/
::-webkit-scrollbar {
    width: 4px; /*对垂直流动条有效*/
}

/*定义滚动条的轨道颜色、内阴影及圆角*/
::-webkit-scrollbar-track{
    border-radius: 4px;
    background-color:rgba(1, 255, 255, 0.3);
}

/*定义滑块颜色、内阴影及圆角*/
::-webkit-scrollbar-thumb{
    border-radius: 4px;
    background-color: #01FFFF;
}

/*定义滑块悬停变化颜色、内阴影及圆角*/
::-webkit-scrollbar-thumb:hover{
    background-color: rgba(1, 255, 255, 0.6);
}
</style>
