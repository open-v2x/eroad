<template>
  <div>
    <div class="title">
      <span class="caption">全域流量监测</span>
    </div>
    <div class="contentFrame" ref="chartFrame">
    </div>
  </div>
</template>

<script setup>
import { setSize } from '@/assets/js/echartsSetSize'
import * as echarts from "echarts"
import { ref, watch, onMounted, getCurrentInstance, defineProps, onUnmounted } from 'vue';
let { proxy } = getCurrentInstance()
let props = defineProps({
  realVolume: {
    type: Array
  },
  projection: {
    type: Array
  }
})
let chartFrame = ref(null)
let chart
let projectionData = []
let realData = []
let timeValue = []
watch(() => props.realVolume, () => {

})
proxy.$bus.on('volumeProjections', (res) => {
  timeValue = []
  let timeSet = new Set()
  projectionData = res[0].map(item => {
    return item.total_flow
  })

  realData = res[1].map(item => {
    timeSet.add(item.date_h.slice(-2)+':00')
    let nextHour = Number(item.date_h.slice(-2))+1
    if(nextHour < 10) {
      nextHour = '0'+nextHour
    }
    timeValue.push(item.date_h.slice(-2)+':00-'+nextHour+':00')
    return item.pred_flow
  })
  console.log(realData)
  chart.clear()
  chart.setOption(getOptions(projectionData, realData, timeValue))
})
onMounted(() => {
  chart = echarts.init(chartFrame.value)
  proxy.$bus.on('resize',()=>{
    chart.resize()
    chart.clear()
    chart.setOption(getOptions(projectionData, realData, timeValue))
  })
})
onUnmounted(()=>{
  proxy.$bus.off('resize')
  proxy.$bus.off('volumeProjections')
})
const getOptions = (realVolume, projection) => {
  let hour = new Date().getHours()
  return {
    legend:{
      show:true,
      right:setSize(14),
      top:setSize(13),
      itemWidth:setSize(18),
      itemHeight:setSize(2),
      data: [{
        name: '预测流量',
        // 强制设置图形为圆。
        icon: 'path://M216.17777813 489.2444448h-163.84a27.30666667 27.30666667 0 1 0 0 54.61333333h163.84a27.30666667 27.30666667 0 1 0 0-54.61333333zM471.04 489.2444448h-163.84a27.30666667 27.30666667 0 1 0 0 54.61333333h163.84a27.30666667 27.30666667 0 1 0 0-54.61333333zM962.56 489.2444448h-145.6355552a27.30666667 27.30666667 0 1 0 0 54.61333333h145.6355552a27.30666667 27.30666667 0 1 0 0-54.61333333zM725.90222187 489.2444448h-163.84a27.30666667 27.30666667 0 1 0 0 54.61333333h163.84a27.30666667 27.30666667 0 1 0 0-54.61333333z',
      },
      {
        name: '实际流量',
        // 强制设置图形为圆。
        icon: 'rect',
      }],
      textStyle:{
        color:'#fff',
        rich: {
          // height:setSize(10),
          a: {
            verticalAlign: 'bottom',
          },
        },
        padding:[0,0,setSize(-2),0],
      }
    },
    tooltip: {
      show: true,
      trigger: 'axis',
    },
    grid:{
      top:'25%',
      left:'16%',
      right:'5%',
      bottom:'18%',
      containLabel: false,
    },
    dataZoom: [{
      type:'slider',
      show: true,
      // handleIcon:'none',
      handleSize:'200%',
      // handleStyle: {
      //   borderWidth: setSize(5)
      // },
      showDataShadow:false,
      startValue:hour < 6 ? 0 : hour - 6,
      endValue:hour < 6 ? 6 : hour,
      // zoomLock:true,
      // brushSelect:false,
      moveHandleSize:setSize(8),
      height:setSize(8),
      bottom:setSize(10),
      showDetail: false,
      borderColor: 'rgba(0,0,0,0)',
      fillerColor: 'transparent'
    },
    {
      type: 'inside',
      start: 0,
      end:6
    },],
    title: {
      text: '累计车次',
      top:'9%',
      left: setSize(15),
      textStyle: {
        fontWeight: '400',
        fontSize: setSize(14),
        color: '#fff'
      },
    },
    xAxis: {
      type: 'category',
      axisTick:{
        show:false
      },
      axisPointer: {
        type: 'shadow'
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(153, 153, 153, 1)'
        }
      },
      data:timeValue,
      axisLabel:{
        color:'#fff',
        fontSize: setSize(14),
        // formatter:(item)=>{
        //   if(item.slice(-5,-3) == '00'){
        //     return item.slice(-8,-3)
        //   }
        // }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: 'rgba(153, 153, 153, 1)'
        }
      },
      splitLine:{
        lineStyle:{
          color:'rgba(255, 255, 255, 0.15)'
        }
      },
      axisLabel:{
        color:'#fff',
        fontSize: setSize(14),
      }
    },
    series: [
      {
        name:'预测流量',
        data: projection,
        type: 'line',
        showSymbol: false,
        itemStyle: {
          color: 'rgba(255, 236, 63, 1)',
        },
        lineStyle: {
          type: 'dashed',
        },
        symbolSize:0
      },
      {
        name:'实际流量',
        data: realVolume,
        type: 'line',
        showSymbol: false,
        itemStyle: {
          color: 'rgba(51, 255, 255, 1)'
        },
        symbolSize:0
      }
    ]
  }
}
</script>

<style lang="less" scoped>
.contentFrame {
  flex: 1;
}
</style>
