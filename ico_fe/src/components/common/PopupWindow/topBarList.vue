<template>
  <div class="selectionOuter" @click="changeList">
    <div class="tip" :class="{ activeTip }" ref="tip"></div>
    <ul class="list" ref="list">
      <li class="item" :class="{ active: item.active }" v-for="(item, index) in routes" :key="item + index"
        :value="item.value" @click.stop="doCheck(item)">
        {{ item.name }}
      </li>
    </ul>
  </div>
</template>

<script>
let cssTimer;
export default {
  data() {
    return {
      activeTip: false,
      flag: false,
      routes: [
        { name: "迁入分析", value: "/MigrationAnalysis", active: false },
        { name: "全息路口", value: "/holographicIntersection", active: false },
        { name: "公交态势", value: "/trafficSituation", active: false },
        { name: "平安守护", value: "/Safety", active: false },
        // { name: "道路感知", value: "/aaa", active: false },
        { name: "指标分析", value: "/tables", active: false },
      ],
    };
  },
  mounted() {
    this.routes.forEach((item) => {
      if (item.value === this.$route.path) {
        this.$refs.tip.style.transform = "rotate(0deg)";
        this.$nextTick(() => {
          this.$refs.tip.style.transform = "rotate(-90deg)";
        });
        item.active = true;
        this.$store.commit('setTopbar', { name: item.name, value: item.value })
        this.activeTip = true;
      }
    });
  },
  unmounted() {
    clearTimeout(cssTimer);
    cssTimer = null;
  },
  methods: {
    changeList() {
      if (!this.flag) {
        clearTimeout(cssTimer);
        cssTimer = null;
        this.$refs.list.style.display = "flex";
        this.$refs.tip.style.transform = "rotate(0deg)";
        this.$refs.list.style.opacity = "0";
        this.$nextTick(() => {
          setTimeout(() => {
            this.$refs.list.style.opacity = "1";
          });
        });
      } else {
        this.$refs.tip.style.transform = "rotate(-90deg)";
        this.$refs.list.style.opacity = "0";
        cssTimer = setTimeout(() => {
          this.$refs.list.style.display = "none";
        }, 500);
      }
      this.flag = !this.flag;
    },
    doCheck(item) {
      this.routes.forEach((item) => {
        item.active = false;
      });
      item.active = true;
      this.$router.push(item.value);
      // this.$store.commit('setTopbar',{name:item.name,value:item.value})
    },
  },
};
</script>

<style lang="less" scoped>
@height: 10.8vh;

.selectionOuter {
  position: absolute;
  transform: translateY(-50%);
  top: 50%;
  right: 240px;
  width: 50px;
  height: 50px;
  font-family: "Noto Sans SC";
  font-weight: 400;
  font-size: 16px;
  cursor: pointer;
  z-index: 1000;

  .tip {
    margin-left: 10px;
    margin-top: 15px;
    position: absolute;
    height: (20 / @height);
    width: (20 / @height);
    background: url(../../../assets/img/Overview/tip.png) no-repeat;
    background-size: 100% 100%;
    transform: rotate(-90deg);
    transition: all 1s;
  }

  .activeTip {
    background: url(../../../assets/img/Overview/Slice\ 146.png) no-repeat;
    background-size: 100% 100%;
  }

  .list {
    box-sizing: border-box;
    position: absolute;
    display: none;
    flex-direction: column;
    align-items: flex-start;
    backdrop-filter: blur(5px);
    right: 8px;
    width: 172px;
    border: 2px solid rgba(51, 153, 255, 0.3);
    border-radius: 2px;
    background-color: rgba(51, 153, 255, 0.15);
    top: 58px;
    transition: transform 1s, opacity 0.5s;
    overflow-x: hidden;
    font-size: 0;

    .item {
      line-height: 24px;
      font-size: 16px;
      color: rgba(255, 255, 255, 0.8);
      user-select: none;
    }

    li {
      width: 100%;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background-color: rgba(0, 149, 255, .5);
      }
    }

    .active {
      background-color: rgba(0, 149, 255, 1);
    }
  }
}

ul {
  list-style: none;
}
</style>
