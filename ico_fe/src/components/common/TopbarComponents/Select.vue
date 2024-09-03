<template>
  <div class="selectionOuter">
    <div class="topbar2" @click="changeList">
      <input @click="open" type="text" v-model="inputValue" readonly placeholder="常规固定配时" class="filter" />
      <div class="tip" ref="tip"></div>
    </div>
    <ul class="list" ref="list">
      <li v-for="(item, index) in videos" :key="item + index" @click="doCheck(item, index)">
        <span class="item">{{ item.name }}</span>
      </li>
    </ul>
  </div>
</template>

<script>
let cssTimer;
export default {
  props: ["index", "getAxios", "getSelectList", "initCrossRoad"],
  data() {
    return {
      videos: [
        { name: "功能演示1", value: vedio_path_one_minute },
        { name: "功能演示2", value: vedio_path },
      ],
      inputValue: "功能演示",
    };
  },
  created() {
  },
  unmounted() {

  },
  methods: {
    changeList() {
      if (!this.flag) {
        clearTimeout(cssTimer);
        cssTimer = null;
        this.$refs.list.style.display = "flex";
        this.$refs.tip.style.transform = "rotate(0deg)";
        // this.$refs.tip.style.transition = "transform 1s,opacity 0.2s;"
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
    doCheck(item, index) {
      this.changeList();
      this.$emit('ChangeVideo', this.videos[index].value)
      this.$store.commit('setVideoIndex', index)
      this.$emit('open')
    },
  },
};
</script>

<style lang="less" scoped>
@height: 10.8vh;

.selectionOuter {
  position: absolute;
  z-index: 999;
  font-family: "Noto Sans SC";
  font-weight: 400;
  font-size: 16px;

  // .filter {
  //   backdrop-filter: blur(3px);
  // }

  .topbar2 {
    position: relative;
    height: (24 / @height);
    display: flex;
    justify-content: flex-start;
    align-items: center;

    &:hover {
      cursor: pointer;
    }

    .tip {
      margin-top: 2.5px;
      position: absolute;
      left: 80px;
      height: (16 / @height);
      width: (16 / @height);
      background: url(../../../assets/img/Overview/tip.png) no-repeat;
      background-size: 100% 100%;
      transform: rotate(-90deg);
      transition: all 1s;
    }
  }

  .list {
    box-sizing: border-box;
    position: absolute;
    display: none;
    flex-direction: column;
    align-items: flex-start;
    width: 100px;
    border: 2px solid rgba(51, 153, 255, 0.3);
    border-radius: 2px;
    background-color: rgba(0, 0, 0, 0.6);
    top: 32px;
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
      height: 36px;
      display: flex;
      justify-content: center;
      align-items: center;

      &:hover {
        background-color: rgba(255, 255, 255, 0.2);

      }
    }
  }
}

ul {
  list-style: none;
}

.labelOuter {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}

input[type="text"] {
  user-select: none;
  box-sizing: border-box;
  display: inline-block;
  width: 100px; // 外部
  height: 36px; //  外部
  outline: none;
  padding-left: 10px;
  background-color: transparent;
  border: none;
  font-size: 16px;
  line-height: (28 / @height);
  color: #fff;

  &::placeholder {
    color: #fff;
  }

  &:hover {
    cursor: pointer;
  }
}

.listItem {
  width: 100%;
  margin: 0;
  list-style: none;
  height: 60px;
  line-height: 55px;
  text-align: center;
  user-select: none;
  font-size: 14;
  color: rgba(255, 255, 255, 0.6);

  &:hover {
    background-color: transparent;
    cursor: auto;
  }
}</style>
