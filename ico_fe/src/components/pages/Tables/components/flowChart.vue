<template>
  <div style="width: 100%; height: 100%">
    <echarts-chart :options="transit"/>
  </div>
</template>

<script>
import echartsChart from './echartsChart.vue'

export default {
  name: 'transitChart',
  components: {
    echartsChart
  },
  data() {
    return {
      transit: {
        xAxis: {
          type: 'category',
          data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        },
        yAxis: {
          type: 'value'
        },
        tooltip: {
          trigger: "axis",
          backgroundColor: "rgba(6, 27, 71, 0.7)",
          textStyle: {
            color: "#fff",
          },
          borderWidth: "2",
          borderColor: "rgba(1, 255, 255, 1)",
          axisPointer: {
            type: "line",
          },
        },
        series: [
          {
            data: [120, 200, 150, 80, 70, 110, 130],
            type: 'bar'
          }
        ]
      }
    }
  },
  // mounted() {
  //   this.initChart(this.data)
  // },
  // methods: {
  //   initChart(data) {
  //     if (this.chart) {
  //       this.chart.dispose()
  //     }
  //   }
  // }
}
</script>

<style lang="less" scoped>
</style>