
export default class UAV{
    radian = Cesium.Math.toRadians(2.0);
    speed = 0.2;
    speedVector = new Cesium.Cartesian3();
    scene = viewer.scene;
    position = Cesium.Cartesian3.fromDegrees(115.91871121327465,39.058739494199585,0.3);
    hpRoll = new Cesium.HeadingPitchRoll();
    orientation = Cesium.Transforms.headingPitchRollQuaternion(
        this.position,
        this.hpRoll
    )
    count = 0;
    flag = {
        moveUp:false,
        moveDown:false,
        moveLeft:false,
        moveRight:false
    };
    constructor(options = {}){    
        //this.position = Cesium.Cartesian3.fromDegrees(options.position[0],options.position[1],0.3)
        this.mycar = viewer.entities.add({
            //id:'car',
            // 位置
            position: new Cesium.CallbackProperty(this.getPositin, false),
            // 姿态
            orientation: new Cesium.CallbackProperty(this.getOrientation, false),
            model: {
                uri: "qiche.gltf",
                scale: 0.05,
            },
            label: { //文字标签
                text: options.id ? options.id:'options.id',
                font: '500 30px Helvetica',// 15pt monospace
                scale: 0.5,
                style: Cesium.LabelStyle.FILL,
                fillColor: Cesium.Color.WHITE,
                pixelOffset: new Cesium.Cartesian2(0, -30), //偏移量
                showBackground: true,
                backgroundColor: new Cesium.Color(0.5, 0.6, 1, 1.0)
            },
        });
        //viewer.trackedEntity = this.mycar;

        

        document.addEventListener('keydown',(e)=>{
            this.setFlagStatus(e, true);
        });
 
        document.addEventListener('keyup',(e)=>{
            this.setFlagStatus(e, false);
        });

        viewer.clock.onTick.addEventListener((clock)=>{
            if(this.flag.moveUp){
                if(this.flag.moveLeft){
                    this.hpRoll.heading -= this.radian;
                    this.count += 2;
                }
                if(this.flag.moveRight){
                    this.hpRoll.heading += this.radian;
                    this.count -= 2;
                }
                this.moveCar(1);
            }else if(this.flag.moveDown){
                if(this.flag.moveLeft){
                    this.hpRoll.heading -= this.radian;
                    this.count += 2;
                }
                if(this.flag.moveRight){
                    this.hpRoll.heading += this.radian;
                    this.count -= 2;
                }
                this.moveCar(-1);
            }else {
              if(this.flag.moveLeft){
                    this.hpRoll.heading -= this.radian;
                    this.count += 2;
                    this.moveCar(0)
                }
                if(this.flag.moveRight){
                    this.hpRoll.heading += this.radian;
                    this.count -= 2;
                    this.moveCar(0)
                } 
            }
        });
    }

    getPositin = () => {
        return this.position
    }
    getOrientation = () => {
        return Cesium.Transforms.headingPitchRollQuaternion(this.position,this.hpRoll)
    }
    
    // 移动小车
    moveCar = (isUP) => {
        // 计算速度矩阵
        if(isUP>0){
            this.speedVector = Cesium.Cartesian3.multiplyByScalar(Cesium.Cartesian3.UNIT_X,this.speed,this.speedVector);
        }else if(isUP<0){
            this.speedVector = Cesium.Cartesian3.multiplyByScalar(Cesium.Cartesian3.UNIT_X,-this.speed,this.speedVector);
        }else{
            this.speedVector = Cesium.Cartesian3.multiplyByScalar(Cesium.Cartesian3.UNIT_X,0,this.speedVector);
        }
        // 根据速度计算出下一个位置的坐标
        //生成一个函数，该函数计算从以提供的原点为中心的参考系到提供的椭圆体的固定参考系的 4x4 变换矩阵。
        let fixedFrameTransforms =  Cesium.Transforms.localFrameToFixedFrameGenerator('east', 'north');
        let modelMatrix= Cesium.Transforms.headingPitchRollToFixedFrame(this.position, this.hpRoll, Cesium.Ellipsoid.WGS84,fixedFrameTransforms);
        this.position = Cesium.Matrix4.multiplyByPoint(modelMatrix ,this.speedVector, this.position);
    }
    

    setFlagStatus = (key,value) => {
        switch (key.keyCode){
            case 37:
                // 左
                this.flag.moveLeft = value;
                break;
            case 38:
                // 上
                this.flag.moveUp = value;
                break;
            case 39:
                // 右
                this.flag.moveRight = value;
                break;
            case 40:
                this.flag.moveDown = value;
                // 下
                break;
        }
    }
}