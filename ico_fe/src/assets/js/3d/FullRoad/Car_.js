import icon from '../../../../assets/img/prompt.png'
export default class Car_{
    radian = Cesium.Math.toRadians(0.5);
    speed = 1;
    speedVector = new Cesium.Cartesian3();
    scene = viewer.scene;
    position = Cesium.Cartesian3.fromDegrees(115.91871121327465,39.058739494199585,0.3);
    hpRoll = new Cesium.HeadingPitchRoll(0,0,0);//90度=1.57079637弧度
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
    isBegin = false;
    constructor(options = {}){    
        this.position = Cesium.Cartesian3.fromDegrees(options.position[0],options.position[1],options.position[2]);
        //this.hpRoll.heading = options.heading;
        this.mycar = viewer.entities.add({
            //id:options.id,
            // 位置
            position: new Cesium.CallbackProperty(this.getPositin, false),
            // 姿态
            orientation: new Cesium.CallbackProperty(this.getOrientation, false),
            model: {
                uri: options.url,//"static/model/bus.glb",
                scale: options.scale//0.05,
            },
            // label: { //文字标签
            //     text: 'options.id',
            //     font: '500 30px Helvetica',// 15pt monospace
            //     scale: 0.5,
            //     style: Cesium.LabelStyle.FILL,
            //     fillColor: Cesium.Color.WHITE,
            //     pixelOffset: new Cesium.Cartesian2(0, -30), //偏移量
            //     showBackground: true,
            //     backgroundColor: new Cesium.Color(0.5, 0.6, 1, 1.0)
            // },
        });
        

        

        document.addEventListener('keydown',(e)=>{
            this.setFlagStatus(e, true);
        });
 
        document.addEventListener('keyup',(e)=>{
            this.setFlagStatus(e, false);
        });
 
        viewer.clock.onTick.addEventListener((clock)=>{
            if(!this.isBegin) return;
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

    begin = () => {
        this.isBegin = true;
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

    removed = ()=>{
        viewer.entities.remove(this.mycar);
    }
}