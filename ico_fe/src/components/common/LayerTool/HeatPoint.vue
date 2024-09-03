<template>
    <div class="layer" ref="road" :class="{active}" @click="showHandel">
        <div class="layer_icon" ></div>
        <div class="toolTipPanel" v-show="showTooltip">路口</div>
    </div>

</template>
<script>

import {crossroad_detail_list} from '../../../assets/js/api'
import breath_red from'@/assets/img/rb.png'
import breath_yellow from'@/assets/img/yb.png'
import breath_orange from'@/assets/img/ob.png'
import unobstructedImg from '@/assets/img/unobstructedStatic.png'
import breath_green from'@/assets/img/green.png'
var CoordTransform = require('coordtransform');
let green,yellow,orange,red,unobstructed;
let green_scatter,yellow_scatter,orange_scatter,red_scatter,unobstructed_scatter;
var zoneList = [];
let msg
export default {
    props:{
        time:String
    },
    watch:{
        time(now,old){
            if(now){
                this.firstQuery(now)
            }
        }
    },
    data(){
        return{
            active: false,
            geoJsonTemp:{
                //可以不用引号
                type: "FeatureCollection",
                features: [

                ]
            },
            showTooltip: false,

        }
    },
    mounted(){
        this.tooltip()
    },
    methods:{
        showHandel(){
            if(continerModel == 1){
                this.breathPointMap();
            }else{
                this.breathPointMap3d();
            }
        },
        firstQuery(){
            this.firstQuery = function(time){
                this.query(time,() => {
                    this.putOut();
                    this.lightUp();
                })
            }
        },
        query(time,callback){
            //接口
            time = time ? time : null
            crossroad_detail_list({
            "data_time": time
            }).then(res => {
                //res包含状态码等一堆信息，只需要其中的data数据
                var data = res.data;
                // console.log("十字路口数据：")
                // console.log(data);
                if(!data)   return;
                red =  JSON.parse(JSON.stringify(this.geoJsonTemp));
                orange = JSON.parse(JSON.stringify(this.geoJsonTemp));
                yellow = JSON.parse(JSON.stringify(this.geoJsonTemp));
                green = JSON.parse(JSON.stringify(this.geoJsonTemp));
                unobstructed = JSON.parse(JSON.stringify(this.geoJsonTemp));
                data.forEach(item => {
                    var feature = {
                        "type": "Feature",
                        "geometry":
                            {
                                "type": "Point",
                                //data中的每一个元素的属性赋值
                                "coordinates": [
                                    item.crossroad_lon,
                                    item.crossroad_lat
                                ]
                            }

                    };
                    //item属性赋值
                    var value = item.tci;

                    if(value>8){//红
                        red.features.push(feature);
                    }else if(value > 6){
                        orange.features.push(feature);
                    }else if(value > 4){
                        yellow.features.push(feature);
                    }else if(value > 2){
                        unobstructed.features.push(feature);
                    }else{
                        green.features.push(feature);
                    }

                });
                //加点击热区
                this.addHotZone(data);
                callback(true);
            })
        },
        breathPointMap(){
            if(this.active){//取消方法
                this.active = false;
                this.$emit('getAxisShowList',{index:1,value:false})
                map.remove(zoneList);  //取消热区圆点
                this.putOut();
            }else{
                // this.putOut();
                this.active = true;
                this.$emit('getAxisShowList',{index:1,value:true})
                if(continerModel == 1){
                    this.query(this.time,() => {
                        //this.$refs.timer.show = true;
                        //this.showLegen = true;
                        this.lightUp();
                    });
                }else{
                    //var radar = new Radar();
                    Cesium.GeoJsonDataSource.load(red
                    , {
                        clampToGround: true, // 设置贴地
                    }).then((dataSource) => {
                        var entities = dataSource.entities.values;
                        for (var o = 0; o < entities.length; o++){
                            var r = entities[o];
                            this.AddCircleScanPostStage(
                                r.position._value,//new Cesium.Cartographic(r.position._value.x, r.position._value.y, 1),
                                100,
                                 Cesium.Color.fromCssColorString('#E80E0E'),
                                3000
                            );

                        }
                    })

                    Cesium.GeoJsonDataSource.load(yellow
                    , {
                        clampToGround: true, // 设置贴地
                    }).then((dataSource) => {
                        var entities = dataSource.entities.values;
                        for (var o = 0; o < entities.length; o++){
                            var r = entities[o];
                            this.AddCircleScanPostStage(
                                r.position._value,//new Cesium.Cartographic(r.position._value.x, r.position._value.y, 1),
                                70,
                                 Cesium.Color.fromCssColorString('#FFD045'),
                                3000
                            );

                        }
                    })

                    Cesium.GeoJsonDataSource.load(green
                    , {
                        clampToGround: true, // 设置贴地
                    }).then((dataSource) => {
                        var entities = dataSource.entities.values;
                        for (var o = 0; o < entities.length; o++){
                            var r = entities[o];
                            this.AddCircleScanPostStage(
                                r.position._value,//new Cesium.Cartographic(r.position._value.x, r.position._value.y, 1),
                                40,
                                 Cesium.Color.fromCssColorString('#4FD27D'),
                                3000
                            );

                        }
                    })
                }
            }
        },
        addHotZone(data){
            if(window.map)
                window.map.remove(zoneList);
            data.forEach(item => {
                var zone = new AMap.Circle({
                    center:CoordTransform.wgs84togcj02(Number(item.crossroad_lon),Number(item.crossroad_lat)),//[Number(item.crossroad_lon),Number(item.crossroad_lat)],
                    radius:50,
                    fillOpacity:0,
                    strokeOpacity:0,
                    zIndex: 17,
                    cursor:'pointer',
                    attribute:{
                        //'viewCom':'LivePlayer',
                        '路口名称':item['crossroad_name'],
                        '实时车流量':item['car_cnt'],
                        // '实时人流量':item['person_cnt'],
                        '拥堵指数':item['tci'],
                        '高峰时段':item['peak_h'],
                        'lng':item['crossroad_lon'],
                        'lat':item['crossroad_lat']
                    }
                })
                zone.on('mousemove', (e) => {
                    msg = {}
                    msg.x = e.pixel.x;
                    msg.y = e.pixel.y;
                    msg.attribute = e.target._opts.attribute;
                    this.$emit('getMessage',msg)
                });
                zone.on('mouseout', (e) => {
                    this.$emit('dontShow',null)
                });
                zone.on('click', (e) => {
                    this.$emit('clickItem', item)
                });

                zoneList.push(zone);
            })
            window.map.add(zoneList);
        },
        lightUp(){
            if(!this.active)  return;
            var new_green = this.layerTrans(green);
            var new_yellow = this.layerTrans(yellow);
            var new_orange = this.layerTrans(orange);
            var new_unobstructed = this.layerTrans(unobstructed)
            var new_red = this.layerTrans(red);

            // 绿色普通点
            var geo = new Loca.GeoJSONSource({
                data:new_green
                //url: '../../assets/config/green_cross.json',
            });
            green_scatter = new Loca.ScatterLayer({
                // loca,
                zIndex: 11,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            green_scatter.setSource(geo);
            green_scatter.setStyle({
                // color: 'rgba(43,156,75,1)',
                // unit: 'meter',
                // size: [80, 80],
                // borderWidth: 0,
                unit: 'meter',
                size: [100, 100],
                texture: breath_green,
                borderWidth: 0,

                // unit: 'meter',
                // size: [100, 100],
                // borderWidth: 0,
                // texture: breath_green,
                // duration: 500,
                // animate: true,
            });
            loca.add(green_scatter);

            // 基本畅通呼吸点
            var geoLevelUnobstructed = new Loca.GeoJSONSource({
                data: new_unobstructed,
            });
            unobstructed_scatter = new Loca.ScatterLayer({
                loca,
                zIndex: 13,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            unobstructed_scatter.setSource(geoLevelUnobstructed);
            unobstructed_scatter.setStyle({
                unit: 'meter',
                size: [100, 100],
                borderWidth: 0,
                texture: unobstructedImg,
                // duration: 500,
                // animate: true,
            });

            // 红色呼吸点
            var geoLevelF = new Loca.GeoJSONSource({
                // data: [],
                data: new_red,
            });
            red_scatter = new Loca.ScatterLayer({
                loca,
                zIndex: 13,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            red_scatter.setSource(geoLevelF);
            red_scatter.setStyle({
                unit: 'meter',
                size: [200, 200],
                borderWidth: 0,
                texture: breath_red,
                duration: 500,
                animate: true,
            });
            //loca.add(breathRed);

            // 黄色呼吸点
            var geoLevelE = new Loca.GeoJSONSource({
                // data: [],
                data: new_yellow,
            });
            yellow_scatter = new Loca.ScatterLayer({
                loca,
                zIndex: 12,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            yellow_scatter.setSource(geoLevelE);
            yellow_scatter.setStyle({
                unit: 'meter',
                size: [150, 150],
                borderWidth: 0,
                texture: breath_yellow,
                duration: 1000,
                animate: true,
            });

            // 橙色呼吸点
            var geoLevelO = new Loca.GeoJSONSource({
                // data: [],
                data: new_orange,
            });
            orange_scatter = new Loca.ScatterLayer({
                loca,
                zIndex: 14,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            orange_scatter.setSource(geoLevelO);
            orange_scatter.setStyle({
                unit: 'meter',
                size: [200, 200],
                borderWidth: 0,
                texture: breath_orange,
                duration: 1000,
                animate: true
            });

            // 启动渲染动画
            loca.animate.start();
            //var dat = new Loca.Dat();
            //dat.addLayer(scatter, ' 贴地');
            // dat.addLayer(breathRed, '红色');
            //dat.addLayer(breathYellow, '黄色');

        },
        //灭掉所有
        putOut(){
            loca.remove(green_scatter);
            loca.remove(yellow_scatter);
            loca.remove(orange_scatter);
            loca.remove(red_scatter);
            loca.remove(unobstructed_scatter);
        },
        layerTrans(feature){
            var new_feature = JSON.parse(JSON.stringify(feature));
            for(var i = 0;i<feature.features.length;i++){
                var new_Coord = CoordTransform.wgs84togcj02(feature.features[i].geometry.coordinates[0],feature.features[i].geometry.coordinates[1]);
                new_feature.features[i].geometry.coordinates = new_Coord;
            }

            return new_feature;
        },
        tooltip(){
            this.$refs.road.addEventListener('mouseenter',()=>{
                this.showTooltip = true
            })
            this.$refs.road.addEventListener('mouseleave',()=>{
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
        background: url('../../../assets/img/layer_icon_2.png') no-repeat;
        background-size:100% 100%;
    }
</style>
