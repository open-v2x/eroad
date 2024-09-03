(function(){
    // var socket = null;
    // //path = 'ws://192.168.137.157:8888';//'ws://144.7.108.34:1202/socket/rad'//'ws://192.168.200.188:7513/socket/rad';
    // //北向南交通角度;1.29979637;
    // targetCollection = {};
    // // 实例化socket
    // socket = new WebSocket('wss://city189.cn:1652/ws/iocquanxitrack')
    // // 监听socket连接
    // socket.onopen = open
    // // 监听socket错误信息
    // socket.onerror = error
    // // 监听socket消息
    // socket.viewer = viewer;
    // socket.onmessage = getMessage

    // function open() {
    //     console.log("socket连接成功")
    // }
    // function error() {
    //     console.log("连接错误")
    // }
    // function getMessage(msg){
    //     if(msg.data == '连接成功')
    //         return;
    //     var data = JSON.parse(msg.data);
    //     //if(data.device_id!='S333S92HMBLD')
    //         //return;
    //     var targets = data.targets;
    //     if(targets.length>0){
    //         targets.forEach(target =>{//循环待优化
    //             var carId = target.targetId+'';
    //             var type = target.targetType;
    //             var getCar = targetCollection[carId];
    //             var lng = target.longitude;
    //             var lat = target.latitude;
    //             var position = Cesium.Cartesian3.fromDegrees(lng+0.00006,lat+0.000015,0.4);
    //             var hpRoll =  new Cesium.HeadingPitchRoll(3.1415927 * target.headingAngle/180 - 1.5707964,0,0);

    //             if(!getCar){//如果是新车
    //                 var getCar = new Target({id:carId,type:type});
    //                 getCar.hpRoll = hpRoll;

    //                 getCar.position = position;
                
    //                 targetCollection[carId] = getCar;
    //             }else{
    //                 getCar.hpRoll = hpRoll;
 
    //                 getCar.position = position;
                                           
    //             }
                
    //         })
            
    //     }

    //     //老车消失
    //     var ids = data.disappear_ids;
    //     disappears(ids);


    // }
    // function send(){
    //     socket.send(params)
    // }
    // function close () {
    //     console.log("socket已经关闭")
    // }
    // function disappears(ids){
    //     if(ids.length>0){
    //         for(var i=0;i<ids.length;i++){
    //             try{
    //                 targetCollection[ids[i]].removed();
    //                 targetCollection[ids[i]] = null;
    //             }catch(ex){
    //                 console.log(ids[i],ex);
    //             }
    //         }
    //     }
    // }
    new FullRoad(window.viewer);
    class FullRoad{
        socket = null;
        //path = 'ws://192.168.137.157:8888';//'ws://144.7.108.34:1202/socket/rad'//'ws://192.168.200.188:7513/socket/rad';
        //北向南交通角度;1.29979637;
        targetCollection = {};
        constructor(viewer){
            //this.createTargetPool();
            this.viewer = viewer;
            this.ws = new Socket({
                //网址（iocdata端口是我下面的服务器的端口）
                'url':'wss://city189.cn:1653/ws/iocquanxi/track',//'wss://city189.cn:1653/ws/iocquanxi/track'//
                //心跳时间（单位:ms）
                //'heartBeat':1000,
                //发送心跳信息（支持json传入）(这个一般内容不重要，除非后端变态)
                //'heartMsg':'hello',
                //开起重连
                //'reconnect':true,
                //重连间隔时间（单位:ms）
                //'reconnectTime':5000,
                //重连次数
                //'reconnectTimes':10
            })
            this.ws.onmessage(msg=>{
                if(msg.data == '连接成功')
                    return;
                var data = JSON.parse(msg);
                data = JSON.parse(data);
                //if(data.device_id!='S333S92HMBLD')
                    //return;
                var targets = data.targets;
                if(targets.length>0){
                    targets.forEach(target =>{//循环待优化
                        var carId = target.targetId+'';
                        var type = target.targetType;
                        var getCar = this.targetCollection[carId];
                        var lng = Number(target.longitude);
                        var lat = Number(target.latitude);
                        var position = Cesium.Cartesian3.fromDegrees(lng,lat,5);//(lng+0.00006,lat+0.000015,0.4);
                        var hpRoll =  new Cesium.HeadingPitchRoll(3.1415927 * Number(target.headingAngle)/180 - 1.5707964,0,0);

                        if(!getCar){//如果是新车
                            var getCar = new Target(this.viewer,{id:carId,type:1});
                            getCar.hpRoll = hpRoll;

                            getCar.position = position;
                        
                            this.targetCollection[carId] = getCar;
                        }else{
                            getCar.hpRoll = hpRoll;
        
                            getCar.position = position;
                                                
                        }
                        
                    })
                    
                }
                //老车消失
                var ids = data.disappear_ids;
                this.disappears(ids);
            })


        }

        disappears(ids){
            if(ids.length>0){
                for(var i=0;i<ids.length;i++){
                    //this.viewer.entities.removeById(disappears[i]);
                    try{
                        this.targetCollection[ids[i]].removed();
                        this.targetCollection[ids[i]] = null;
                    }catch(ex){
                        console.log(ids[i],ex);
                    }
                }
            }
        }


        close(){
            this.ws.close();
        }

    } 
    class Target{
        viewer= null;
        radian = Cesium.Math.toRadians(2.0);
        scene = null;
        position = Cesium.Cartesian3.fromDegrees(115.88433595360499,39.043907374116166,0.3);
        hpRoll = new Cesium.HeadingPitchRoll(0,0,0);//90度=1.57079637弧度
        orientation = Cesium.Transforms.headingPitchRollQuaternion(
            this.position,
            this.hpRoll
        )
        count = 0;

        constructor(viewer,options = {}){    
            this.viewer = viewer;
            this.scene = viewer.scene;
            var uri;
            var scale = 1;
            ////0 — 未知 1 — 汽车 2 — 卡车、货车 3 — 大巴车 4 — 行人 5 — 自行车 6 — 摩托车/电动车 7 – 中巴车 8-手推车
            if(options.type == 1){
                uri = "static/model/qiche.gltf"
                scale = 0.04
            }else if(options.type == 2){
                uri = "static/model/qiche.gltf"
                scale = 0.04
            }else if(options.type == 3){
                uri = "static/model/bus.glb"
                scale = 0.8
            }else if(options.type == 4){
                uri ="static/model/ren.gltf"
            }else if(options.type == 5){
                uri =""
            }else if(options.type == 6){
                uri = "static/model/motuo1.glb"
            }else if(options.type == 7){
                uri = "static/model/bus.glb"
                scale = 0.5
            }
            //this.hpRoll.heading = options.heading;
            this.mycar = this.viewer.entities.add({
                //id:options.id,
                // 位置
                position: new Cesium.CallbackProperty(this.getPositin, false),
                // 姿态
                orientation: new Cesium.CallbackProperty(this.getOrientation, false),
                model: {
                    uri: uri,
                    scale: scale,
                },
                // label: { //文字标签
                //     text: options.id ? options.id:'options.id',
                //     font: '500 14px Helvetica',// 15pt monospace
                //     style: Cesium.LabelStyle.FILL,
                //     fillColor: Cesium.Color.WHITE,
                //     pixelOffset: new Cesium.Cartesian2(0, -30), //偏移量
                //     showBackground: true,
                //     backgroundColor: new Cesium.Color(0.5, 0.6, 1, 1.0)
                // },
            });
            



        }

        getPositin = () => {
            return this.position
        }
        getOrientation = () => {
            return Cesium.Transforms.headingPitchRollQuaternion(this.position,this.hpRoll)
        }
        

        removed = ()=>{
            //this.position = Cesium.Cartesian3.fromDegrees(115.88433595360499,39.043907374116166,0.3);//{x:0,y:0,z:0};
            //this.mycar.id = this.mycar.id + '_';
            this.viewer.entities.remove(this.mycar);
        }
    }
    // function createTarget({}){
    //     var position;
    //     var hpRoll;
    //     var uri;
    //     var scale = 1;
    //     ////0 — 未知 1 — 汽车 2 — 卡车、货车 3 — 大巴车 4 — 行人 5 — 自行车 6 — 摩托车/电动车 7 – 中巴车 8-手推车
    //     if(options.type == 1){
    //         uri = "static/model/qiche.gltf"
    //         scale = 0.04
    //     }else if(options.type == 2){
    //         uri = "static/model/qiche.gltf"
    //         scale = 0.04
    //     }else if(options.type == 3){
    //         uri = "static/model/bus.glb"
    //         scale = 0.8
    //     }else if(options.type == 4){
    //         uri ="static/model/ren.gltf"
    //     }else if(options.type == 5){
    //         uri =""
    //     }else if(options.type == 6){
    //         uri = "static/model/motuo1.glb"
    //     }else if(options.type == 7){
    //         uri = "static/model/bus.glb"
    //         scale = 0.5
    //     }
    //     //this.hpRoll.heading = options.heading;
    //     var mycar = viewer.entities.add({
    //         //id:options.id,
    //         // 位置
    //         position: new Cesium.CallbackProperty(getPositin, false),
    //         // 姿态
    //         orientation: new Cesium.CallbackProperty(getOrientation, false),
    //         model: {
    //             uri: uri,
    //             scale: scale,
    //         }
    //     });
    //     function getPositin(){
    //         return position
    //     }
    //     function getOrientation(){
    //         return Cesium.Transforms.headingPitchRollQuaternion(position,hpRoll)
    //     }
        
    
    // }
    // function removed(mycar){
    //     viewer.entities.remove(mycar);
    // }


    
}())
