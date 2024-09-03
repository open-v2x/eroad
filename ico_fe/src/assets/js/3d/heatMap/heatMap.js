import  CesiumHeatmap from 'cesium-heatmap';
export default class HeatMap {
    constructor(viewer, data = null, bounds = null, opts = null) {
        this.viewer = viewer;
        this.data = data;
        this.initHeatMap(bounds, data, opts);
    }
    initHeatMap(bounds, data, opts = null) {
        let heatMap = window.CesiumHeatmap.create(  // 1.384行 this => window})("h337", window, function () {...2.698行k.data = l;注释调 ...3.报错加let
            this.viewer, 
            bounds, 
            opts ? opts : {
                backgroundColor: "rgba(255,0,0,0)",
                radius: 50,
                maxOpacity: .93,
                minOpacity: 0,
                blur: .75
            }
        );
        this.heatMap = heatMap
        let values = data.map(d => d.value)
        let valueMin = Math.min(...values);
        let valueMax = Math.max(...values);
        heatMap.setWGS84Data(0, 10, data);
    }
    // 控制显示隐藏
    show(s) {
        this.heatMap.show(s)
    }
    // 更新数据源
    update(data) {
        let values = data.map(d => d.value)
        let valueMin = Math.min(...values);
        let valueMax = Math.max(...values);
        this.heatMap.setWGS84Data(valueMin, valueMax, data);
        // 因为大片都是空的啊，所以只是一小块数据
        //this.viewer.zoomTo(this.heatMap._layer);
		window.viewer.camera.setView({
            destination: new Cesium.Rectangle(
                Cesium.Math.toRadians(bounds.west),
                Cesium.Math.toRadians(bounds.south),
                Cesium.Math.toRadians(bounds.east),
                Cesium.Math.toRadians(bounds.north)
            )
        });
    }

}