<template>
  <div class="bg">
    <div v-if="lightCurrentColor.left">
      <div class="arrow left arrow-left" :class="[lightCurrentColor.left]"></div>
      <span>
        {{ lightCurrentTime.left }}
      </span>
    </div>
    <div v-if="lightCurrentColor.forward">
      <div class="arrow forward" :class="[lightCurrentColor.forward]"></div>
      <span>
        {{ lightCurrentTime.forward }}
      </span>
    </div>
    <div v-if="lightCurrentColor.right">
      <div class="arrow right arrow-right" :class="[lightCurrentColor.right]"></div>
      <span>
        {{ lightCurrentTime.right }}
      </span>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      message: 'Hello, Vue!',
      lightCurrentTime: {
        left: 0,
        forward: 0,
        right: 0,
      },
      lightCurrentColor: {
        left: "grey",
        forward: "grey",
        right: "grey",
      },
    }
  },

  mounted() {
    console.log(this.message, 'm');
  }
}
</script>

<style scoped lang="less">
.bg {
  position: absolute;
  width: 110px;
  height: 44px;
  border: 0.83px solid #3961AF;
  border-radius: 10px;
  background: rgba(10, 20, 46, 0.8);
  display: flex;
  flex-direction: row;
  > div {
    justify-content: center;
    align-items: center;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    height: 100%;
    width: 100%;
    padding-left: 3px;
    text-align: center;
    font-size: 10px;
    color: #fff;
    line-height: 18px;
    font-family: "Digital Numbers";
  }
  .forward {
    width: 22px;
    height: 22px;
    background: url("../../../../assets/img/signalControlOptimization/lamp-green-forward.png");
    background-size: 100% 100%;
  }
  .left {
    width: 22px;
    height: 22px;
    background: url("../../../../assets/img/signalControlOptimization/lamp-green-left.png");
    background-size: 100% 100%;
  }
  .right {
    width: 22px;
    height: 22px;
    background: url("../../../../assets/img/signalControlOptimization/lamp-green-right.png");
    background-size: 100% 100%;
  }

  .forward.green {
    background: url("../../../../assets/img/signalControlOptimization/lamp-green-forward.png");
    background-size: 100% 100%;
  }

  .forward.red {
    background: url("../../../../assets/img/signalControlOptimization/lamp-red-forward.png");
    background-size: 100% 100%;
  }

  .forward.yellow {
    background: url("../../../../assets/img/signalControlOptimization/lamp-yellow-forward.png");
    background-size: 100% 100%;
  }

  .left.green {
    background: url("../../../../assets/img/signalControlOptimization/lamp-green-left.png");
    background-size: 100% 100%;
  }
  .left.red {
    background: url("../../../../assets/img/signalControlOptimization/lamp-red-left.png");
    background-size: 100% 100%;
  }
  .left.yellow {
    background: url("../../../../assets/img/signalControlOptimization/lamp-yellow-left.png");
    background-size: 100% 100%;
  }

  .right.green {
    background: url("../../../../assets/img/signalControlOptimization/lamp-green-right.png");
    background-size: 100% 100%;
  }
  .right.red {
    background: url("../../../../assets/img/signalControlOptimization/lamp-red-right.png");
    background-size: 100% 100%;
  }
  .right.yellow {
    background: url("../../../../assets/img/signalControlOptimization/lamp-yellow-right.png");
    background-size: 100% 100%;
  }
  // .arrow {
  //   width: 16px;
  //   height: 16px;
  //   background: url("../../../../assets/img/signalControlOptimization/greenArrow.png");
  //   background-size: 100% 100%;
  // }
  // .left {
  //   transform: rotate(270deg);
  // }
  // .right {
  //   transform: rotate(90deg);
  // }
  // .green {
  //   background: url("../../../../assets/img/signalControlOptimization/greenArrow.png");
  //   background-size: 100% 100%;
  // }
  // .red {
  //   background: url("../../../../assets/img/signalControlOptimization/redArrow.png");
  //   background-size: 100% 100%;
  // }
  // .yellow {
  //   background: url("../../../../assets/img/signalControlOptimization/yellowArrow.png");
  //   background-size: 100% 100%;
  // }
  // .grey {
  //   background: url("../../../../assets/img/signalControlOptimization/greyArrow.png");
  //   background-size: 100% 100%;
  // }
}
</style>
