
export default class Flying{
    lat = 39.058739494199585;
    lng = 115.91871121327465;
    height = 500.0;
        heading = 0;
        pitch = 0;
        roll = 0;
    //定义飞行过程中的六自由度参数,飞行过程中，参数实时更新，经度，维度，高度，偏航角，俯仰角，滚转角
    tempLng; tempLat; temp; tempHeight; tempHeading; tempPitch; tempRoll;
    //左右和上下移动时对速度有影响，用correction去修正速度
    correction = 1;
    //全局实体
    myEntity; myEntity2;
    msg = [1000000];
    //全局步长参数
    step = 0.0001;
    //全局变量标识
    //停止飞行变量和飞机模型变量
    pauseFlag = 0;
    planeFlag = 0;
    flag = {
        moveUp:false,
        moveDown:false,
        moveForward:false,
        moveBackward:false,
        moveLeft:false,
        moveRight:false
    };
    constructor(options = {}){    
        //this.position = Cesium.Cartesian3.fromDegrees(options.position[0],options.position[1],0.3)
        this.myFlying = viewer.entities.add({
            //id:'car',
            // 位置
            position : Cesium.Cartesian3.fromDegrees(this.lng,this.lat,this.height),
            //position: new Cesium.CallbackProperty(this.getPositin, false),
            // 姿态
            //orientation: new Cesium.CallbackProperty(this.getOrientation, false),
            model: {
                uri: "Cesium_Air.gltf",
                scale: 10,
                //minimumPixelSize: 128,
                //maximumScale: 20000,
            },
            // label: { //文字标签
            //     text: options.id ? options.id:'options.id',
            //     font: '500 30px Helvetica',// 15pt monospace
            //     scale: 0.5,
            //     style: Cesium.LabelStyle.FILL,
            //     fillColor: Cesium.Color.WHITE,
            //     pixelOffset: new Cesium.Cartesian2(0, -30), //偏移量
            //     showBackground: true,
            //     backgroundColor: new Cesium.Color(0.5, 0.6, 1, 1.0)
            // },
            
        });

        Cesium.when(this.myFlying.readyPromise).then(() => {
            this.myFlying.activeAnimations.addAll({
                loop: Cesium.ModelAnimationLoop.REPEAT
          })
        })
        viewer.trackedEntity = this.myFlying;
        //viewer.shouldAnimate = true;

        this.tempLng = this.lng
        this.tempLat = this.lat
        this.tempHeight = this.height
        this.tempHeading = this.heading
        this.tempPitch = this.pitch
        this.tempRoll = this.roll
        

        document.addEventListener('keydown',(e)=>{
            this.setFlagStatus(e, true);
        });
 
        document.addEventListener('keyup',(e)=>{
            this.setFlagStatus(e, false);
        });

        viewer.clock.onTick.addEventListener((clock)=>{
            if(this.flag.moveUp && this.tempPitch < 1.57 && this.tempHeight < 100000){
                this.tempPitch += 0.02;
                if (this.tempPitch > 0)
                    this.tempHeight += this.step * Math.sin(this.tempPitch) * 1110000; //1经纬度约等于110km
                    //this.moveModel(0);
            }else if(this.flag.moveDown && this.tempPitch > -1.57){
                this.tempPitch -= 0.02;
                if (this.tempPitch < 0)
                    this.tempHeight += this.step * Math.sin(this.tempPitch) * 1110000;
                    //this.moveModel(0);
            }
            if(this.flag.moveLeft){
                this.tempHeading -= 0.02;
                if (this.tempRoll > -0.785)
                    this.tempRoll -= 0.05;
                //this.moveModel();
            }else if(this.flag.moveRight){
                this.tempHeading += 0.02;
                if (this.tempRoll < 0.785)
                    this.tempRoll += 0.05;
                //this.moveModel();
            }

            this.correction = Math.abs(Math.cos(this.tempHeading)) * Math.abs(Math.cos(this.tempPitch));
            
            if(this.flag.moveForward){
                this.step=0.001;
                //this.moveModel();
            }
            if(this.flag.moveBackward){
                this.step=0;
                //this.moveModel();
            }

            if (Math.abs(this.tempRoll) < 0.001)
            this.tempRoll = 0;
            if (Math.abs(this.tempPitch) < 0.001)
            this.tempPitch = 0;

            if (this.tempRoll > 0)
                this.tempRoll -= 0.025;
            if (this.tempRoll < 0)
                this.tempRoll += 0.025;
            if (this.tempPitch > 0)
                this.tempPitch -= 0.0025;
            if (this.tempPitch < 0)
                this.tempPitch += 0.0025;

                this.moveModel();

        });
    }


    //移动模型
    moveModel = () => {
        this.tempLng += (this.step * this.correction);
        this.tempLat -= (this.step * Math.sin(this.tempHeading)); //主要用来左右移动
        var position = Cesium.Cartesian3.fromDegrees(this.tempLng, this.tempLat, this.tempHeight);
        var hpr = new Cesium.HeadingPitchRoll(this.tempHeading, this.tempPitch, this.tempRoll);
        var wgs84 = Cesium.Ellipsoid.WGS84.cartesianToCartographic(position);
        var orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);
        this.myFlying.orientation = orientation;
        this.myFlying.position = position;
    }
    

    setFlagStatus = (key,value) => {
        switch (key.keyCode){
            case 65:
                this.flag.moveLeft = value;
                break;
            case 87:
                this.flag.moveForward = value;
                break;
            case 68:
                this.flag.moveRight = value;
                break;
            case 83:
                this.flag.moveBackward = value;
                break;
            case 69:
                this.flag.moveUp = value;
                break;
            case 81:
                this.flag.moveDown = value;
                break;
        }
    }
}