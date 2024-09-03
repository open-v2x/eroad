<template>
  <div class="lightContent">
    <loading v-show="loadingFlag" ></loading>
    <div class="selection">
      <span class="config">路口选择</span>
      <el-select v-model="intersection" class="input1" filterable placeholder=" " default-first-option  @blur="onTypeBlur($event)">
        <el-option v-for="item in intersectionOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <span class="config">状态选择</span>
      <el-select v-model="state" class="input1 input2"  placeholder="请选择状态" @change="periodChange">
        <el-option v-for="item in stateOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <div class="button active" @click="select(intersection, state)">搜索</div>
      <div class="button negative" @click="reset">重置</div>
    </div>
      <div class="tableList">
        <tableLeft title="设计方案" :data="names"></tableLeft>
        <tableRight title="设计方案" :data="dataDesign"></tableRight>
        <tableRight title="实际方案" :data="dataReal"></tableRight>
        <tableRight title="方案差值" :data="dataDiff"></tableRight>
      </div>
      <div v-show="names.length==0" class="prompt">
        <span>无数据</span>
      </div>
    <div class="pagination">
      <span>共{{ total }}条</span>
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="5" v-model:current-page="pageIndex"/>
    </div>
  </div>
</template>

<script setup>
import loading from '@/components/common/loading.vue'
import { trafficLight } from '../../api.js'
import tableLeft from './tableLeft.vue';
import tableRight from './tableRight.vue';
import { onMounted, ref, watch } from 'vue';
let intersection = ref('')
let state = ref('%平峰%')
let intersectionOptions = ref()
let total = ref(20)
let pageIndex = ref()
let loadingFlag = ref(false)
let stateOptions = ref([{ value: '%平峰%', label: '平峰' }, { value: '%早高峰%', label: '早高峰' }, { value: '%晚高峰%', label: '晚高峰' }])
const dataDesign = ref([[0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6]])
const dataReal = ref([[0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6]])
const dataDiff = ref([[0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6], [0.6, 0.6, 0.6]])
const names = ref(['甘棠路-佳泰路', '甘棠路-佳泰路', '甘棠路-佳泰路', '甘棠路-佳泰路', '甘棠路-佳泰路'])
let dataDesignRaw = []
let dataRealRaw = []
let dataDiffRaw = []
let namesRaw = []
let firstFetch = false


watch(pageIndex,(now)=>{
  names.value = namesRaw.slice((now-1)*5,now*5)
  dataDesign.value = dataDesignRaw.slice((now-1)*5,now*5)
  dataReal.value = dataRealRaw.slice((now-1)*5,now*5)
  dataDiff.value = dataDiffRaw.slice((now-1)*5,now*5)
})

onMounted(() => {
  select('%%', state.value)
})


const select = (crossroad_name, period) =>{
  loadingFlag.value = true
  trafficLight({crossroad_name:'%'+crossroad_name+'%',period}).then(res=>{
    loadingFlag.value = false
    dataDesignRaw = []
    dataRealRaw = []
    dataDiffRaw = []
    namesRaw = []
    total.value = res.data.length
    if(!firstFetch){
      intersectionOptions.value = res.data.map(item => {
        return {value:item.crossroad_name,label:item.crossroad_name}
      })
      // intersection.value = intersectionOptions.value[0].value
      firstFetch = true
    }
    res.data.forEach(item=>{
      namesRaw.push(item.crossroad_name)
      // 请求接口，数据缺了的话就显示一个'/',0就显示0
      dataDesignRaw.push([item.green_design ?? '/',item.red_design ?? '/',item.offset_design ?? '/'])
      dataRealRaw.push([item.greenpersistenttime ?? '/',item.redpersistenttime ?? '/',item.cross_offset ?? '/'])
      dataDiffRaw.push([item.green_difference ?? '/',item.red_difference ?? '/',item.offset_difference ?? '/'])
    })
    // 获取完数据设置分页页码到1,之前就是1的话不触发watch,就在下方切前五显示,如果之前不是1,则触发watch在回调中切。
    let pageIndexBefore = pageIndex.value
    pageIndex.value = 1
    if(pageIndexBefore===pageIndex.value){
      names.value = namesRaw.slice(0,5)
      dataDesign.value = dataDesignRaw.slice(0,5)
      dataReal.value = dataRealRaw.slice(0,5)
      dataDiff.value = dataDiffRaw.slice(0,5)
    }
  })
}

const periodChange = (now) => {
  select(intersection.value,now)
}

const reset = () => {
  intersection.value = ''
  state.value = '%平峰%'
  select(intersection.value,'%平峰%')
}

const onTypeBlur = (e) => {
  // setTimeout(()=>{
  //   if (e.target.value=='') {
  //     intersection.value = '';
  //   }
  // })
}
</script>

<style lang="less" scoped>
.prompt {
  width: 100%;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  background-color: rgba(51, 153, 255, 0.1);
}
.lightContent {
  position: relative;
  margin: 16px 20px 0;
  flex: 1;
  color: #fff;
}

.selection {
  display: flex;
  margin-bottom: 17px;
}

.button {
  display: flex;
  align-items: center;
  text-align: center;
  user-select: none;
  justify-content: center;
  height: 30px;
  width: 80px;
  border-radius: 2px;
  cursor: pointer;
  margin-right: 16px;
}

.active {
  background-color: #3399ff;
  color: #fff;
  font-weight: 700;

  &:hover {
    background-color: #79bbff;
  }

  &:active {
    background-color: #337ecc;
  }
}

.negative {

  box-sizing: border-box;
  border: 2px solid #3399FF;
  border-radius: 2px;
  color: rgba(51, 153, 255, 1);
  font-weight: 700;

  &:hover {
    background-color: rgba(51, 153, 255, .5);
  }

  &:active {
    background-color: rgba(51, 153, 255, .6);
  }
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #3399FF !important;
}

.config {
  font-family: "Noto Sans SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 32px;
  color: rgba(51, 153, 255, 0.8);
}

.input1 {
  margin-left: 8px;
  margin-right: 32px;
}

.input2 {
  width: 120px;
}

.tableList {
  display: flex;
  justify-content: space-between;
  user-select: none;
}

.table {
  text-align: center;
  line-height: 44px;
  color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;

  &:nth-child(even) {
    background-color: rgba(1, 255, 255, 0.1);
  }

  &:nth-child(3) {
    background-color: rgba(255, 255, 255, 0.1);
  }

  margin-right: 1px;
  font-family: 'Noto Sans SC';
  font-style: normal;
  font-weight: 500;
  font-size: 16px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-bottom: 24px;
  margin-top: 16px;

  span {
    margin-right: 16px;
    font-size: 14px;
    color: #999999;

  }
}









:deep(.el-pagination.is-background .el-pager li) {
  background-color: transparent !important;
  box-sizing: border-box;
  border: 2px solid rgba(51, 153, 255, 1);
  color: rgba(51, 153, 255, 1);
}

:deep(.el-pagination.is-background .btn-next) {
  background-color: transparent !important;
  box-sizing: border-box;
  border: 2px solid rgba(51, 153, 255, 1);
  color: rgba(51, 153, 255, 1);
}

:deep(.el-pagination.is-background .btn-prev) {
  background-color: transparent !important;
  box-sizing: border-box;
  border: 2px solid rgba(51, 153, 255, 1);
  color: rgba(51, 153, 255, 1);
}

:deep(.el-pagination  .el-icon) {
  color: #fff;
}


</style>
