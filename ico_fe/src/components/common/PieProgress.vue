<template>
    <div class="pie-progress-wrap">
        <div ref='pieProgress' style="width:100%;height:100%"></div>
    </div>
</template>

<script>
import * as echarts from 'echarts';
import {setSize} from '../../assets/js/echartsSetSize'
export default {
    data() {
        return {
            chart: null
        }
    },
    props: {
        originData: {
            type: Object
        }
    },
    mounted() {
        this.__initChart()
        window.addEventListener('resize', () => {
            this.chart.resize()
        })
    },
    methods: {
        __initChart() {
            this.chart = echarts.init(this.$refs.pieProgress)
             var datas = {
                value: this.originData.percent,
                company: "%",
                ringColor: [{
                    offset: 0,
                    color: '#02d6fc' // 0% 处的颜色
                }, {
                    offset: 1,
                    color: '#50fc73' // 100% 处的颜色
                }]
            }
            let option = {
                title: {
                    text: datas.value + datas.company,
                    // subtext: '救助人口',
                    x: 'center',
                    y: 'center',
                    textStyle: {
                        fontWeight: 'normal',
                        color: '#fdfeff',
                        fontSize: setSize(12)
                    },
                    subtextStyle: {
                        fontSize: setSize(12),
                        color: '#c9c9c9'
                    },
                },
                //  color: ['#282c40'],
                legend: {
                    show: false,
                    data: []
                },
                color: ['#282c40', '#29aa82', '#f1b144'],
                tooltip: {
                    show: false,
                    trigger: 'item',
                    padding: [10, 10, 10, 10],
                    formatter: "{b} :<br/> {d}%",

                },
                series: [{
                        name: 'Line 1',
                        type: 'pie',
                        clockWise: true,
                        radius: ['70%', '78%'],
                        itemStyle: {
                            normal: {
                                label: {
                                    show: false
                                },
                                labelLine: {
                                    show: false
                                }
                            }
                        },
                        hoverAnimation: false,
                        data: [{
                            value: datas.value,
                            name: '',
                            itemStyle: {
                                normal: {
                                    color: { // 完成的圆环的颜色
                                        colorStops: datas.ringColor
                                    },
                                    label: {
                                        show: false
                                    },
                                    labelLine: {
                                        show: false
                                    }
                                }
                            }
                        }, 
                        {
                            name: '',
                            value: 100 - datas.value
                        }]
                    },
                    {
                        name: '',
                        type: 'pie',
                        radius: ['50%', '69%'],
                        center: ['50%', '50%'],
                        label: {
                            show: false,
                            fontSize: 13,
                            color: '#333',

                        }
                    }
                ]
            }
            this.chart.setOption(option)
        }
    }
}
</script>

<style lang="less" scoped>
.pie-progress-wrap {
    height: 100%;
}
</style>