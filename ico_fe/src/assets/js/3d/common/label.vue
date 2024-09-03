<template>
    <div :id="id" class="divlabel-container" v-if="show" >
      <div class="animate-maker-border">
        <span class="animate-marker__text">{{ title }}</span>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "DynamicLabel",
    data() {
      return {
        show: true,
      };
    },
    props: {
      title: {
        type: String,
        default: "标题",
      },
      id:{
          type:String,
          default:'001'
      }
    },
  };
  </script>
  
  
  <style lang="less" scoped>
  .divlabel-container {
    position: absolute;
    left: 0;
    bottom: 0;
    pointer-events: none;
    cursor: pointer;
  }
  .animate-maker-border {
    width: 150px;
    height: 30px;
    margin: 0;
    color: #f7ea00;
    box-shadow: inset 0 0 0 1px rgba(247, 234, 0, 0.56);
  }
  .animate-marker__text {
    color: #fff;
    font-size: 14px;
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;
    font-weight: bolder;
    user-select: none;
    cursor: pointer;
    background: rgba(0, 173, 181, 0.32);
  }
  </style>