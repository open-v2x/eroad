
import { getWeather } from '../api.js'


import '../../../assets/js/3d/common/CesiumOfflineCache.min.js'



import PostStageManager from './common/postStageManager';


import AmapImageryProvider from './imagery/provider/AmapImageryProvider'
import CesiumVideo3d from './video/CesiumVideo3d'
import { cameraOnLine, getUrlByLamp, play } from './video/videoHelper'
import Arrow from './trafficLight/arrow_1'
import Flying from './FullRoad/flying.js';
import KeyBoardControl from './common/keyBoardControl';
import { promiseTimeout } from '@vueuse/shared';

var addressLabel = {};
var label = null;
var featureDic = {};
var postStageManager = null;
export const initEarth = (cesiumContainer) => {
  //3d
  Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI0ZmIxMDkxMy0xY2Q0LTRlZjUtYjg1MC01MTU2MDU4OGI0ZGQiLCJpZCI6NjYzOTQsImlhdCI6MTYzMDkxOTE1Nn0.oaYfPD_JHJZT6Zo3zRwygnd9ozYH5U590WNHa6b8_3g';
  window.viewer = new Cesium.Viewer(cesiumContainer, {
    // 加载底图，用WMTS服务方式加载
    imageryProvider: new Cesium.WebMapTileServiceImageryProvider({
      // 以键值对方式加载，url为请求模板，下面的大部分是能力文档xml的内容，按照文档填写即可，layer图层名，style风格，format格式，tileMatrixSetID用于请求WMTS服务的标识符
      url: "https://{s}.tianditu.gov.cn/img_c/wmts?service=wmts&request=GetTile&version=1.0.0" +
        "&LAYER=img&tileMatrixSet=c&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}" +
        "&style=default&format=tiles&tk=88ff353e86f9fcf070aadb4df96156a7",
      layer: "tdtImg_c",
      style: "default",
      format: "tiles",
      tileMatrixSetID: "c",
      subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],//URL模板中用于 {s} 占位符的子域.如果此参数是单个字符串，则字符串中的每个字符都是一个子域。一个数组，数组中的每个元素都是一个子域。这里都是天地图的子域。
      tilingScheme: new Cesium.GeographicTilingScheme(),// 切片方式
      tileMatrixLabels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"], // 每级一个的tileMatrix标识符
      maximumLevel: 50, // 最大层级
      show: false // ？没这个属性啊
    }),
    //requestRenderMode : true,
    //maximumRenderTimeChange:Infinity,
    animation: false, //左下角的动画仪表盘
    // terrainProvider : new Cesium.CesiumTerrainProvider({
    //     url: Cesium.IonResource.fromAssetId(1) //id 如上图获取
    // }),
    //terrainProvider: Cesium.createWorldTerrain(),
    baseLayerPicker: false, //右上角的图层选择按钮
    geocoder: false, //搜索框
    homeButton: false, //home按钮
    sceneModePicker: false, //模式切换按钮
    timeline: false, //底部的时间轴
    navigationHelpButton: false, //右上角的帮助按钮，
    fullscreenButton: false, //右下角的全屏按钮
    infoBox: false,//小弹窗
    selectionIndicator: false,
    zoomIndicatorContainer: false,
    navigation: false,//指南针
    shadows: false,//阴影
    shouldAnimate: true,
    scene3DOnly: true,//为true，则每个几何体实例将仅以3D渲染以节省GPU内存。

  });
  //viewer.scene.mode = Cesium.SceneMode.COLUMBUS_VIEW// 哥伦布视图
  viewer.scene.globe.enableLighting = false;//启用使用场景光源照亮地球
  viewer.scene.globe.depthTestAgainstTerrain = true;// 深度检测，开启就有遮挡效果了
  viewer._cesiumWidget._creditContainer.style.display = "none";  //	去除版权信息

  postStageManager = new PostStageManager(viewer);// 在场景渲染的纹理或上一个后期处理阶段的输出上运行一个后期处理阶段。这里是加天气特效。
  // 多视锥减少drawcall，对数深度缓存也可以减少drawcall，在深度缓存中更好分分配数据，都是解决Z值冲突问题的，配合起来减少闪烁，但是多视锥配合深度缓冲可能有性能问题。
  // 可能有性能问题，所以配合起来使用的时候开启下面这个，将不必要的视锥清除，以比较好的配比解决Z值冲突问题。
  viewer.scene.logarithmicDepthBuffer = true;//是否使用对数深度缓冲区。启用此选项将允许在多视锥体中减少视锥体，从而提高性能。此属性依赖于支持的 fragmentDepth。
  //viewer.scene.debugShowFramesPerSecond = true;

  viewer.scene.globe.dynamicAtmosphereLighting = true;//启用对大气和雾的动态照明效果
  viewer.scene.globe.dynamicAtmosphereLightingFromSun = true;//动态氛围照明是否使用太阳方向而不是场景的光线方向


  // 黑白设置，没啥用
  // var collection = viewer.scene.postProcessStages;
  // var silhouette = collection.add(Cesium.PostProcessStageLibrary.createBlackAndWhiteStage ());
  // silhouette.enabled = true;
  // silhouette.uniforms.gradations =20; //调节黑白程度（1-20）
  // 亮度设置，其实这个是饱和度设置，3为最高
  var stages = viewer.scene.postProcessStages;
  // PostProcessStageLibrary包含用于创建常见的后期处理阶段的函数。createBrightnessStage此阶段具有一个统一的亮度，值可缩放每个像素的饱和度。
  viewer.scene.brightness = viewer.scene.brightness || stages.add(Cesium.PostProcessStageLibrary.createBrightnessStage());
  viewer.scene.brightness.enabled = true;
  viewer.scene.brightness.uniforms.brightness = 1.5;

  // FeatureDetection一组用于检测当前浏览器是否支持各种功能的功能。
  if (Cesium.FeatureDetection.supportsImageRenderingPixelated()) {//判断是否支持图像渲染像素化处理
    // devicePixelRatio 设备独立像素跟css像素的比率，即几个独立像素代表一个css像素
    var vtxf_dpr = window.devicePixelRatio;
    // 适度降低分辨率
    while (vtxf_dpr >= 2.0) {
      vtxf_dpr /= 2.0;
    }
    // 获取或设置渲染分辨率的缩放比例。小于1.0的值可以改善性能不佳的设备上的性能，而值大于1.0的设备将以更高的速度呈现分辨率，然后缩小比例，从而提高视觉保真度。例如，如果窗口小部件的尺寸为640x480，则将此值设置为0.5将导致场景以320x240渲染，然后在设置时按比例放大设置为2.0会导致场景以1280x960渲染，然后按比例缩小。
    viewer.resolutionScale = 1;
    //viewer.useBrowserRecommendedResolution = true;
  }
  //是否开启抗锯齿
  viewer.scene.fxaa = true;
  viewer.scene.postProcessStages.fxaa.enabled = true;
  viewer.scene.camera.setView({
    destination : new Cesium.Cartesian3.fromDegrees(106.980541,36.058507,20000000),
    orientation: {
        heading : Cesium.Math.toRadians(0),
        pitch : Cesium.Math.toRadians(-90.0),//从上往下看为-90
        roll : 0
    }
})
  //
  //调用影响中文注记服务
  //   viewer.imageryLayers.addImageryProvider(new Cesium.WebMapTileServiceImageryProvider({
  //     url: '4366a4ecb0de5c749a7d9ea1d4493a45',
  //     layer: "tdtImg_c",
  //     style: "default",
  //     format: "tiles",
  //     tileMatrixSetID: "c",
  //     subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],
  //     tilingScheme: new Cesium.GeographicTilingScheme(),
  //     tileMatrixLabels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"],
  //     maximumLevel: 50,
  //     //show: false
  // }))

  // 指定url模板提供瓦片
  // var tdtLayer = new Cesium.UrlTemplateImageryProvider({
  //   url: "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png ",
  //   subdomains: ["a", "b", "c", "d"],

  // })
  // 提供由ArcGIS MapServer托管的平铺图像。默认情况下，服务器的预缓存磁贴为使用（如果有）。
  // var blueArcgisMap = new Cesium.ArcGisMapServerImageryProvider({
  //   name: "arcgis蓝灰",
  //   url: "https://map.geoq.cn/ArcGIS/rest/services/ChinaOnlineStreetPurplishBlue/MapServer",
  //   maximumLevel: 19,
  // })
  // 提供由Mapbox托管的平铺图像。
  // var layer = new Cesium.MapboxStyleImageryProvider({
  //   url: 'https://api.mapbox.com/styles/v1/',
  //   username: '18333286662',
  //   styleId: 'ckujk1slp8ibs18rxuis5se1g',//mapbox://styles/18333286662/ckujk1slp8ibs18rxuis5se1g
  //   accessToken: 'pk.eyJ1IjoiMTgzMzMyODY2NjIiLCJhIjoiY2t0d2J2YTF0MDk3YTJ2dGhsd25uZTVsOCJ9.ZqW3569-uLO-4OclJw7JVA',
  //   scaleFactor: true
  // });


  //viewer.imageryLayers.addImageryProvider(layer);
  //newHightLightPolyline(viewer,'IBD_LANE_BOUNDARY.json',Cesium.Color.WHITE,false);

  //http://webst02.is.autonavi.com/appmaptile?style=8&x={x}&y={y}&z={z} //高德地名地址


  //容东0.03分辨率正射影像
  //  let ImageryProvider = new Cesium.WebMapServiceImageryProvider({
  //                     url: "http://192.168.137.102:8080/geoserver/rongdong/wms?",
  //                     layers: "rongdong:rdtif003",
  //                     parameters: {
  //                       transparent:true, //是否透明
  //                       format: "image/png",
  //                       srs: "EPSG:4326",
  //                       styles: "",
  //                     },
  //                   });
  //viewer.imageryLayers.addImageryProvider(ImageryProvider);


  //viewer.imageryLayers.addImageryProvider(layer);

  //viewer.scene.screenSpaceCameraController.enableCollisionDetection = true;

  // cesium加载代码
  //update3dtilesMaxtrix(pctileset);
  //hightlight3dTiles(pctileset);
  //viewer.scene.primitives.add(pctileset);
  // 这个tileset.json做了请求转发了。
  var dm = new Cesium.Cesium3DTileset({
    url: roadNetwork_path,
    //maximumMemoryUsage:1024,
    // preferLeaves:true,
    // dynamicScreenSpaceError:true,
    // dynamicScreenSpaceErrorFactor:80,
    // foveatedScreenSpaceError:false,
    //skipLevelOfDetail:false,
  });
  // readyPromise就是tileset渲染前一帧的末尾将被resolved的一个promise，他resolve了就说明tileset加载好了
  let dmPromise = dm.readyPromise.then(tileset => {
    // 这个是位置转换函数
    rectify(tileset, { r: 0.7, e: -1.3, n: -0.5, u: -1 })
    // rectify(tileset, { r: 0.7, e: 47.5, n: 41.5, u: -3 })
    dm.style = new Cesium.Cesium3DTileStyle({
      color: {
        evaluateColor: (feature, result) => {
          let key = feature.getProperty('name');
          if (featureDic.hasOwnProperty(key)) {
            featureDic[key].push(feature);
          } else {
            featureDic[key] = [feature]
          }
          if (key == 'RD-SY-001') {
            //replaceWaterFeature(feature);
            //silhouetteGreen.selected=[feature]
          }

        },
      },
    });
    viewer.scene.primitives.add(tileset);
  })

  //最新模型
  var pictileset = new Cesium.Cesium3DTileset({
    url: tiles_path,
    maximumMemoryUsage: 2048,
    //preferLeaves:true,
    dynamicScreenSpaceError: true,
    // dynamicScreenSpaceErrorFactor:80,
    // foveatedScreenSpaceError:false,//true:优先加载屏幕中间区域
    foveatedConeSize: 1,
    cullRequestsWhileMovingMultiplier: 100,
    skipLevelOfDetail: true,
    skipLevels: 19,
    skipScreenSpaceErrorFactor: 19,
    foveatedConeSize: 1,
    foveatedTimeDelay: 0,
    loadSiblings: true,
    maximumScreenSpaceError: 19,
    preferLeaves: true,
    preloadWhenHidden: true
    //classificationType:1
  });
  let picPromise = pictileset.readyPromise.then(function (tileset) {

    //rectify(tileset,{r:0.7,e:307,n:150,u:5})
    rectify(tileset, { r: 0.7, e: 2881, n: -84, u: 4.5 })
    viewer.scene.primitives.add(tileset);
    render(true)
    //viewer.camera.viewBoundingSphere(tileset.boundingSphere, new Cesium.HeadingPitchRange(0.1, 0.2, tileset.boundingSphere.radius));
    //viewer.camera.lookAtTransform(Cesium.Matrix4.IDENTITY);
    //     setTimeout(() => {
    //       //viewer.flyTo(tileset);
    //       viewer.camera.flyTo({
    //         // fromDegrees()将经纬度和高程转换为世界坐标
    //         destination: {x: -2170568.102606102, y: 4462506.932798863, z: 3996642.024235847},
    //         orientation: {
    //             // 指向
    //             heading: 0.10409242841539346,
    //             // 视角
    //             pitch: -0.4945232818220173,
    //             roll: 6.283075287195388
    //         }
    //     });


    //  }, 5000);
    //singleModel(tileset);
    //getEntityProperty(tileset);

    //////////

    // 延迟给阴影
    // setTimeout(() => {
      //if (window.Worker){
      // render(true);
      // const taskProcessor = new Cesium.TaskProcessor('sceneWorker.js');
      // const promise = taskProcessor.scheduleTask({
      //     //someParameter : true,
      //     //another : 'hello'
      // });
      // if (!Cesium.defined(promise)) {
      //     // too many active tasks - try again later
      // } else {
      //     promise.then(function(result) {
      //         // use the result of the task
      //         console.log('++++++++++++++++++++++++++++++++++++++',result);
      //     });
      // }
      //var sceneWoker = new Worker('static/js/sceneWorker.js');
      // sceneWoker.postMessage({
      //  //Cesium:Cesium,
      //  viewer:viewer
      // });
      //}

      // var shareWorker = new SharedWorker('http://localhost:8080/static/js/sceneWorker.js');
      // // 接受信息
      // shareWorker.port.onmessage = (e)=> {
      //   console.log(e.data)
      // }
      // // 发送信息
      // shareWorker.port.postMessage({
      //   type : 'notifyTab',
      //   payload : {}
      // });
    // }, 20000);
  });

  // 往window上传入模型，存在内存

  window.build_tileset = pictileset;
  window.tileset = dm;
  initEvent();
  return Promise.all([dmPromise,picPromise])

  //     pictileset.allTilesLoaded.addEventListener(() => {
  //         alert('loaded');

  //    });
  //addTree();
  // var time = new Date().getTime();
  // var imageryLayer = new AmapImageryProvider({
  //     url:'http://webst02.is.autonavi.com/appmaptile?style=8&x={x}&y={y}&z={z}',
  //     //url:'https://tm.amap.com/trafficengine/mapabc/traffictile?v=1.0&t=1&x={x}&y={y}&z={z}',
  //     crs:'WGS84',
  //     classificationType: Cesium.ClassificationType.BOTH,
  // })
  // viewer.imageryLayers.addImageryProvider(imageryLayer);

  //videoMix(viewer);

  // viewer.imageryLayers.addImageryProvider(new Cesium.UrlTemplateImageryProvider({
  //     url: "https://minedata.cn/service/data/mvt-layer?datasource=merge&z={z}&x={x}&y={y}&request=GetTile&key=16be596e00c44c86bb1569cb53424dc9&solu=12877",
  //     layer: "tdtAnnoLayer",
  //     style: "default",
  //     format: "image/jpeg",
  //     tileMatrixSetID: "GoogleMapsCompatible"
  // }));


  //addPoint();
  //showDynamicLabel();
  //addRoadName();
  //getWGS84();
  //addWeatherState();
  // addWaterAnimation([115.94114409759602,39.075681504568486,12,
  //     115.94359567436712,39.070616565691886,12,
  //     115.94192587587743,39.05877978316769,12,
  //     115.94414708819522,39.05837450080938,12,
  //     115.94724546176086,39.075637125897295,12],viewer)
  //addTree();

  //addVideoWall();
  //singleModel(pictileset);

  //initTrafficLight();
  //new Flying();
  // var keyBoardControl = new KeyBoardControl();
  // setTimeout(()=>{
  //   keyBoardControl.start();
  // },10000)
}

 const initEvent = () => {
  // cesiumWidget包含cesium场景的小部件,screenSpaceEventHandler处理用户输入事件，removeInputAction删除在输入事件上要执行的功能
  viewer.cesiumWidget.screenSpaceEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK);//禁用双击操作
  // 参数为容器，监听容器内的输入事件，监听左键单击
  window.handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas);
  // setInputAction (action, type, modifier ) action是函数，为回调，type是ScreenSpaceEventType，就是这个type的输入事件发生时执行什么
  window.handler.setInputAction((i) => {
    // pick一个点，屏幕坐标转为三维笛卡尔坐标,下面这个是根据射线获取地球表面坐标。
    var cartesian = viewer.scene.globe.pick(viewer.camera.getPickRay(i.position),viewer.scene);
    // 三维笛卡尔坐标转为世界坐标（弧度）
    let gra = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesian);  // longitude, latitude, height
    // 弧度转经纬度
    const longitude = Cesium.Math.toDegrees(gra.longitude) //将弧度转为度
    const latitude = Cesium.Math.toDegrees(gra.latitude)
    console.log(longitude,latitude)
    // 这个是根据椭球坐标系获取三维坐标
    // console.log(viewer.scene.camera.pickEllipsoid(i.position, viewer.scene.globe.ellipsoid))
    //输入事件自带position是屏幕二维坐标，以它拾取实体
    const t = viewer.scene.pick(i.position);
    if (t && t.id && t.id.click) {
      // 如果拾取到的实体有id也有click事件，就执行
      t.id.click(t.id);
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
}

export const render = (b) => {
  viewer.shadows = b;// bool，确定是否使用太阳投射
  viewer.shadowMap.darkness = 0.4; // 阴影黑暗
  viewer.shadowMap.softShadows = b;//是否启用百分比更接近过滤以产生更柔和的阴影。
  //viewer.shadowMap.maximumDistance = 1500;//距离低于此值产生阴影
  viewer.shadowMap.size = 4096;//数值越高质量越好越卡，阴影贴图的宽度和高度
  viewer._cesiumWidget._supportsImageRenderingPixelated = Cesium.FeatureDetection.supportsImageRenderingPixelated();
  viewer._cesiumWidget._forceResize = b;
}

export const flyTo = (params) => {
  window.viewer.camera.flyTo({
    // fromDegrees()将经纬度和高程转换为世界坐标
    destination: params.position,//Cesium.Cartesian3.fromDegrees(params.lon, params.lat, params.alt),
    orientation: {
      // 指向
      heading: params.heading,
      // 视角
      pitch: params.pitch,
      roll: params.roll
    }
  });
}

const initTrafficLight = () => {
  var carWay1 = new Arrow({
    sign: true,
    positions: [
      115.9464655117649, 39.062395091561335,
      115.9463978998975, 39.06240886174407,
    ],
    width: 30,
  });

  var carWay2 = new Arrow({
    sign: true,
    positions: [
      115.94626304277813, 39.06236436820851,
      115.94631943882173, 39.062356373925866,
    ],
    width: 30,
  });

  var carWay3 = new Arrow({
    sign: false,
    positions: [
      115.94632649996568, 39.06225306287025,
      115.9463654463892, 39.06234413059696
    ],
    width: 30,
  });



  var carWay4 = new Arrow({
    sign: false,
    positions: [
      115.9463874126903, 39.06250253393414,
      115.94635248151926, 39.06242222629394
    ],
    width: 30,
  });

  //左转
  var carWay5 = new Arrow({
    sign: true,
    positions: [
      115.94645962022105, 39.06238008492669,
      115.94642465183216, 39.06238863434092,
      115.9463897903135, 39.0623765175604
    ],
    width: 30,
  });

  var carWay6 = new Arrow({
    sign: true,
    positions: [
      115.9462572053575, 39.062350080124446,
      115.94628186780078, 39.06234224947073,
      115.94630261905736, 39.062321733233496
    ],
    width: 30,
  });

  var carWay7 = new Arrow({
    sign: false,
    positions: [
      115.94632649996568, 39.06225306287025,
      115.94634802386834, 39.06230564309773,
      115.94632814815105, 39.06235315928236
    ],
    width: 30,
  });

  var carWay8 = new Arrow({
    sign: false,
    positions: [
      115.9463874126903, 39.06250253393414,
      115.94637154757092, 39.06247004641995,
      115.94639072803389, 39.06242384988007
    ],
    width: 30,
  });

}

export const destoryEarth = () => {
  if (viewer != null && !viewer.isDestroyed()) {
    //viewer.scene.primitives.removeAll();
    viewer.destroy();
    window.viewer = null;
  }
}

export const cameraOnLineControl = () => {
  cameraOnLine();
}

export const videoMix = (viewer) => {
  let par = {
    position: { x: 115.94407655332522, y: 39.06282539866337, z: 7.5 }, //相机中心点位置
    alpha: 0.8, //透明度
    far: 200, //最远距离
    rotation: { x: -8, y: 107 }, //旋转角度
    url: "/video/video1.mp4", //视频地址
    fov: 35, //水平拉伸
    aspectRatio: 1, //高宽比
    near: 0.0, //相机框离原点距离
    debugFrustum: true, //视锥线
  };
  //const viewer = window.globalViewer;
  new CesiumVideo3d(Cesium, viewer, par);
}

export const addPoint = () => {
  const f = new BounceMarker(viewer, [115.9135592508151, 39.061111961006596, 0], {
    "设备名称": "摄像头",
    "设备编号": "SXT0001",
    "位置": "瑞祥路",
    "运行状态": "正常",
    "弹窗": "equipmentPop"
  });

  //f.bounceMarker.id = 'SXT0001';
  initEvent(viewer);
  //viewer.zoomTo();
  //bMarkers.push(f);
}

export const getEntityProperty = (tileset, callback) => {
  // Information about the currently selected feature
  var selected = {
    feature: undefined,
    originalColor: new Cesium.Color(),
  };

  // An entity object which will hold info about the currently selected feature for infobox display
  var selectedEntity = new Cesium.Entity();

  // Get default left click handler for when a feature is not picked on left click


  // If silhouettes are supported, silhouette features in blue on mouse over and silhouette green on mouse click.
  // If silhouettes are not supported, change the feature color to yellow on mouse over and green on mouse click.
  if (
    Cesium.PostProcessStageLibrary.isSilhouetteSupported(viewer.scene)
  ) {
    // Silhouettes are supported
    var silhouetteBlue = Cesium.PostProcessStageLibrary.createEdgeDetectionStage();
    silhouetteBlue.uniforms.color = Cesium.Color.BLUE;
    silhouetteBlue.uniforms.length = 0.01;
    silhouetteBlue.selected = [];

    var silhouetteGreen = Cesium.PostProcessStageLibrary.createEdgeDetectionStage();
    silhouetteGreen.uniforms.color = Cesium.Color.LIME;
    silhouetteGreen.uniforms.length = 0.01;
    silhouetteGreen.selected = [];

    viewer.scene.postProcessStages.add(
      Cesium.PostProcessStageLibrary.createSilhouetteStage([
        silhouetteBlue,
        silhouetteGreen,
      ])
    );

    // Silhouette a feature blue on hover.
    // viewer.screenSpaceEventHandler.setInputAction(function onMouseMove(
    //   movement
    // ) {
    //   // If a feature was previously highlighted, undo the highlight
    //   silhouetteBlue.selected = [];

    //   // Pick a new feature
    //   var pickedFeature = viewer.scene.pick(movement.endPosition);


    //   // Highlight the feature if it's not already selected.
    //   if (pickedFeature !== selected.feature) {
    //     silhouetteBlue.selected = [pickedFeature];
    //     var propertyNames = pickedFeature.getPropertyNames();
    //     var length = propertyNames.length;
    //     for (var i = 0; i < length; ++i) {
    //         var propertyName = propertyNames[i];
    //         //console.log(propertyName + ': ' + pickedFeature.getProperty(propertyName));
    //     }
    //   }
    // },
    // Cesium.ScreenSpaceEventType.MOUSE_MOVE);

    viewer.screenSpaceEventHandler.setInputAction(function (event) {
      silhouetteGreen.selected = [];
      //获取相机射线
      var ray = viewer.scene.camera.getPickRay(event.position);
      //根据射线和场景求出在球面中的笛卡尔坐标
      var position1 = viewer.scene.globe.pick(ray, viewer.scene);
      //获取该浏览器坐标的顶部数据
      var pickedFeature = viewer.scene.pick(event.position);
      // console.log(feature);
      //点击模型
      if (pickedFeature !== selected.feature) {
        const featureName = pickedFeature.getProperty('name');
        tileset.style = new Cesium.Cesium3DTileStyle({
          defines: {
            name: "${feature['name']}",
          },
          color: {
            evaluateColor: (feature, result) => {
              let key = feature.getProperty('name');
              if (key == featureName) {
                return Cesium.Color.BLUE.withAlpha(.8);
              }
            }
          },

        });
        callback(featureName);

        //getUrlByLamp(featureName,(url)=>{
        //console.log(featureName,url);
        //callback(url,position1);
        //       alert(url);
        //       let par = {
        //         position: { x:115.94651175309028,y:39.062634530535796, z: 7.5 }, //相机中心点位置
        //         alpha: 0.8, //透明度
        //         far: 100, //最远距离
        //         rotation: { x: -8, y: 203 }, //旋转角度
        //         url: url, //视频地址
        //         fov: 33, //水平拉伸
        //         aspectRatio: 2, //高宽比
        //         near: 0.0, //相机框离原点距离
        //         debugFrustum: true, //视锥线
        //       };
        //       new CesiumVideo3d(Cesium, viewer, par);

        //play(url);
        //});
        // const len = featureDic[featureName].length;
        // if(featureDic[featureName]['selected']){
        //     for(var i = 0;i<len;i++){
        //         featureDic[featureName]['selected'] = false;
        //         featureDic[featureName][i].color = new Cesium.Color(1,1,1,1);
        //         //viewer.scene.requestRender();
        //     }
        // }else{
        //     for(var i = 0;i<len;i++){
        //         featureDic[featureName]['selected'] = true;
        //         featureDic[featureName][i].color = Cesium.Color.RED;
        //         //viewer.scene.requestRender();
        //     }
        // }

      }


    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

  }

}


export const addVideoWall = () => {

  var xa = document.getElementById("trailer1");
  xa.play();
  viewer.entities.add({
    wall: {
      positions: [
        Cesium.Cartesian3.fromDegrees(
          115.91403448246281, 39.05985134343483,
          110
        ),
        Cesium.Cartesian3.fromDegrees(
          115.91542215658495, 39.05982890325738,
          110
        ),
      ],
      minimumHeights: [60, 60],
      material: xa,
    },
  });

}






















































































export const addWeatherState = (weather) => {
  postStageManager.clear();
  getWeather({
    city: '容城',
    extensions: 'base',
    key: '63b2abf7010d6d97f40d16d91f96bb97'
  }).then((res) => {
    //let weather = '雪'//res.lives[0].weather;
    postStageManager.show(weather);
  })
};

export const getWGS84 = () => {
  var handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas);

  handler.setInputAction(function (evt) {
    var cartesian = viewer.camera.pickEllipsoid(evt.position, viewer.scene.globe.ellipsoid);

    var cartographic = Cesium.Cartographic.fromCartesian(cartesian);

    var lng = Cesium.Math.toDegrees(cartographic.longitude);//经度值

    var lat = Cesium.Math.toDegrees(cartographic.latitude);//纬度值

    var mapPosition = { x: lng, y: lat, z: cartographic.height };//cartographic.height的值始终为零。
    var p = {
      position: viewer.scene.camera.position,
      heading: viewer.scene.camera.heading,//Cesium.Math.toRadians(-30.0),
      pitch: viewer.scene.camera.pitch,//Cesium.Math.toRadians(-45.0),
      roll: viewer.scene.camera.roll,
      //range: 5000.0
    }
    alert(lng + ',' + lat);
    console.log(JSON.stringify(p));

  }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
}

export const getHeight = () => {
  let handlers = new Cesium.ScreenSpaceEventHandler(viewer.scene._imageryLayerCollection);
  viewer.screenSpaceEventHandler.setInputAction(function (event) {
    //获取相机射线
    var ray = viewer.scene.camera.getPickRay(event.position);
    //根据射线和场景求出在球面中的笛卡尔坐标
    var position1 = viewer.scene.globe.pick(ray, viewer.scene);
    //获取该浏览器坐标的顶部数据
    var feature = viewer.scene.pick(event.position);
    // console.log(feature);
    var MouseHeight = 0;
    if (feature == undefined && position1) {
      cartographic1 = Cesium.Ellipsoid.WGS84.cartesianToCartographic(position1);
      var lon = Cesium.Math.toDegrees(cartographic1.longitude);
      var lat = Cesium.Math.toDegrees(cartographic1.latitude);
      MouseHeight = 0;
    }
    else {
      let cartesian = viewer.scene.pickPosition(event.position);
      if (Cesium.defined(cartesian)) {
        //如果对象已定义，将度转为经纬度
        let cartographic = Cesium.Cartographic.fromCartesian(cartesian);
        var lon = Cesium.Math.toDegrees(cartographic.longitude);
        var lat = Cesium.Math.toDegrees(cartographic.latitude);
        MouseHeight = cartographic.height;//模型高度
      }
    }
    // console.log(A,B,MouseHeight)
    alert(MouseHeight);
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
}

export const singleModel = (tileset) => {
  featureDic = {};
  function processContentFeatures(content, callback) {
    var featuresLength = content.featuresLength;
    for (var i = 0; i < featuresLength; ++i) {
      var feature = content.getFeature(i);
      callback(feature);
    }
  }
  function processTileFeatures(tile, callback) {
    var content = tile.content;
    var innerContents = content.innerContents;
    if (Cesium.defined(innerContents)) {
      var length = innerContents.length;
      for (var i = 0; i < length; ++i) {
        processContentFeatures(innerContents[i], callback);
      }
    } else {
      processContentFeatures(content, callback);
    }
  }



  tileset.tileLoad.addEventListener(function (tile) {
    tileset;
    //console.log('tilecount',++tilecount);
    processTileFeatures(tile, function (feature) {
      let key = feature.getProperty('name');
      if (featureDic.hasOwnProperty(key)) {
        featureDic[key].push(feature);
      } else {
        featureDic[key] = [feature]
      }

    });
  });

}

export const rectify = (tileset, opt = {}) => {
  //旋转
  let cartographic = Cesium.Cartographic.fromCartesian(tileset.boundingSphere.center);
  let surface = Cesium.Cartesian3.fromRadians(cartographic.longitude, cartographic.latitude, cartographic.height);
  let m = Cesium.Transforms.eastNorthUpToFixedFrame(surface);
  let mz = Cesium.Matrix3.fromRotationZ(Cesium.Math.toRadians(opt.r));//逆时针
  let rotate = Cesium.Matrix4.fromRotationTranslation(mz);
  Cesium.Matrix4.multiply(m, rotate, m);
  //平移 //向东向北
  let tempTranslation = new Cesium.Cartesian3(opt.e, opt.n, opt.u);
  let offset = Cesium.Matrix4.multiplyByPoint(m, tempTranslation, new Cesium.Cartesian3(0, 0, 0)); // 这里计算平移后落点是使用了旋转后的局部坐标系，因此东和北都转方向了。正确做法是在m矩阵乘旋转前完成平移，但是这里参数配好了，就先不改了。
  let translation = Cesium.Cartesian3.subtract(offset, surface, new Cesium.Cartesian3());
  tileset.modelMatrix = Cesium.Matrix4.fromTranslation(translation);
  tileset._root.transform = m;//生效S
}


export const setFire = () => {

}
