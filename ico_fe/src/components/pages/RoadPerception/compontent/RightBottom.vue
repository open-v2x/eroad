<template>
  <div>
    <div class="title">
      <span class="caption">路段平均速度</span>
    </div>
    <div class="contentFrame">
      <div class="contentTitle">
        <span>排名</span>
        <span>路段名称</span>
        <span>平均速度</span>
      </div>
      <div class="mainContentFather">
        <ul class="mainContent" ref="element">
          <!-- <transition-group name="list"> -->
          <li v-for="(item, index) in order" :key="index" class="contentColume" @click="sendRoadName(item)"><!--key不知道绑啥好-->
            <span :class="classList[item.colorIndex]">{{ item.index }}</span>
            <div>
              <span>{{ item.road }}</span>
              <span>{{ item.street }}</span>
            </div>
            <span v-if="item.speed" :style="{ color: classList2[item.colorIndex] }">{{ item.speed }}km/h</span>
          </li>
          <!-- </transition-group> -->
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      order: [//模拟数据
        { road: '乐民街', street: '善容路-佳泰路', speed: 49 },
        { road: '乐民街', street: '容德路-容德东路', speed: 46 },
        { road: '乐民街', street: '富家路-善容路', speed: 45 },
        { road: '甘棠路', street: '双文街-金湖北街', speed: 45 },
        { road: '甘棠路', street: '金湖北街-悦容北街', speed: 45 },
        { road: '乐民街', street: '龚庄路-金湖西路', speed: 44 },
        { road: '乐民街', street: '兴贤路-兴贤东路', speed: 43 },
        { road: '乐民街', street: '佳泰路-善容路', speed: 42 },
        { road: '渥城北路', street: '乐安北三街-八于街', speed: 42 },
        { road: '乐民街', street: '容德西路-兴贤东路', speed: 41 },

      ],
      classList: ['first', 'second', 'third', 'fourth'],//左侧序号颜色class
      classList2: ['rgba(255, 0, 0, 1)', 'rgba(255, 153, 0, 1)', 'rgba(255, 255, 0, 1)', 'rgba(0, 255, 0, 1)'],//右侧速度颜色
      animateTimer: null,
      dataLength: null,
      animateLock: false,
    }
  },
  methods: {
    orderSort(order) {//数据按平均速度排序后只剩下前五
      order.sort((a, b) => {
        if (a.speed < b.speed) {
          return -1
        }
        if (a.speed > b.speed) {
          return 1
        }
        if (a.speed = b.speed) {
          return 0
        }
      })
      // order.splice(5)
    },
    addColorIndex(order) {//给数据对象加新的colorIndex属性用于添加颜色样式
      order.forEach((item) => {
        if (item.speed >= 0 && item.speed < 15) {
          item.colorIndex = 0
        } else if (item.speed < 20) {
          item.colorIndex = 1
        } else if (item.speed < 25) {
          item.colorIndex = 2
        } else {
          item.colorIndex = 3
        }
      })
    },
    addIndex(order) {
      for (let i = 0; i < order.length; i++) {
        order[i].index = i + 1
      }
    },
    sendRoadName(data) {
      this.$bus.emit('protrudeRoad',{road:data.road,street:data.street})
    }
  },

  created() {
    this.addColorIndex(this.order)
    this.addIndex(this.order)
    var add = this.order.slice(0, 5)
    add.forEach((item) => {
      this.order.push(item)
    })
    this.$bus.on('road_section_avgspeed_10min', res => {
      this.order = [];
      let data = res.data.slice(0, 10);
      this.dataLength = data.length
      data.forEach(item => {
        this.order.push({ road: item.road_name, street: item.crossroad_s_e, speed: item.avg_speed });
      })
      this.addColorIndex(this.order)
      this.addIndex(this.order)
      while (this.order.length < 10) {
        this.order.push({})
      }
      var add = this.order.slice(0, 5)
      add.forEach((item) => {
        this.order.push(item)
      })
    })
  },
  mounted() {
    var cur = 0;
    var len = 10//不可用this.order.length，created对order的改变时间不确定。
    var target = document.documentElement.clientHeight * (40 / 1080)//根据开始前浏览器视口调整每次移动距离。
    //但不能在运行过程中改变视口大小，因为距离计算是根据初始视口大小计算，无法实时适配。
    let that = this
    var swiper = (delay) => {//函数需要参数较多，不放入methods
      that.timeId = setTimeout(() => {
        cur += 1
        if (cur > len) {
          cur = 0
          that.$refs.element.style.transition = "none"
          that.$refs.element.style.transform = "translateY(0)"
          swiper(1000);
        } else {
          var top = -target * cur
          that.$refs.element.style.transform = `translateY(${top}px)`
          that.$refs.element.style.transition = "all 1s"
          cur === len ? swiper(1000) : swiper(2000)//这是调用下一次的滚动，如果当前cur=len证明滚完了，transition完之后，
          //让他图返回原位，然后再定时，触发下一次。
        }
      }, delay)
    };
    swiper(2000);

  },
  beforeUnmount() {
    clearTimeout(this.timeId)
    this.$bus.off('road_section_avgspeed_10min')
  }
}
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

  .contentTitle {
    height: (40 / @height);
    margin-bottom: (4 / @height);
    padding-left: 20px;
    padding-right: 12px;
    background: linear-gradient(90deg, rgba(118, 175, 249, 0) 0%, rgba(118, 175, 249, 0.15) 51.87%, rgba(118, 175, 249, 0) 100%);
    display: flex;
    justify-content: space-between;

    span {
      color: #199FFF;
      font-weight: 400;
      font-size: 16px;
      line-height: (40 / @height);
    }
  }

  .mainContentFather {
    position: relative;
    overflow: hidden;
    height: (200/@height);
  }

  .mainContent {
    box-sizing: border-box;
    position: absolute;
    display: flex;
    flex-direction: column;
    width: 100%;
    flex: 1;
    padding: 0 20px;

    li {
      // flex: 1;
      height: (40/@height);
      display: flex;
      justify-content: space-between;
      align-items: center;
      overflow: hidden;
      font-weight: 400;
      font-size: 14px;
      list-style-type: none;

      >div {
        display: flex;

        span:first-child {
          color: #199FFF;
          margin-right: 5px;
        }

        span:last-child {
          color: rgba(255, 255, 255, 0.8);
        }
      }

      span {
        display: flex;
        align-items: center;
        justify-content: center;
      }

      >span:first-child {
        width: 28px;
        height: (22 /@height);
        font-size: (14/@height);
        border-bottom-left-radius: 3px;
        border-bottom-right-radius: 3px;
        color: white;
      }
    }
  }
}

.first {
  background: linear-gradient(180deg, rgba(255, 0, 0, 0), rgba(255, 0, 0, 0.6));
}

.second {
  background: linear-gradient(180deg, rgba(255, 153, 0, 0) 0%, rgba(255, 153, 0, 0.6) 100%);
}

.third {
  background: linear-gradient(180deg, rgba(255, 255, 0, 0) 0%, rgba(255, 255, 0, 0.6) 100%);
}

.fourth {
  background: linear-gradient(180deg, rgba(0, 255, 0, 0) 0%, rgba(0, 255, 0, 0.6) 100%);
}

.list-enter-active {
  transition: all 0.8s ease-out;
}

.list-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.list-enter-from {
  transform: translateX(-20px);
  opacity: 0;
}

.list-leave-to {
  transform: translateX(20px);
  opacity: 0;
}
.contentColume {
  &:hover {
    cursor: pointer;
    background-color: #273152;
  }
}
</style>

