

export default class KeyBoardControl {
  constructor() {
    this.viewer = viewer;
    this.isKeyboardControl = false;
    this.screenSpaceCameraController = {
      enableRotate: true,
      enableTranslate: true,
      enableZoom: true,
      enableTilt: true,
      enableLook: true,
    };
    this.flags = {
      looking: false,
      moveForward: false,
      moveBackward: false,
      moveUp: false,
      moveDown: false,
      moveLeft: false,
      moveRight: false,
    };
    this.canvasOnclick = null;
    this.canvasTabindex = null;
    this.handler = null;
    this.mousePosition = null;
    this.startMousePosition = null;
    this.keyDownFuc = null;
    this.keyUpFuc = null;
    this.onTickFuc = null;
  }

  start() {
    if (this.isKeyboardControl) {
    return;
    }
    this.isKeyboardControl = true;
    const { viewer } = this;
    const { scene } = viewer;
    const { canvas } = viewer;
    scene.debugShowFramesPerSecond = true;

    this.canvasOnclick = canvas.onclick;
    this.canvasTabindex = canvas.getAttribute('tabindex');
    viewer.camera.setView({
        destination : viewer.scene.camera.position,
        orientation: {
            heading : viewer.scene.camera.heading,
            pitch : Cesium.Math.toRadians(0),
            roll : viewer.scene.camera.roll
        }
    })
    // 禁止默认的事件
    // scene.screenSpaceCameraController.enableRotate = false;
    // scene.screenSpaceCameraController.enableTranslate = false;
    // scene.screenSpaceCameraController.enableZoom = false;
    // scene.screenSpaceCameraController.enableTilt = false;
    // scene.screenSpaceCameraController.enableLook = false;
    // 焦点设置
    canvas.setAttribute('tabindex', '0');
    canvas.onclick = function () {
      canvas.focus();
    };
    // 监听输入
    this.handler = new Cesium.ScreenSpaceEventHandler(canvas);
    // // 鼠标左键
    // this.handler.setInputAction((movement) => {
    //   this.flags.looking = true;
    //   this.mousePosition = Cesium.Cartesian3.clone(
    //     movement.position,
    //   );
    //   this.startMousePosition = Cesium.Cartesian3.clone(
    //     movement.position,
    //   );
    // }, Cesium.ScreenSpaceEventType.LEFT_DOWN);
    // // 鼠标移动
    // this.handler.setInputAction((movement) => {
    //   this.mousePosition = movement.endPosition;
    // }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
    // // 鼠标左键抬起
    // this.handler.setInputAction(() => {
    //   this.flags.looking = false;
    // }, Cesium.ScreenSpaceEventType.LEFT_UP);
    this.keyDownFuc = (e) => { this.keyDown(e); };
    this.keyUpFuc = (e) => { this.keyUp(e); };
    this.onTickFuc = () => { this.onTick(); };
    // 获得键盘keydown事件
    document.addEventListener('keydown', this.keyDownFuc, false);
    // 获得键盘keyup事件
    document.addEventListener('keyup', (e) => { this.keyUp(e); }, false);
    // 更新相机
    viewer.clock.onTick.addEventListener(this.onTickFuc);
  }

  stop() {
    const { viewer } = this;
    const { scene } = viewer;
    const { canvas } = viewer;
    canvas.onclick = this.canvasOnclick;
    canvas.setAttribute('tabindex', this.canvasTabindex);
    scene.screenSpaceCameraController.enableRotate = this.screenSpaceCameraController.enableRotate;
    scene.screenSpaceCameraController.enableTranslate = this.screenSpaceCameraController.enableTranslate;
    scene.screenSpaceCameraController.enableZoom = this.screenSpaceCameraController.enableZoom;
    scene.screenSpaceCameraController.enableTilt = this.screenSpaceCameraController.enableTilt;
    scene.screenSpaceCameraController.enableLook = this.screenSpaceCameraController.enableLook;
    if (this.handler) {
      this.handler.destroy();
    }
    document.removeEventListener('keydown', this.keyDownFuc);
    document.removeEventListener('keyup', this.keyUpFuc);
    viewer.clock.onTick.removeEventListener(this.onTickFuc);
    this.isKeyboardControl = false;
  }

  putEntity(){
    this.myFlying = viewer.entities.add({
        //id:'car',
        // 位置
        //position : Cesium.Cartesian3.fromDegrees(this.lng,this.lat,this.height),
        position: new Cesium.CallbackProperty(this.getPositin, false),
        // 姿态
        //orientation: new Cesium.CallbackProperty(this.getOrientation, false),
        model: {
            uri: "Cesium_Air.gltf",
            scale: 3,
        },
    });
  }
  //
  getPositin = () =>{
    return viewer.scene.camera.position;
  }

  getFlagForKeyCode(keyCode) {
    switch (keyCode) {
      case 'W'.charCodeAt(0):
        return 'moveForward';
      case 'S'.charCodeAt(0):
        return 'moveBackward';
      case 'Q'.charCodeAt(0):
        return 'moveUp';
      case 'E'.charCodeAt(0):
        return 'moveDown';
      case 'D'.charCodeAt(0):
        return 'moveRight';
      case 'A'.charCodeAt(0):
        return 'moveLeft';
      default:
        return undefined;
    }
  }

  keyUp(e) {
    const flagName = this.getFlagForKeyCode(e.keyCode);
    if (typeof flagName !== 'undefined') {
      this.flags[flagName] = false;
    }
  }

  keyDown(e) {
    const flagName = this.getFlagForKeyCode(e.keyCode);
    if (typeof flagName !== 'undefined') {
      this.flags[flagName] = true;
    }

    //this.onTick();
  }

  onTick() {
    const { viewer } = this;
    const { canvas } = viewer;
    const { camera } = viewer;
    const { ellipsoid } = viewer.scene.globe;
    // 按下鼠标左键
    if (this.flags.looking) {
      const width = canvas.clientWidth;
      const height = canvas.clientHeight;
      // 鼠标滑动的距离的x或y/网页可见区域的宽或者高
      const x = (this.mousePosition.x - this.startMousePosition.x) / width;
      const y = -(this.mousePosition.y - this.startMousePosition.y) / height;
      // 相机移动速度参数
      const lookFactor = 0.1;
      // 相机移动
      camera.lookRight(x * lookFactor);
      camera.lookUp(y * lookFactor);
    }
    // 镜头移动的速度基于镜头离地球的高度
    const cameraHeight = ellipsoid.cartesianToCartographic(camera.position).height;
    var moveRate = 30;//cameraHeight / 2.0;
    // console.log('this.flags', this.flags)
    if (this.flags.moveLeft) {
        moveRate = moveRate/2;
        camera.lookLeft(0.1);
    }
    if (this.flags.moveRight) {
        moveRate = moveRate/2;
        camera.lookRight(0.1);
    }
    if (this.flags.moveForward) {
      camera.moveForward(moveRate);
    }
    if (this.flags.moveBackward) {
      camera.moveBackward(moveRate);
    }
    if (this.flags.moveUp) {
      camera.moveUp(cameraHeight/5);
    }
    if (this.flags.moveDown) {
      camera.moveDown(cameraHeight/5);
    }

  }
}
