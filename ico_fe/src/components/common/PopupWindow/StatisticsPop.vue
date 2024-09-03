<template>
  <div class="bg">
    <span class="close" @click="$emit('close')"></span>
    <div v-if="popMode == 'crossRoad' || popMode == 'overflow'" class="panel">
      <div>
        <span>路口名称:</span>
        <span>{{ attribute.路口名称 }}</span>
      </div>
      <div v-if="popMode == 'overflow'">
        <span>溢出指数:</span>
        <span>{{ attribute.溢出指数 }}</span>
      </div>
      <div>
        <span>实时车流量:</span>
        <span>{{ attribute.实时车流量 }}</span>
      </div>

      <!-- <div>
                <span>实时人流量:</span>
                <span>{{attribute.实时人流量}}</span>
            </div> -->

      <div>
        <span>拥堵指数:</span>
        <span>{{ attribute.拥堵指数 == null || attribute.拥堵指数 == 0 ? '暂无' : attribute.拥堵指数 }}</span>
      </div>

      <div>
        <span>高峰时段:</span>
        <span>{{ attribute.高峰时段 == null ? '暂无' : attribute.高峰时段 }}</span>
      </div>
    </div>
    <div v-else-if="popMode == 'road'" class="panel">
      <div>
        <span>道路名称:</span>
        <span>{{ attribute.道路名称 }}</span>
      </div>
      <!-- <div>
                <span>实时车流量:</span>
                <span>{{attribute.实时车流量}}</span>
            </div> -->

      <div>
        <span>拥堵指数:</span>
        <span>{{ attribute.拥堵指数 == 0 ? '暂无' : attribute.拥堵指数 }}</span>
      </div>

      <div>
        <span>高峰时段:</span>
        <span>{{ attribute.高峰时段 == null ? '09:00-09:59' : attribute.高峰时段 }}</span>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      attribute: {},
    }
  },
  props: {
    popMode: String
  }
}
</script>
<style lang="less" scoped>
.close {
  position: absolute;
  width: 20px;
  height: 20px;
  top: 8px;
  right: 16px;
  &:hover {
    cursor: pointer;
  }
}

.bg {
  position: absolute;
  z-index: 999;
  min-width: 189px;
  max-width: 230px;
  height: 182px;
  background: url('../../../assets/img/Frame 1000002311.png');
  background-repeat: no-repeat;
  background-size: 100% 100%;

  .panel {
    margin-top: 48px;
    margin-left: 12px;
    margin-right: 12px;
    width: 100%;
    height: calc(100% - 48px);
    display: flex;
    flex-direction: column;

    >div {
      flex: 1;

      >span:nth-child(1) {
        color: #8A9CA8;
        font-family: 'Noto Sans SC';
        font-style: normal;
        font-weight: 400;
        font-size: 14px;
        line-height: 22px;
        margin-right: 8px;
      }

      >span:nth-child(2) {
        color: #FFFFFF;
        height: 22px;
        font-family: 'Noto Sans SC';
        font-style: normal;
        font-weight: 400;
        font-size: 14px;
        line-height: 22px;
      }
    }
  }
}
</style>

