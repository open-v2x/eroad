import Socket from '../../socket'
import TargetPrimitive from './targetPrimitive'

export default class FullRoad{
    socket = null;
    //path = 'ws://192.168.137.157:8888';//'ws://144.7.108.34:1202/socket/rad'//'ws://192.168.200.188:7513/socket/rad';
    //北向南交通角度;1.29979637;
    targetCollection = {};//{'deviceId1':{'1':,'2','3','4'},...}
    h = 5;
    list = {};
    constructor(viewer){
        if(!viewer) return;
        //this.createTargetPool();
        this.viewer = viewer;
        this.primitiveCollection = new Cesium.PrimitiveCollection();   
        this.viewer.scene.primitives.add(this.primitiveCollection);
        //this.fixedFrameTransforms =  Cesium.Transforms.localFrameToFixedFrameGenerator('north', 'west');
        this.ws = new Socket({
            //网址（iocdata端口是我下面的服务器的端口）
            //'url':'ws://192.168.137.26:8099'
            'url':iocquanxitrack,//'wss://city189.cn:1653/ws/iocquanxi/track'//
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
            //if(msg.data == '连接成功')
                //return;
            //console.time('one');
            let data = JSON.parse(msg);
            
            //data = JSON.parse(data);
            let targets = data.targets;
            let deviceId = data.deviceId;
            if(this.list[deviceId]){
                this.list[deviceId].push(data);
            }else{
                this.list[deviceId] = [];
            }
            
            if(targets.length>0){
                targets.forEach(target =>{//循环待优化
                    let carId = target.targetId;
                    let type = target.targetType;
                    let speed = Number(target.speed);
                    if(!this.targetCollection[deviceId]){
                        this.targetCollection[deviceId] = {};
                    }
                    let getCar = this.targetCollection[deviceId][carId];
                    let lng = Number(target.longitude);
                    let lat = Number(target.latitude);
                    if(!target.headingAngle || !lng || !lat){
                        return;
                    }
                    let position = Cesium.Cartesian3.fromDegrees(lng,lat,this.h);//(lng+0.00006,lat+0.000015,0.4);
                    let hpRoll =  new Cesium.HeadingPitchRoll(3.1415927 * Number(target.headingAngle)/180,0,0);// - 1.5707964
                    if(!getCar){//如果是新车
                        //console.log('new');
                        //var getCar = new Target({type:1});
                        let getCar = new TargetPrimitive({
                            position : position,
                            hpRoll : hpRoll,
                            type:1
                        });
                        //this.viewer.scene.primitives.add(getCar.mycar);
                        this.primitiveCollection.add(getCar.mycar);
                        // let modelMatrix = Cesium.Transforms.headingPitchRollToFixedFrame(position, hpRoll, 
                        //     Cesium.Ellipsoid.WGS84,Cesium.Transforms.localFrameToFixedFrameGenerator('north', 'west'));;
                        //getCar.move(position, hpRoll);
                        // getCar.hpRoll = hpRoll;
                        // getCar.position = position;
                    
                        this.targetCollection[deviceId][carId] = getCar;


                        if(this.primitiveCollection.length>2000){
                            //this.savefiles(JSON.stringify(this.list['E11N91HMBLD']));
                            this.primitiveCollection.removeAll();
                            this.targetCollection = {};
                        }
                    }else{
                        //console.log('old')
                        // var s = 4;
                        // var i = 1;
                        // setInterval(()=>{
                        //     let splinePos = {
                        //         x:getCar.position.x + (position.x - getCar.position.x)/s*i,
                        //         y:getCar.position.y + (position.y - getCar.position.y)/s*i,
                        //         z:getCar.position.z           
                        //     }
                        //     i++;
                        //     if(i <= s){
                        //         getCar.move(splinePos, hpRoll);
                        //     }
                        //     else{
                        //         getCar.position = position;
                        //     }
                        // },100)

                        getCar.moveSpline(position, hpRoll);
                        

                                            
                    }
                    
                })
                //老车消失
            }
            var ids = data.disappear_ids;
            //disappears(deviceId,ids);
            if(ids.length>0){
                ids.forEach(id => {                   
                    //this.viewer.entities.removeById(disappears[i]);
                    if(this.targetCollection[deviceId] && this.targetCollection[deviceId][id]){                  
                        //this.targetCollection[ids[i]].removed();
                        //this.viewer.scene.primitives.remove(this.targetCollection[deviceId][ids[i]].mycar);
                        //console.log(this.targetCollection[deviceId][id])
                        // this.targetCollection[deviceId][id].moveBySpeed(
                        //     this.targetCollection[deviceId][id].hpRoll,
                        //     this.targetCollection[deviceId][id].speed);
                        this.primitiveCollection.remove(this.targetCollection[deviceId][id].mycar);
                        //this.targetCollection[deviceId + ids[i]].mycar.destroy();
                        this.targetCollection[deviceId][id] = null;
                    }else{
                        //console.log(deviceId+id);
                    }
                })
            }

            //console.timeEnd('one');
        })


    }


    changeViewer(viewer){
        //先清空
        this.primitiveCollection.removeAll();
        this.viewer = viewer;
        this.primitiveCollection = new Cesium.PrimitiveCollection();   
        this.viewer.scene.primitives.add(this.primitiveCollection);
    }

    setH(h){
        this.h = h;
    }

    close(){
        this.ws.close();
    }

    savefiles(data, name){
    //Blob为js的一个对象，表示一个不可变的, 原始数据的类似文件对象，这是创建文件中不可缺少的！
    var urlObject = window.URL || window.webkitURL || window;
    var export_blob = new Blob([data]);
    var save_link = document.createElementNS("http://www.w3.org/1999/xhtml", "a")
    save_link.href = urlObject.createObjectURL(export_blob);
    save_link.download = 'f.txt';
    save_link.click();
    }

} 