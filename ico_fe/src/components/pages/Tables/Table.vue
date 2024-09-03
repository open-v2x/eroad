<template>
  <top-bar :active="6"></top-bar>
  <div class="BG"></div>
  <div class="conTent">
    <div class="outLine">
      <div class="top">
        <div class="topLeft">
          <div class="topLeftComponent">
            <div class="title">通行能力分析</div>
            <transitChart />
          </div>
          <div class="topLeftComponent">
            <div class="title">拥堵分析</div>
            <jamChart />
          </div>
          <div class="topLeftComponent">
            <div class="title">违规分析</div>
            <illegalChart />
          </div>
        </div>
        <div class="topRight">
          <div class="topRightTop">
            <div class="title">路口排名</div>
            <rankChart />
          </div>
          <div class="topRightBottom">
            <div class="title">路口通过率</div>
            <passChart />
          </div>
        </div>
      </div>
      <div class="bottom">
        <div class="title">流量分析</div>
        <flowChart />
      </div>
    </div>
  </div>
</template>

<script>
import topBar from "../../common/TopBar.vue";
import transitChart from './components/transitChart/index.vue'
import jamChart from './components/jamChart/index.vue'
import illegalChart from './components/illegalChart/index.vue'
import rankChart from './components/rankChart/index.vue'
import passChart from './components/passChart/index.vue'
import flowChart from './components/flowChart/index.vue'

export default {
  name: "Table",
  components: {
    topBar,
    transitChart,
    jamChart,
    illegalChart,
    rankChart,
    passChart,
    flowChart
  },
  data() {
    return {}
  }
};
</script>

<style lang="less" scoped>
@height: 10.8vh;
.BG {
  position: absolute;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 51, 1);
}
.title {
  box-sizing: border-box;
  margin-left: 15px;
  margin-top: 15px;
  width: 172px;
  height: 40px;
  background: url(../../../assets/img/table/titleBG.svg);
  background-size: 100% 100%;
  font-family: "Noto Sans SC";
  font-style: normal;
  font-weight: 500;
  font-size: 20px;
  line-height: 40px;
  padding-left: 28px;
  text-align: left;
  color: #fff;
}
.title2{
  background: url(../../../assets/img/table/titleBG2.svg);
  background-size: 100% 100%;
}
.chart {
  flex: 1;
}
.conTent {
  box-sizing: border-box;
  position: absolute;
  top: 90px;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 22px 22px 25px;
  overflow: auto;
  scrollbar-color:  #01ffff rgba(1, 255, 255, 0.3);
  scrollbar-width: thin;
  z-index: 10;
}
.outLine {
  display: flex;
  flex-direction: column;
}
.bottom {
  height: 320px;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: url(../../../assets/img/table/tableBG4.png);
  background-size: 100% 100%;
  margin-bottom: 23px;
  position: relative;
}
.trafficLightDiv{
  // height: 527px;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: url(../../../assets/img/table/tableBG5.png);
  background-size: 100% 100%;
}

#trafficLightDiv{
  height: 320px;
  width: 100%;
}
.top {
  height: 1020px;
  display: flex;
  .topLeft {
    width: 1240px;
    margin-right: 23px;
    .topLeftComponent {
      height: 320px;
      margin-bottom: 20px;
      display: flex;
      flex-direction: column;
      background: url(../../../assets/img/table/tableBG.svg);
      background-size: 100% 100%;
      position: relative;
    }
  }
  .topRight {
    flex: 1;
    .topRightTop {
      height: 660px;
      // margin-bottom: 20px;
      display: flex;
      flex-direction: column;
      background: url(../../../assets/img/table/tableBG2.png);
      background-size: 100% 100%;
    }
    .topRightBottom {
      height: 320px;
      margin-bottom: 20px;
      display: flex;
      flex-direction: column;
      background: url(../../../assets/img/table/tableBG3.png);
      background-size: 100% 100%;
    }
  }
}

/*css主要部分的样式*/
/*定义滚动条宽高及背景，宽高分别对应横竖滚动条的尺寸*/
::-webkit-scrollbar {
  width: 4px; /*对垂直流动条有效*/
}

/*定义滚动条的轨道颜色、内阴影及圆角*/
::-webkit-scrollbar-track 
{
  border-radius: 4px;
  background-color: rgba(1, 255, 255, 0.3);
}

/*定义滑块颜色、内阴影及圆角*/
::-webkit-scrollbar-thumb {
  border-radius: 4px;
  background-color: #01ffff;
}


/*定义滑块悬停变化颜色、内阴影及圆角*/
::-webkit-scrollbar-thumb:hover {
  background-color: rgba(1, 255, 255, 0.6);
}
</style>