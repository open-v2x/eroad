import Socket from '../../socket'
import TargetPrimitive from './targetPrimitive'
import Target from './target'
export default class RadTest{
    socket = null;
    //path = 'ws://192.168.137.157:8888';//'ws://144.7.108.34:1202/socket/rad'//'ws://192.168.200.188:7513/socket/rad';
    //北向南交通角度;1.29979637;
    targetCollection = {};//{'deviceId1':{'1':,'2','3','4'},...}
    h = 5;
    constructor(viewer){
        if(!viewer) return;
        //this.createTargetPool();
        this.viewer = viewer;
        this.primitiveCollection = new Cesium.PrimitiveCollection();   
        this.viewer.scene.primitives.add(this.primitiveCollection);
        //this.fixedFrameTransforms =  Cesium.Transforms.localFrameToFixedFrameGenerator('north', 'west');
        this.ws = new Socket({
            //网址（iocdata端口是我下面的服务器的端口）
            'url':'wss://city189.cn:1653/ws/iocquanxi/track',//'wss://city189.cn:1653/ws/iocquanxi/track'//
        })
        this.ws.onmessage(msg=>{
            //if(msg.data == '连接成功')
                //return;
            //console.time('one');
            let data = JSON.parse(msg);
            data = JSON.parse(data);
            let targets = data.targets;
            if(targets.length>0){
                targets.forEach(target =>{//循环待优化
                    let lng = Number(target.longitude);
                    let lat = Number(target.latitude);
                    let position = Cesium.Cartesian3.fromDegrees(lng,lat,this.h);//(lng+0.00006,lat+0.000015,0.4);
                    let hpRoll =  new Cesium.HeadingPitchRoll(3.1415927 * Number(target.headingAngle)/180,0,0);// - 1.5707964
                    let getCar = new Target({type:1});
                    getCar.position = position;
                    getCar.hpRoll = hpRoll;
                    
                })
                //老车消失
                this.viewer.entities.removeAll();
            }


            

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