<template>
<div class="table">
  <span>{{props.title}}</span>
  <div class=" tableRow header">
    <span>东西向</span>
    <span>南北向</span>
    <span>相位差</span>
  </div>
  <ul>
    <li class=" tableRow row" v-for="item in props.data">
      <span :class="content != 0 ? 'active' : ''" v-for="content in item">
        {{content}}
      </span>
    </li>
  </ul>
</div>
</template>

<script setup>
import { ref, defineProps, computed } from "vue"
const props = defineProps({
  title:{
    type: String
  },
  data:{
    type:Array
  }
})
</script>

<style lang="less" scoped>
.table{
  flex: 5;
  display: flex;
  flex-direction: column;
  .row{
    color: #01FFFF;;
  }
  .active{
    color: rgba(255, 98, 77, 1);
  }
}
.tableRow{
  display: flex;
  span{
    flex: 1;
  }
}
.header{
  background-color: rgba(51, 153, 255, 0.3);
}


</style>