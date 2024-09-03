
export default class TargetPrimitive{
    viewer= null;
    radian = Cesium.Math.toRadians(2.0);
    scene = null;
    position = Cesium.Cartesian3.fromDegrees(115.92927574782557,39.057755544373585,5);
    hpRoll = new Cesium.HeadingPitchRoll(0,0,0);//90度=1.57079637弧度
    fixedFrameTransforms =  Cesium.Transforms.localFrameToFixedFrameGenerator('north', 'west');
    modelMatrix = Cesium.Transforms.headingPitchRollToFixedFrame(this.position, this.hpRoll, Cesium.Ellipsoid.WGS84,this.fixedFrameTransforms);
    speedVector = new Cesium.Cartesian3();

    constructor(options = {}){    
        this.viewer = viewer;
        this.scene = viewer.scene;
        var uri;
        var scale = 0.9;
        
        ////0 — 未知 1 — 汽车 2 — 卡车、货车 3 — 大巴车 4 — 行人 5 — 自行车 6 — 摩托车/电动车 7 – 中巴车 8-手推车
        if(options.type == 1){
            uri = "static/model/car-baise.gltf"
            scale = 1
        }else if(options.type == 2){
            uri = "static/model/car-baise.gltf"
            scale = 0.9
        }else if(options.type == 3){
            uri = "static/model/bus.glb"
            scale = 0.8
        }else if(options.type == 4){
            uri ="static/model/ren.gltf"
        }else if(options.type == 5){
            uri ="static/model/motuo1.glb"
        }else if(options.type == 6){
            uri = "static/model/motuo1.glb"
        }else if(options.type == 7){
            uri = "static/model/bus.glb"
            scale = 0.5
        }
        //this.hpRoll.heading = options.heading;
        this.position = options.position;
        this.hpRoll = options.hpRoll;
        this.timeStamp = new Date().getTime();
        this.mycar = Cesium.Model.fromGltf({
            id:options.id,
            url:uri,
            scale:scale,
            color:Cesium.Color.RED,
            //distanceDisplayCondition:new Cesium.DistanceDisplayCondition(0.0, 1500.0),
            modelMatrix: Cesium.Transforms.headingPitchRollToFixedFrame(this.position, this.hpRoll, Cesium.Ellipsoid.WGS84,this.fixedFrameTransforms),
        })
        // this.mycar = this.viewer.entities.add({
        //     //id:options.id,
        //     // 位置
        //     position: new Cesium.CallbackProperty(this.getPositin, false),
        //     // 姿态
        //     orientation: new Cesium.CallbackProperty(this.getOrientation, false),
        //     model: {
        //         uri: uri,
        //         scale: scale,
        //     },
        //     // label: { //文字标签
        //     //     text: options.id ? options.id:'options.id',
        //     //     font: '500 14px Helvetica',// 15pt monospace
        //     //     style: Cesium.LabelStyle.FILL,
        //     //     fillColor: Cesium.Color.WHITE,
        //     //     pixelOffset: new Cesium.Cartesian2(0, -30), //偏移量
        //     //     showBackground: true,
        //     //     backgroundColor: new Cesium.Color(0.5, 0.6, 1, 1.0)
        //     // },
        // });
        



    }

    setColor(colorStr){
        let color = Cesium.Color.WHITE;
        switch (colorStr){
            case 'black':
                color = Cesium.Color.BLACK;
                break;
            case 'red':
                color = Cesium.Color.RED;
                break;
            case 'brown':
                color = Cesium.Color.BROWN;
                break;
            case 'gray':
                color = Cesium.Color.GRAY;
                break;
            case 'blue':
                color = Cesium.Color.BLUE;
                break;
            case 'orange':
                color = Cesium.Color.ORANGE;
                break;
            case 'purple':
                color = Cesium.Color.PURPLE;
                break;
            case 'silver':
                color = Cesium.Color.SILVER;
                break;
            case 'yellow':
                color = Cesium.Color.YELLOW;
                break;
            case 'gloden':
                color = Cesium.Color.DARKORANGE;
                break;

        }

        this.mycar.color = color;
    }

    move = (position, hpRoll) => {
        this.position = position;
        this.hpRoll = hpRoll;
        //this.speed = speed;
        Cesium.Transforms.headingPitchRollToFixedFrame(position, hpRoll, Cesium.Ellipsoid.WGS84, this.fixedFrameTransforms, this.mycar.modelMatrix);
    }

    moveSpline = async(position,hpRoll) => {
        this.hpRoll = hpRoll;
        let s = 2;
        let t = new Date().getTime();
        let pre = Math.floor((t - this.timeStamp)/s);
        for(let i = 1;i<=s;i++){
            let splinePos = {
                x:this.position.x + (position.x - this.position.x)/s*i,
                y:this.position.y + (position.y - this.position.y)/s*i,
                z:this.position.z + (position.z - this.position.z)/s*i          
            }
            Cesium.Transforms.headingPitchRollToFixedFrame(splinePos, hpRoll, Cesium.Ellipsoid.WGS84, this.fixedFrameTransforms, this.mycar.modelMatrix);
            await this.delay(70);
        }
        this.position = position;
        this.timeStamp = t;
    }

    delay = (num) => {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                resolve()
            }, num)
        })
    }

    // moveBySpeed = (hpRoll,speed) => {
    //     this.speedVector = Cesium.Cartesian3.multiplyByScalar(Cesium.Cartesian3.UNIT_X,speed,this.speedVector);
    //     // 根据速度计算出下一个位置的坐标
    //     this.position = Cesium.Matrix4.multiplyByPoint(this.mycar.modelMatrix ,this.speedVector, this.position);
    //     // 小车移动
    //     Cesium.Transforms.headingPitchRollToFixedFrame(this.position, hpRoll, Cesium.Ellipsoid.WGS84, this.fixedFrameTransforms, this.mycar.modelMatrix);
    // }

    // getPositin = () => {
    //     return this.position
    // }
    // getOrientation = () => {
    //     return Cesium.Transforms.headingPitchRollQuaternion(this.position,this.hpRoll)
    // }
    

    // removed = ()=>{
    //     this.scene.primitives.remove(this.mycar);
    // }

}