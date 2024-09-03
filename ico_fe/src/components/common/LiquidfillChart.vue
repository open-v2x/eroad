<template>
    <div id="liquidfillChart">

    </div>
</template>
<script>
import * as echarts from 'echarts'
import 'echarts-liquidfill'
import {setSize} from '../../assets/js/echartsSetSize'
export default {
    data(){
        return{
            pieProgressDataGreen:[],
            pieProgressDataBase:[],
            value:0.85,
            value1:0.8,
        }
    },
    mounted(){
        this.init();
    },
    methods:{
        init(){
            let myChart = echarts.init(document.getElementById('liquidfillChart'));
            var data = [this.value, this.value1];
            var option = {
                backgroundColor: 'transparent',
                title: [
                    {
                        text: '90%绿色出行',
                        x: '10%',
                        y: '75%',
                        textStyle: {
                            fontSize: setSize(14),
                            fontWeight: 'bold',
                            color: '#FFFFFF',
                            lineHeight: 16,
                            textAlign: 'center',
                        },
                    },
                    {
                        text: '80%公共出行',
                        x: '60%',
                        y: '75%',
                        textStyle: {
                            fontSize: setSize(14),
                            fontWeight: 'bold',
                            color: '#FFFFFF',
                            lineHeight: 16,
                            textAlign: 'center',
                        },
                    },
                ],
                series: [
                    {
                        type: 'liquidFill',
                        radius: '47%',
                        center: ['25%', '45%'],
                        color: [
                            {
                                type: 'linear',
                                x: 0,
                                y: 0,
                                x2: 0,
                                y2: 1,
                                colorStops: [
                                    {
                                        offset: 0,
                                        color: '#446bf5',
                                    },
                                    {
                                        offset: 1,
                                        color: '#2ca3e2',
                                    },
                                ],
                                globalCoord: false,
                            },
                        ],
                        data: [this.value, this.value], // data个数代表波浪数
                        backgroundStyle: {
                            borderWidth: 10,
                            color: 'RGBA(51, 66, 127, 0.7)',
                        },
                        label: {
                            normal: {
                                textStyle: {
                                    fontSize: setSize(25),
                                    color: '#fff',
                                },
                                formatter: `{d|${this.value * 100}}{e|%}`,
                                rich:{
                                    d:{
                                        fontSize:setSize(25),
                                        fontWeight: 'bold',
                                        verticalAlign:'bottom'
                                    },
                                    e:{
                                        fontSize:setSize(14),
                                        fontWeight: 'bold',
                                        verticalAlign:'bottom',
                                        shadowOffsetY:12
                                    },
                                }
                            },
                        },
                        outline: {
                            // show: false
                            borderDistance: 0,
                            itemStyle: {
                                borderWidth: 2,
                                borderColor: '#112165',
                            },
                        },
                    },


                    { //第二个球的填充
                        type: 'liquidFill',
                        radius: '47%',
                        center: ['75%', '45%'],
                        color: [
                            {
                                type: 'linear',
                                x: 0,
                                y: 0,
                                x2: 0,
                                y2: 1,
                                colorStops: [
                                    {
                                        offset: 0,
                                        color: '#2aa1e3',
                                    },
                                    {
                                        offset: 1,
                                        color: '#08bbc9',
                                    },
                                ],
                                globalCoord: false,
                            },
                        ],
                        data: [this.value1, this.value1], // data个数代表波浪数
                        backgroundStyle: {
                            borderWidth: 1,
                            color: 'RGBA(51, 66, 127, 0.7)',
                        },
                        label: {
                            normal: {
                                textStyle: {
                                    fontSize: setSize(25),
                                    color: '#fff',
                                },
                                formatter: `{d|${this.value1 * 100}}{e|%}`,
                                rich:{
                                    d:{
                                        fontSize:setSize(25),
                                        fontWeight: 'bold',
                                        verticalAlign:'bottom'
                                    },
                                    e:{
                                        fontSize:setSize(14),
                                        fontWeight: 'bold',
                                        verticalAlign:'bottom',
                                        shadowOffsetY:12
                                    },
                                }
                            },
                        },
                                    outline: {
                                        // show: false
                                        borderDistance: 0,
                                        itemStyle: {
                                            borderWidth: 2,
                                            borderColor: '#112165',
                                        },
                                    },
                    },
                ],
            };

            myChart.setOption(option);
        }

    }
}
</script>
<style scoped>
#liquidfillChart{
    width: 100%;
    height: 100%;
    z-index: 999;
}
</style>
