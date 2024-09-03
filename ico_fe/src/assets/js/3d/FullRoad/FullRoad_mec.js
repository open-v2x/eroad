import Socket from '../../socket'
import TargetPrimitive from './targetPrimitive'
export default class FullRoad_mec{
    socket = null;
    //path = 'ws://192.168.137.157:8888';//'ws://144.7.108.34:1202/socket/rad'//'ws://192.168.200.188:7513/socket/rad';
    //北向南交通角度;1.29979637;
    targetCollection = {};
    constructor(viewer){
        if(!viewer) return;
        //this.createTargetPool();
        this.viewer = viewer;
        // 实例化socket
        //this.socket = new WebSocket(ws_path);
        this.socket = new Socket({
            //网址（iocdata端口是我下面的服务器的端口）
            'url':ws_path,
        })

        this.socket.onmessage(msg => {
            var data = JSON.parse(msg);
            var targets = data.e1FrameParticipant;
            if(targets){
                targets.forEach(target =>{//循环待优化
                    var carId = target.id+'';
                    var type = target.type;
                    var getCar = this.targetCollection[carId];
                    var lng = target.longitude;
                    var lat = target.latitude;
                    var position = Cesium.Cartesian3.fromDegrees(lng+0.00006,lat+0.000015,0.4);
                    var hpRoll =  new Cesium.HeadingPitchRoll(3.1415927 * target.courseAngle/180,0,0);

                    if(!getCar){//如果是新车
                        let getCar = new TargetPrimitive({type:type});
                        this.primitiveCollection.add(getCar.mycar);
                        getCar.move(position, hpRoll);
                    
                        this.targetCollection[deviceId][carId] = getCar;
                    }else{
                        getCar.move(position, hpRoll);
                                            
                    }
                    
                })
                
            }
            //老车消失
            var disappears = data.disappear_ids;
            if(disappears.length>0){
                for(var i=0;i<disappears.length;i++){
                    //this.viewer.entities.removeById(disappears[i]);
                    this.targetCollection[disappears[i].id+''].removed();
                    this.targetCollection[disappears[i].id+''] = null;
                }
            }
        })


    }

    close(){
        this.socket.close();
    }


} 