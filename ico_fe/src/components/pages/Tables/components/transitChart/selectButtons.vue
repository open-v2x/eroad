<template>
  <div class="buttons">
    <div :class="['button', active ? 'default' : 'active', { active2: !active, default2: active }]" @click="changeActive(false)">
      <slot name="right"></slot>
    </div>
    <div :class="['button', active ? 'active' : 'default', { active2: active, default2: !active }]" @click="changeActive(true)">
      <slot name="left"></slot>
    </div>
  </div>  
</template>

<script>

export default {
  props: {
    mode: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      active: this.mode
    };
  },
  watch: {
    mode(newVal) {
      this.active = newVal;
    }
  },
  methods: {
    changeActive(bool) {
      this.active = bool;
      this.$emit('changeTimeQuantumActive', bool);
    }
  }
};
</script>

<style lang="less" scoped>
.buttons{
  margin-right: 20px;
  user-select: none;
  cursor: pointer;
  height: 28px;
  display: flex;
  font-family: 'Noto Sans SC';
  font-style: normal;
  line-height: 28px;
  font-size: 14px;
  text-align: center;
  >div{
    padding:0 8px;
    margin-right: 8px;
    flex: 0 0 auto;
  }
}
.active{
  color: rgba(1, 255, 255, 1);
  font-weight: 700;
  // background: url(@/assets/img/table/buttonActive.svg);
  background-size: 100% 100%;
}
.active2{
  color: #fff;
  font-weight: 700;
  background-image: none;
  background-color: rgba(64, 106, 255, 1);
}
.default{
  color: rgba(255, 255, 255, 0.8);
  // background: url(@/assets/img/table/buttonDefault.svg);
  background-size: 100% 100%;
}
.default2{
  color: rgba(255, 255, 255, 0.8);
  background-image: none;
  background-color: rgba(0, 44, 75, 1);
}
:deep(.el-input__wrapper){
  box-shadow:0 0 0 1px #3399FF !important;
  background-color: rgba(29, 38, 48, 1) !important;
}
</style>
  