<template>
  <div class="echarts-chart" />
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'EchartsChart',
  props: {
    options: {
      type: Object,
      required: true
    },
    theme: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      chartInstance: null
    }
  },
  watch: {
    options: {
      deep: true,
      handler() {
        this.renderChart()
      }
    }
  },
  mounted() {
    this.renderChart()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
      window.removeEventListener('resize', this.handleResize)
    }
  },
  methods: {
    renderChart() {
      this.chartInstance = echarts.init(this.$el, this.theme)
      this.chartInstance.setOption(this.options)
    },
    handleResize() {
      this.chartInstance.resize()
    }
  }
}
</script>

<style scoped>
.echarts-chart {
  width: 100%;
  height: 100%;
}
</style>
