<template>
    <div class="layer"  ref="section" :class="{active}" @click="showHandel">
        <div class="layer_icon"></div>
        <div class="toolTipPanel" v-show="showTooltip">路段</div>
    </div>
</template>
<script>
import { road_section_jam } from '../../../assets/js/api'
var CoordTransform = require('coordtransform');
let msg;
var heatLineLayer;
var zoneList = [];
var zoneList_heatMap = [];
export default {
    props:{
        time:String
    },
    watch:{
        time(){
            this.roadHeatMap_lightup()
        }
    },
    data(){
        return{
            active: false,
            showTooltip: false,
            geoJsonTemp:{
                //可以不用引号
                "type": "FeatureCollection",
                "features": [
                ]
            },
        }
    },
    mounted(){
        this.tooltip()
    },
    methods:{
        showHandel(){
            if(continerModel == 1){
                this.roadHeatMap();
            }else{
                this.roadHeatMap3d();
            }
        },
        roadHeatMap(){
        if(this.active){//取消方法
            this.active = false;
            this.$emit('getAxisShowList',{index:0,value:false})
            map.off('click');
            if(heatLineLayer){
                loca.remove(heatLineLayer);
                map.remove(zoneList_heatMap);
            }
        }else{
            this.active = true;
            this.$emit('getAxisShowList',{index:0,value:true})
            if(continerModel == 1){
                this.roadHeatMap_lightup(this.time);
                // map.on('click', e => {
                //     console.log('线热量看图',heatLineLayer.queryFeature([e.pixel.x,e.pixel.y]));
                // });
            }
        }
    },
    addHotZone_heatMap(data){
        map.remove(zoneList_heatMap);
        data.forEach(item => {
            let road_lon_lat = JSON.parse(item.road_lon_lat);
            var lngLatStart = CoordTransform.wgs84togcj02(road_lon_lat[0][0],road_lon_lat[0][1]);
            var lngLatEnd = CoordTransform.wgs84togcj02(road_lon_lat[1][0],road_lon_lat[1][1]);
            var polyLineCenter = [(lngLatStart[0]+lngLatEnd[0])/2.00, (lngLatStart[1]+lngLatEnd[1])/2.00];
            var zone = new AMap.Polyline({
                path: [lngLatStart, polyLineCenter, lngLatEnd],   //折线只能输入三个点以上
                strokeColor: '#fff', //线颜色
                strokeOpacity: 0,     //线透明度
                strokeWeight: 20,      //线宽
                strokeStyle: "solid",   //线样式
                strokeDasharray: [10, 5], //补充线样式
                cursor:'pointer',
                zIndex: 116,
                attribute:{
                    '道路名称': item['road_name'],
                    '拥堵指数':item['tci'],
                }
            })
            zone.on('mouseover', (e) => {
                msg = {}
                msg.x = e.pixel.x;
                msg.y = e.pixel.y;
                msg.attribute = e.target._opts.attribute;
                this.$emit('getMessage',msg)
            });

            zoneList_heatMap.push(zone);
        })
        map.add(zoneList_heatMap);
    },
     roadHeatMap_lightup(){
        if(!this.active)  return;
        // 调用函数如果存在热力曾，就remove
        // console.log(this.time)
        road_section_jam({
            data_time:this.time ? this.time : null
        }).then(res => {
            if(heatLineLayer){
                loca.remove(heatLineLayer);
            }
            // console.log('路段热裤力',res);
            var data = res.data;
            var greenline =  JSON.parse(JSON.stringify(this.geoJsonTemp));
            data.forEach(line => {
                var road_lon_lat = JSON.parse(line.road_lon_lat);
                var feature = {
                    "type": "Feature",
                    "properties":{"tci":Number(line.tci),'crossroad_s_e':line.crossroad_s_e},
                    "geometry":
                        {
                            "type": "LineString",
                            "coordinates": [CoordTransform.wgs84togcj02(road_lon_lat[0][0],road_lon_lat[0][1]),CoordTransform.wgs84togcj02(road_lon_lat[1][0],road_lon_lat[1][1])]
                        }

                };
                greenline.features.push(feature);

            })
            this.addHotZone_heatMap(data);

            heatLineLayer = new Loca.PulseLineLayer({
                // loca,
                zIndex: 115,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });

            var greengeo = new Loca.GeoJSONSource({
                data:greenline
            });

            heatLineLayer.setSource(greengeo);
            heatLineLayer.setStyle({
                altitude: 0,
                lineWidth: 3,
                // 脉冲头颜色
                headColor: //,
                (_, feature) => {
                    var value = feature.properties.tci;
                    if(value>8){//红
                        return 'rgba(255,0,0,0.8)';
                    }else if(value > 6){
                        return 'rgba(255,153,0,0.8)';
                    }else if(value > 4){
                        return 'rgba(255,255,0,0.8)';
                    }else if(value > 2){
                        return 'rgba(153,204,0,0.8)';
                    }else{
                        return 'rgba(0,128,0,0.8)';
                    }
                    //return headColors[feature.properties.type - 1];
                },
                // 脉冲尾颜色
                trailColor: 'rgba(10, 26, 41, 0)',
                // 脉冲长度，0.25 表示一段脉冲占整条路的 1/4
                interval: 1,
                // 脉冲线的速度，几秒钟跑完整段路
                duration: 2000
            });
            loca.add(heatLineLayer);
            loca.animate.start();

        })
     },
        roadHeatMap3d(){

        },
        tooltip(){
            this.$refs.section.addEventListener('mouseenter',()=>{
                this.showTooltip = true
            })
            this.$refs.section.addEventListener('mouseleave',()=>{
                this.showTooltip = false
            })
        }
    }
}
</script>
<style scoped lang="less">
    .layer{
        //flex: 1;
        margin-top: 24px;
        width: 48px;
        height: 48px;
        background: radial-gradient(50% 50% at 50% 50%, rgba(25, 159, 255, 0) 0%, rgba(25, 159, 255, 0.45) 100%);
        backdrop-filter: blur(4px);
        border-radius: 24px;
        cursor: pointer;
    }
    .active{
        background: radial-gradient(50% 50% at 50% 50%, rgba(1, 255, 255, 0) 0%, rgba(1, 255, 255, 0.45) 100%) !important;
    }

    .layer_icon{
        width: 28px;
        height: 28px;
        margin: auto;
        margin-top: 10px;
        background: url('../../../assets/img/layer_icon_1.png') no-repeat;
        background-size:100% 100%;
    }
</style>
