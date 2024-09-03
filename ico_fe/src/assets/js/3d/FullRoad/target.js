export default class Target{
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

    constructor(options = {}){    
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
            label: { //文字标签
                text: options.id ? options.id:'options.id',
                font: '500 14px Helvetica',// 15pt monospace
                style: Cesium.LabelStyle.FILL,
                fillColor: Cesium.Color.WHITE,
                pixelOffset: new Cesium.Cartesian2(0, -30), //偏移量
                showBackground: true,
                backgroundColor: new Cesium.Color(0.5, 0.6, 1, 1.0)
            },
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