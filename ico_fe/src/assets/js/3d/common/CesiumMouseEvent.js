export class CesiumMouseEvent{

    funcList = [];
    constructor(){
        viewer.cesiumWidget.screenSpaceEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK);//禁用双击操作
        // 监听鼠标点击事件
        this.handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas).setInputAction((e) => {
            
        }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
    }


}