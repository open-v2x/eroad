<template>
    <div id="time-axis" v-show="show">

    </div>
</template>
<script>
import * as echarts from 'echarts'
import {setSize} from '../../assets/js/echartsSetSize'
let myChart = null;
let currentIndex
let timer
export default{
    props:{
        getFatherQuery: {
            type: Function,
            default: () => {
                return Function
            }
        },
        getFatherLightUp:{
            type: Function,
            default: () => {
                return Function
            }
        },
        getFatherPutOut:{
            type:Function,
            default:()=>{
                return Function
            }
        },
        getFatherRoadHeatMap_lightup:{
            type:Function,
            default:()=>{
                return Function
            }
        }
    },
    data(){
        return{
            show:true,
            timeLineData:[],
            startValue:0,
            endValue:0,

        }
    },
    mounted(){
        // console.log('index',currentIndex)
        myChart = echarts.init(document.getElementById('time-axis'));
        timer = setInterval(
            this.update()        
        , 1000*60*3);
        

        window.addEventListener("resize", function () {
            myChart.resize();
        });
        myChart.on('timelinechanged', (params) => {
            // console.log('目前索引',params.currentIndex)
            if(params.currentIndex > -1){
                var today = new Date();      
                let time = today.toJSON().split('T').join(' ').split(' ')[0] + ' ' + this.timeLineData[params.currentIndex] + ':00';
                let res = {}
                res.time = time
                res.unlock = true
                currentIndex = params.currentIndex
                // console.log('目前索引值',res)
                this.$bus.emit('timelinechanged',res)

                // this.getFatherQuery(time,res => {
                //     //先把旧的移除
                //     this.getFatherPutOut();
                //     this.getFatherLightUp();
                // })
                // this.getFatherRoadHeatMap_lightup(time);

            }
            
        });

    },
    unmounted(){
        myChart.dispose()
        currentIndex = null
        clearInterval(timer)
        timer = null
        this.$bus.off('timelinechanged')
    },
    methods:{
        update(){
            this.timeLineData = this.getTimeLineData();
            if(currentIndex > this.timeLineData.length-1){currentIndex = this.timeLineData.length-1}
            let options = {
                timeline: {
                    show: true,
                    axisType: 'category',
                    symbol:'pin',
                    symbolSize:setSize(10),
                    autoPlay: false,   
                    currentIndex: currentIndex,
                    playInterval: 1500,  //播放速度
                    checkpointStyle:{
                        symbol:'pin',
                        symbolSize:setSize(10)
                    },
                    label: {
                        normal: {
                            show: true,
                            color: '#20dbfd',
                            interval: 'auto',
                            position:'bottom',
                            formatter: function(params) {
                                return params
                            }
                        },
                    },
                    lineStyle: {
                        width: 0,
                        show: true,
                        color: '#20dbfd'
                    },
                    itemStyle: {
                        show: true,
                        color: '#20dbfd'
                    },
                    controlStyle: {
                        show: true,
                        color: '#20dbfd',
                        borderColor: '#20dbfd'
                    },
                    progress: {
                        itemStyle: {
                            show: true,
                            color: '#20dbfd'
                        },
                        label: {
                                show: true,
                                color: '#20dbfd',
                                interval: 'auto',
                                position:'bottom',
                        },

                    },
                    left: "0",
                    right: "0",
                    bottom: '0',
                    // padding: [setSize(15), 0],
                    data: this.timeLineData,
                },
            }

            myChart.setOption(options);
            setTimeout(this.getTimeStamp(),0)
            // console.log('index',currentIndex)
            return this.update;
        },
        getTimeStamp(){
            var today = new Date();      
            let time = today.toJSON().split('T').join(' ').split(' ')[0] + ' ' + this.timeLineData[this.timeLineData.length -1] + ':00';
            let res = {}
            res.time = time
            this.$bus.emit('timelinechanged',res)
        },
        formatSeconds(value) {
            var secondTime = parseInt(value); // 秒
            var minuteTime = 0; // 分
            var hourTime = 0; // 小时
            if (secondTime >= 60) {
                minuteTime = parseInt(secondTime / 60);
                secondTime = parseInt(secondTime % 60);
                if (minuteTime >= 60) {
                    hourTime = parseInt(minuteTime / 60);
                    minuteTime = parseInt(minuteTime % 60);
                }
            }
            var result = '';
            //result = '' + (parseInt(secondTime) < 10 ? '0' + parseInt(secondTime) : parseInt(secondTime));
            result = '' + (parseInt(minuteTime) < 10 ? '0' + parseInt(minuteTime) : parseInt(minuteTime));// + ':' + result;
            result = '' + (parseInt(hourTime) < 10 ? '0' + parseInt(hourTime) : parseInt(hourTime)) + ':' + result;
            return result;
        },

        //把当前时间转化为时间轴，放到目录数组中返回
        getTimeLineData() {
            var today = new Date(); 
            var m = Math.floor(today.getMinutes()/10)*10;//取十位
            var h = today.getHours();
            if(m<0){
                h -= 1;
                m = 50;
            }//todo h<0
            m = m<10?'0'+m:m;    //更改显示格式
            h = h<10?'0'+h:h;
            //var time = today.toJSON().split('T').join(' ').split(' ')[0] + ' ' + h +':'+m + ':' + '00'
            let times = h * 60 * 60 + (m * 60)
            //let times = 24 * 60 * 60; // 24小时转化为秒
            let categoryData = [];
            //let values = [];
            for (let i = 0; i < times; i+= 600) {
                categoryData.push(this.formatSeconds(i));
                //values.push(2); // 固定值
            }
            return categoryData;
            // return {
            //     categoryData: categoryData,
            //     values: values,
            // };
        },
    }
}
</script>
<style lang="less" scoped>
    #time-axis{
        position:absolute;
        bottom: 0px;
        left: 463px;
        width: 994px;
        height: 60px;
        z-index: 998;
    }
</style>