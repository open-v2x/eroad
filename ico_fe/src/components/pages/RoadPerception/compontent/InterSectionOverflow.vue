<template>
  <div>
    <div class="title">
      <span class="caption">路口溢出监测</span>
    </div>
    <div class="contentFrame">
      <div class="contentTitle">
        <span class="leftTitle">路口</span>
        <span>溢出指数</span>
        <span>溢出次数</span>
      </div>
      <div class="listOuter" ref="listOuter">
        <ul class="list" ref="list">
          <li class="overflowListItem" v-for="(item, index) in data" :key="index"
            @click="protrudeIntersection(item.crossroad_name)">
            <span class="leftItem">{{ item.crossroad_name }}</span>
            <span>{{ item.overflow_rate }}</span>
            <span>{{ item.overflow_cnt }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance, onUnmounted } from 'vue'
let { proxy } = getCurrentInstance()
let listOuter = ref(null)
let list = ref(null)
let data = ref([{ crossroad_name: '容德东路-乐安北一街', overflow_rate: 1.21, data_time: '2023/06/13 10:53',overflow_cnt: 3 },
{ crossroad_name: '容德东路-乐安北二街', overflow_rate: 1.21, data_time: '2023/06/13 10:53',overflow_cnt: 1 },
{ crossroad_name: '容德东路-乐安北三街', overflow_rate: 1.11, data_time: '2023/06/13 10:53',overflow_cnt: 2 },
{ crossroad_name: '甘棠路-佳泰路', overflow_rate: 1.02, data_time: '2023/06/13 10:53',overflow_cnt: 1 },
{ crossroad_name: '乐安街-双文北街', overflow_rate: 1.23, data_time: '2023/06/13 10:53',overflow_cnt: 1 },
{ crossroad_name: '崇文北路-乐民街', overflow_rate: 1.21, data_time: '2023/06/13 10:53',overflow_cnt: 1 },
{ crossroad_name: '金湖北街-悦容北街', overflow_rate: 1.07, data_time: '2023/06/13 10:53',overflow_cnt: 2 },
{ crossroad_name: '甘棠路-八于街', overflow_rate: 1.18, data_time: '2023/06/13 10:53',overflow_cnt: 3 },
])
let innerLength = 5
let index = 0
let height
let timer
const protrudeIntersection = (data) => {
  proxy.$bus.emit('protrudeIntersection', data)
}
onMounted(() => {
  height = listOuter.value.offsetHeight / innerLength
  document.documentElement.style.setProperty('--item-height', height + 'px');
  window.onresize = () => {
    height = listOuter.value.offsetHeight / innerLength
    document.documentElement.style.setProperty('--item-height', height + 'px');
  }
  data.value = data.value.concat(data.value.slice(0, innerLength))
  roll(2000)
})
proxy.$bus.on('intersectionOverflow', (apiData) => {
  data.value = apiData.concat(apiData.slice(0, innerLength))
})
const roll = (item) => {
  timer = setTimeout(() => {
    index++
    if (index > data.value.length - innerLength) {
      index = 0
      list.value.style.transition = 'none'
      list.value.style.transform = `translateY(0)`
      roll(1000)
    } else {
      list.value.style.transition = "all 1s"
      list.value.style.transform = `translateY(-${index * height}px)`
      index == data.value.length - innerLength ? roll(1000) : roll(2000)
    }

  }, item)
}
onUnmounted(() => {
  clearTimeout(timer)
})
</script>

<style lang="less" scoped>
@height: 10.8vh;

.contentFrame {
  position: relative;
  margin: (12 / @height);
  flex: 1;
  display: flex;
  flex-direction: column;
  font-family: 'Noto Sans SC';
}

.contentTitle {
  height: (40 / @height);
  margin-bottom: (4 / @height);
  padding: 0 0 0 12px;
  text-align: center;
  background: linear-gradient(90deg, rgba(118, 175, 249, 0) 0%, rgba(118, 175, 249, 0.15) 51.87%, rgba(118, 175, 249, 0) 100%);
  display: flex;
  justify-content: space-between;

  span:nth-child(2) {
    margin: 0 15px 0 20px;
  }

  span {
    color: #199FFF;
    font-weight: 400;
    font-size: 16px;
    line-height: (40 / @height);
  }

  span:last-child {
    flex: 1 1 0;
  }

  span:first-child {
    width: 0;
    flex: 1 1 0;
  }
}

.listOuter {
  display: flex;
  flex-direction: column;
  height: (200/@height);
  overflow: hidden;
}

.list {
  transition: all 1s;
  color: #fff;
}

.overflowListItem {
  padding: 0 0 0 12px;
  height: var(--item-height);
  line-height: var(--item-height);
  display: flex;
  justify-content: space-between;
  text-align: center;
  font-weight: 500;
  align-items: center;
  &:hover {
    cursor: pointer;
    background-color: #273152;
  }

  .leftItem {
    // text-align: left;
  }

  span:nth-child(2) {
    width: 60px;
    margin: 0 20px 0 30px;
    color: #01FFFF;
    font-size: 14px;
  }

  span:last-child {
    color: rgba(255, 255, 255, 0.8);
    font-size: 12px;
    flex: 1 1 0;
  }

  span:first-child {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    flex: 1 1 0;
    white-space: nowrap;
  }
}</style>
