<template>
  <div class="table">
    <span >{{props.title}}</span>
    <span class="header">路口名</span>
    <span v-for="item in props.data">
      {{item}}
    </span>
  </div>
  </template>
  
  <script setup>
  import { defineProps, ref } from 'vue';
  const props = defineProps({
    title:{
      type: String,
    },
    data:{
      type: Array
    }
  })
  </script>
  
  <style lang="less" scoped>
  .table{
    flex: 3;
    background-color: rgba(51, 153, 255, 0.1);
  }
.header{
  background-color: rgba(51, 153, 255, 0.3);
}
  </style>