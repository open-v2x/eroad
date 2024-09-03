import Socket from '../../socket'
import TargetPrimitive from './targetPrimitive'

export default class FullRoad_laser{
    socket = null;
    //path = 'ws://192.168.137.157:8888';//'ws://144.7.108.34:1202/socket/rad'//'ws://192.168.200.188:7513/socket/rad';
    //北向南交通角度;1.29979637;
    targetCollection = {};//{'deviceId1':{'1':,'2','3','4'},...}
    h = 5;
    constructor(viewer,path){
        if(!viewer) return;
        //this.createTargetPool();
        this.viewer = viewer;
        this.primitiveCollection = new Cesium.PrimitiveCollection();   
        this.viewer.scene.primitives.add(this.primitiveCollection);
        this.path = path;
        //this.fixedFrameTransforms =  Cesium.Transforms.localFrameToFixedFrameGenerator('north', 'west');       

        this.ws = new Socket({
            //网址（iocdata端口是我下面的服务器的端口）
            'url':this.path
        })
        this.ws.onopen(msg => {
            // setTimeout(()=>{
            //     this.ws1 = new Socket({
            //         //网址（iocdata端口是我下面的服务器的端口）
            //         'url':'ws://192.168.137.26:8199'
            //     })
            //     this.ws1.onmessage(msg => {
            //         let data = JSON.parse(msg);
            //         data.id_color.forEach(item => {
            //             if(this.targetCollection["E2N61JGLD"][item.id]){
            //                 this.targetCollection["E2N61JGLD"][item.id].setColor(item.color);
            //             }
            //         })
            //     })
            // },5000)
        })
        this.ws.onmessage(msg=>{
            let data = JSON.parse(msg);
            let targets = data.targets;
            let deviceId = data.device_id;
            if(targets.length>0){
                targets.forEach(target =>{//循环待优化
                    let carId = target.targetId;
                    let type = target.targetType;
                    let color = target.color;
                    if(!this.targetCollection[deviceId]){
                        this.targetCollection[deviceId] = {};
                    }
                    let getCar = this.targetCollection[deviceId][carId];
                    let lng = Number(target.longitude);
                    let lat = Number(target.latitude);
                    let position = Cesium.Cartesian3.fromDegrees(lng,lat,this.h);//(lng+0.00006,lat+0.000015,0.4);
                    let hpRoll =  new Cesium.HeadingPitchRoll(Number(target.headingAngle),0,0);// - 1.5707964
                    if(!getCar){//如果是新车
                        //var getCar = new Target({type:1});
                        let getCar = new TargetPrimitive({
                            color:color,
                            type:type,
                            position : position,
                            hpRoll : hpRoll
                        });
                        //this.viewer.scene.primitives.add(getCar.mycar);
                        this.primitiveCollection.add(getCar.mycar);
                        // let modelMatrix = Cesium.Transforms.headingPitchRollToFixedFrame(position, hpRoll, 
                        //     Cesium.Ellipsoid.WGS84,Cesium.Transforms.localFrameToFixedFrameGenerator('north', 'west'));;
                        getCar.move(position, hpRoll);
                        // getCar.hpRoll = hpRoll;
                        // getCar.position = position;
                    
                        this.targetCollection[deviceId][carId] = getCar;
                    }else{
                        getCar.move(position, hpRoll);
                        // getCar.hpRoll = hpRoll;   
                        // getCar.position = position;
                                            
                    }
                    
                })
                //老车消失
            }
            var ids = data.disappear_ids;
            //disappears(deviceId,ids);
            if(ids.length>0){
                ids.forEach(id => {                   
                    if(this.targetCollection[deviceId] && this.targetCollection[deviceId][id]){                  
                        this.primitiveCollection.remove(this.targetCollection[deviceId][id].mycar);
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

} 