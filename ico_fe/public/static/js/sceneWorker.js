

    //var window = self;
    var viewer = window.viewer;
    var Cesium = window.Cesium;
    var b = true;
    if(Cesium.FeatureDetection.supportsImageRenderingPixelated()){//判断是否支持图像渲染像素化处理
        var vtxf_dpr = window.devicePixelRatio;
        // 适度降低分辨率
        while (vtxf_dpr >= 2.0) {
            vtxf_dpr /= 2.0;
        }
        viewer.resolutionScale = vtxf_dpr;
    }
      //是否开启抗锯齿
    viewer.scene.fxaa = true;
    viewer.scene.postProcessStages.fxaa.enabled = true;
    viewer.shadows = b;
    viewer.shadowMap.darkness=0.4;
    viewer.shadowMap.softShadows  = b;//是否启用百分比更接近过滤以产生更柔和的阴影。
    //viewer.shadowMap.maximumDistance = 1500;//距离低于此值产生阴影
    viewer.shadowMap.size = 4096;//数值越高质量越好越卡
    viewer._cesiumWidget._supportsImageRenderingPixelated = Cesium.FeatureDetection.supportsImageRenderingPixelated();
    viewer._cesiumWidget._forceResize = b;

