
import { PolylineTrailLinkMaterialProperty } from './polylineTrailLinkMaterial'
import materialPng from '../../../img/polylineFlowMaterialYellow.png'
import water from '../../../img/water.jpg';
import fencePng from '../../../img/fence.png';
var Cesium = window.Cesium;
var viewer = window.viewer;

export const add3DTiles = (viewer,url) => {
    const tileset1 = new Cesium.Cesium3DTileset({
      url,
      // 控制切片视角显示的数量，可调整性能
      maximumScreenSpaceError: 2,
      maximumNumberOfLoadedTiles: 100000,
    });
    viewer.flyTo(tileset1);
    tileset1.readyPromise.then((tileset) => {
        if(!tileset)
            return;
      viewer.scene.primitives.add(tileset);
    //   viewer.scene.camera.setView({
    //     destination: tileset.boundingSphere.center,
    //     orientation: {
    //       heading: 0.0,
    //       pitch: -0.5,
    //       endTransform: Cesium.Matrix4.IDENTITY,
    //     },
    //   });
      tileset.style = new Cesium.Cesium3DTileStyle({
        color: 'vec4(0, 0.5, 1.0,0.8)',
      });
      // 注入 shader
      tileset.tileVisible.addEventListener((tile) => {
        const { content } = tile;
        const { featuresLength } = content;
        for (let i = 0; i < featuresLength; i += 2) {
          const feature = content.getFeature(i);
          const model = feature.content._model;

          if (model && model._sourcePrograms && model._rendererResources) {
            Object.keys(model._sourcePrograms).forEach((key) => {
              const program = model._sourcePrograms[key];
              const fragmentShader = model._rendererResources.sourceShaders[program.fragmentShader];
              let vPosition = '';
              if (fragmentShader.indexOf(' v_positionEC;') !== -1) {
                vPosition = 'v_positionEC';
              } else if (fragmentShader.indexOf(' v_pos;') !== -1) {
                vPosition = 'v_pos';
              }
              const color = `vec4(${feature.color.toString()})`;

              // 自定义着色器
              model._rendererResources.sourceShaders[program.fragmentShader] = `
                varying vec3 ${vPosition};// 相机坐标系的模型坐标
                void main(void){
                  /* 渐变效果 */
                  vec4 v_helsing_position = czm_inverseModelView * vec4(${vPosition},1);// 解算出模型坐标
                  float stc_pl = fract(czm_frameNumber / 120.0) * 3.14159265 * 2.0;
                  float stc_sd = v_helsing_position.z / 60.0 + sin(stc_pl) * 0.1;
                  gl_FragColor = ${color};// 基础蓝色
                  gl_FragColor *= vec4(stc_sd, stc_sd, stc_sd, 1.0);// 按模型高度进行颜色变暗处理
                  /* 扫描线 */
                  float glowRange = 360.0; // 光环的移动范围(高度)，最高到360米
                  float stc_a13 = fract(czm_frameNumber / 360.0);// 计算当前着色器的时间，帧率/（6*60），即时间放慢6倍
                  float stc_h = clamp(v_helsing_position.z / glowRange, 0.0, 1.0);
                  stc_a13 = abs(stc_a13 - 0.5) * 2.0;
                  float stc_diff = step(0.005, abs(stc_h - stc_a13));// 根据时间来计算颜色差异
                  gl_FragColor.rgb += gl_FragColor.rgb * (1.0 - stc_diff);// 原有颜色加上颜色差异值提高亮度
                }
              `;
            });
            // 让系统重新编译着色器
            model._shouldRegenerateShaders = true;
          }
        }
      });
    });
  }

export const hightlight3dTiles = tileset =>{
    tileset.readyPromise
        .then(function (tileset) {
            if(!tileset)
            return;
        tileset.tileVisible.addEventListener(function (tile) {
            //此事件在渲染帧的tileset 遍历期间触发
            var content = tile.content;
            var featuresLength = content.featuresLength;
            for (var i = 0; i < featuresLength; i += 2) {
            let feature = content.getFeature(i);
            let model = feature.content._model;

            if (
                model &&
                model._sourcePrograms &&
                model._rendererResources
            ) {
                Object.keys(model._sourcePrograms).forEach((key) => {
                let program = model._sourcePrograms[key];
                let fragmentShader =
                    model._rendererResources.sourceShaders[
                    program.fragmentShader
                    ];
                let v_position = "";
                if (fragmentShader.indexOf(" v_positionEC;") != -1) {
                    v_position = "v_positionEC";
                } else if (fragmentShader.indexOf(" v_pos;") != -1) {
                    v_position = "v_pos";
                }
                const color = `vec4(${feature.color.toString()})`;
                model._rendererResources.sourceShaders[
                    program.fragmentShader
                ] =
                    "varying vec3 " +
                    v_position +
                    ";\n" + // 相机坐标系的模型坐标
                    "void main(void){\n" + //渐变效果
                    "    vec4 position = czm_inverseModelView * vec4(" +
                    v_position +
                    ",1);\n" + //解算出模型坐标
                    "    float glowRange = 250.0;\n" + //光环的移动范围（高低），最高到200米
                    "    gl_FragColor = " +
                    color +
                    ";\n" + //颜色
                    "    gl_FragColor *= vec4(vec3(position.z / 50.0), 1.0);\n" + //按模型高度进行颜色变暗处理
                    //扫描线
                    "    float time = fract(czm_frameNumber / 600.0);\n" + //计算当前着色器的时间，帧率/(6*60)，及时间放慢6倍
                    "    time = abs(time - 0.5) * 2.0;\n" +
                    "    float diff = step(0.005, abs( clamp(position.z / glowRange, 0.0, 1.0) - time));\n" + //根据时间来计算颜色差异，比例
                    "    gl_FragColor.rgb += gl_FragColor.rgb * (1.0 - diff);\n" + //原有颜色加上颜色差异提高亮度值
                    "}\n";
                });
                model._shouldRegenerateShaders = true;
            }
            }
        });
        })
        .otherwise(function (error) {
        console.error(error);
        });
    }

export const flowingPolyline = (roadUrl) => {
    var viewer = window.viewer;
    Cesium.GeoJsonDataSource.load(roadUrl
    , {
        clampToGround: true, // 设置贴地
    }).then((dataSource) => {
        viewer.dataSources.add(dataSource).then(() => {
        const material = new PolylineTrailLinkMaterialProperty(materialPng);
        var entities = dataSource.entities.values;
        console.log(entities);
        for (var i = 0; i < entities.length; i++) {
            let entity = entities[i];
            entity.polyline.material =
            material
        }

        //viewer.zoomTo(entities[0]);
        });
    });
}

export const heatLine = (roadUrl,viewer) => {
    //var viewer = window.viewer;
    var promise1 = Cesium.GeoJsonDataSource.load(roadUrl);
    var promise = Cesium.GeoJsonDataSource.load(roadUrl).then((dataSource) => {
        var entities = dataSource.entities.values;
        var instances = [];
        for (var o = 0; o < entities.length; o++){
            var r = entities[o];
            const instance = new Cesium.GeometryInstance({
                geometry : new Cesium.GroundPolylineGeometry({
                    positions : r.polyline.positions._value,
                    width : 15.0
                }),
            });
            instances.push(instance);
        }

        viewer.scene.groundPrimitives.add(new Cesium.GroundPolylinePrimitive({
            geometryInstances : instances,
            classificationType:1,
            appearance : new Cesium.PolylineMaterialAppearance({
                material : Cesium.Material.fromType(Cesium.Material.PolylineGlowType, {
                    glowPower : 0.1,
                    //taperPower : 0.2,
                    color : Cesium.Color.GREEN
                }),
            })
        }));
    });

}

export const heatLine1 = (roadUrl,redPolylines,yellowPolylines,greenPolylines)=>{
    var viewer = window.viewer;

    redPolylines = viewer.scene.primitives.add(new Cesium.PolylineCollection());
    yellowPolylines = viewer.scene.primitives.add(new Cesium.PolylineCollection());
    greenPolylines = viewer.scene.primitives.add(new Cesium.PolylineCollection());
    var promise = Cesium.GeoJsonDataSource.load(roadUrl, {
        clampToGround: true, // 设置贴地
    });  //显示管线数据  直接加载json数据 比把json转化成czml在加载 快很多
    promise.then(function (dataSource) {
        //viewer.dataSources.add(dataSource);
        var entities = dataSource.entities.values;
        for (var o = 0; o < entities.length; o++) {
            var r = entities[o];
            //建立随机数
            var index = Math.random();
            if(index>0.9 && index<0.96){
                yellowPolylines.add({
                    positions : r.polyline.positions._value,
                    width: 15,
                    clampToGround: true,
                    classificationType: Cesium.ClassificationType.CESIUM_3D_TILE,
                    material : Cesium.Material.fromType(Cesium.Material.PolylineGlowType, {
                        glowPower : 0.1,
                        //taperPower : 0.2,
                        color : Cesium.Color.YELLOW
                    }),
                 });
            }else if(index > 0.96){
                redPolylines.add({
                    positions : r.polyline.positions._value,
                    width: 15,
                    classificationType: Cesium.ClassificationType.CESIUM_3D_TILE,
                    clampToGround: true,
                    material : Cesium.Material.fromType(Cesium.Material.PolylineGlowType, {
                        glowPower : 0.1,
                        //taperPower : 0.2,
                        color : Cesium.Color.RED
                    }),
                 });
            }else{
                // r.polyline.material = new Cesium.PolylineGlowMaterialProperty({
                //     glowPower : 0.1,
                //     //taperPower : 0.2,
                //     color : Cesium.Color.GREEN
                //   });
                // r.polyline.classificationType =  Cesium.ClassificationType.CESIUM_3D_TILE;
                // r.polyline.width = 15;
                greenPolylines.add(
                    {
                        positions : r.polyline.positions._value,
                        width: 15,
                        //classificationType: Cesium.ClassificationType.CESIUM_3D_TILE,
                        //clampToGround: true,
                        arcType: Cesium.ArcType.NONE,
                        material : Cesium.Material.fromType(Cesium.Material.PolylineGlowType, {
                            glowPower : 0.1,
                            //taperPower : 0.2,
                            color : Cesium.Color.GREEN
                        }),
                    })

            }
            //viewer.dataSources.add(dataSource)
        }
    });
}

export const newHightLightPolyline = (viewer,roadUrl,color,isW) => {
    var w = 4;
    if(isW){
        w = 15;
    }
    var polylines = viewer.scene.primitives.add(new Cesium.PolylineCollection());
    // polylines.width = new Cesium.CallbackProperty(() => {
    //     var height = viewer.camera.positionCartographic.height;
    //     alert(height);
    //     return w - (height/10000000)
    // },false)
    var promise = Cesium.GeoJsonDataSource.load(roadUrl, {
        clampToGround: true, // 设置贴地
    });  //显示管线数据  直接加载json数据 比把json转化成czml在加载 快很多
    promise.then(function (dataSource) {
        //viewer.dataSources.add(dataSource);
        var entities = dataSource.entities.values;
        for (var o = 0; o < entities.length; o++) {
            var r = entities[o];
             polylines.add({
                positions : r.polyline.positions._value,
                width: w,
                material : Cesium.Material.fromType(Cesium.Material.PolylineGlowType, {
                    glowPower : 0.1,
                    //taperPower : 0.2,
                    color : color
                }),
             });
        }



    });



    // A simple polyline with two points.
    // var polyline = polylines.add({
    //     positions : Cesium.PolylinePipeline.generateCartesianArc({
    //         positions : Cesium.Cartesian3.fromDegreesArray([-120.0, 40.0,
    //                                                                                        -110.0, 30.0])
    //     }),
    //     material : Cesium.Material.fromType('Color', {
    //         color : new Cesium.Color(1.0, 1.0, 1.0, 1.0)
    //     })
    // });
}

export const hightLightPolyline = (roadUrl,color,isW)=> {
    var w = 4;
    if(isW)
        w = 15;
    var promise = Cesium.GeoJsonDataSource.load(roadUrl, {
        clampToGround: true, // 设置贴地
    });  //显示管线数据  直接加载json数据 比把json转化成czml在加载 快很多
    promise.then(function (dataSource) {
        viewer.dataSources.add(dataSource);
        var entities = dataSource.entities.values;
        var colorHash = {};

        for (var o = 0; o < entities.length; o++) {
            var r = entities[o];
            r.nameID = o;   //给每条线添加一个编号，方便之后对线修改样式
            r.polyline.width = w;  //添加默认样式
            (r.polyline.material = new Cesium.PolylineGlowMaterialProperty({
                glowPower: .1, //一个数字属性，指定发光强度，占总线宽的百分比。
                color: color//Cesium.Color.WHITE.withAlpha(.9)
            }), 10)

        }
        // var temp = new Array();
        // window.Hightlightline = function (nameid) {
        //     var exists = temp.indexOf(nameid);
        //     if (exists <= -1) {
        //         Highlight(nameid,50, 50);
        //         temp.push(nameid);  // 添加线nameID到数组，
        //     }
        //     else  //已经是高亮状态了 再次点击修改为原始状态
        //     {
        //         Highlight(nameid,10, 10);
        //         temp.splice(exists, 1);  //删除对应的nameID
        //     }
        // }
        // var Highlight = function (nameid,width1, width2) {
        //     for (var o = 0; o < entities.length; o++) {
        //         var m = entities[o];
        //         if (nameid == o) {
        //             m.polyline.width = width1;
        //             (m.polyline.material = new Cesium.PolylineGlowMaterialProperty({
        //                 glowPower: .1, //一个数字属性，指定发光强度，占总线宽的百分比。
        //                 color: color//Cesium.Color.YELLOW.withAlpha(.9)
        //             }), width2)
        //         }
        //     }
        // }
    });
    //viewer.flyTo(promise);
    // viewer.screenSpaceEventHandler.setInputAction(function onLeftClick(movement) {
    //     var pickedFeature = viewer.scene.pick(movement.position);
    //     if (typeof (pickedFeature) != "undefined")   //鼠标是否点到线上
    //     {
    //         var name_id = pickedFeature.id.nameID;  //获取每条线的nameID
    //         Hightlightline(name_id);
    //     }
    // }, Cesium.ScreenSpaceEventType.LEFT_CLICK);


}; 


export function addWaterAnimation(coordinates, viewer) {
    const primitive = new Cesium.Primitive({
      show: true,
      geometryInstances: new Cesium.GeometryInstance({
        geometry: new Cesium.PolygonGeometry({
          polygonHierarchy: new Cesium.PolygonHierarchy(Cesium.Cartesian3.fromDegreesArrayHeights(coordinates)),
          vertexFormat: Cesium.EllipsoidSurfaceAppearance.VERTEX_FORMAT,
          extrudedHeight: 0, // 只显示水面
          //height: -1,
        }),
      }),
      appearance: new Cesium.EllipsoidSurfaceAppearance({
        aboveGround: true,
        material: new Cesium.Material({
          fabric: {
            type: 'Water',
            uniforms: {
              normalMap: water,
              frequency: 100.0,
              animationSpeed: 0.01,
              amplitude: 10.0,
            },
          },
        }),
        // 水透明化
        fragmentShaderSource: 'varying vec3 v_positionMC;\n'
        + 'varying vec3 v_positionEC;\n'
        + 'varying vec2 v_st;\n'
        + 'void main()\n'
        + '{\n'
        + 'czm_materialInput materialInput;\n'
        + 'vec3 normalEC = normalize(czm_normal3D * czm_geodeticSurfaceNormal(v_positionMC, vec3(0.0), vec3(1.0)));\n'
        + '#ifdef FACE_FORWARD\n'
        + 'normalEC = faceforward(normalEC, vec3(0.0, 0.0, 1.0), -normalEC);\n'
        + '#endif\n'
        + 'materialInput.s = v_st.s;\n'
        + 'materialInput.st = v_st;\n'
        + 'materialInput.str = vec3(v_st, 0.0);\n'
        + 'materialInput.normalEC = normalEC;\n'
        + 'materialInput.tangentToEyeMatrix = czm_eastNorthUpToEyeCoordinates(v_positionMC, materialInput.normalEC);\n'
        + 'vec3 positionToEyeEC = -v_positionEC;\n'
        + 'materialInput.positionToEyeEC = positionToEyeEC;\n'
        + 'czm_material material = czm_getMaterial(materialInput);\n'
        + '#ifdef FLAT\n'
        + 'gl_FragColor = vec4(material.diffuse + material.emission, material.alpha);\n'
        + '#else\n'
        + 'gl_FragColor = czm_phong(normalize(positionToEyeEC), material, czm_lightDirectionEC);\n'
        + 'gl_FragColor.a=0.85;\n'
        + '#endif\n'
        + '}\n',
      }),
    });
    viewer.scene.primitives.add(primitive);
  }

/*
      流动纹理线
       color 颜色
       duration 持续时间 毫秒
*/
export const addWave = (position,color,duration)=> {

    function EllipsoidFadeMaterialProperty(color, duration) {
        this._definitionChanged = new Cesium.Event();
        this._color = undefined;
        this._colorSubscription = undefined;
        this.color = color;
        this.duration = duration;
        this._time = (new Date()).getTime();
    }
    Cesium.defineProperties(EllipsoidFadeMaterialProperty.prototype, {
        isConstant: {
            get: function () {
                return false;
            }
        },
        definitionChanged: {
            get: function () {
                return this._definitionChanged;
            }
        },
        color: Cesium.createPropertyDescriptor('color')
    });
    EllipsoidFadeMaterialProperty.prototype.getType = function (time) {
        return 'EllipsoidFade';
    }
    EllipsoidFadeMaterialProperty.prototype.getValue = function (time, result) {
        if (!Cesium.defined(result)) {
            result = {};
        }
        result.color = Cesium.Property.getValueOrClonedDefault(this._color, time, Cesium.Color.WHITE, result.color);

        result.time = (((new Date()).getTime() - this._time) % this.duration) / this.duration;
        return result;

        // return Cesium.defined(result) || (result = {}),
        //     result.color = Cesium.Property.getValueOrClonedDefault(this._color, time, Cesium.Color.WHITE, result.color),
        //     void 0 === this._time && (this._time = time.secondsOfDay),
        //     result.time = time.secondsOfDay - this._time,
        //     result
    }
    EllipsoidFadeMaterialProperty.prototype.equals = function (other) {
        return this === other ||
            (other instanceof EllipsoidFadeMaterialProperty &&
                Property.equals(this._color, other._color))
    }
    Cesium.EllipsoidFadeMaterialProperty = EllipsoidFadeMaterialProperty;
    Cesium.Material.EllipsoidFadeType = 'EllipsoidFade';
    Cesium.Material.EllipsoidFadeSource =
        "czm_material czm_getMaterial(czm_materialInput materialInput)\n" +
        "{\n" +
        "czm_material material = czm_getDefaultMaterial(materialInput);\n" +
        "material.diffuse = 1.5 * color.rgb;\n" +
        "vec2 st = materialInput.st;\n" +
        "float dis = distance(st, vec2(0.5, 0.5));\n" +
        "float per = fract(time);\n" +
        "if(dis > per * 0.5){\n" +
        "material.alpha = 0.0;\n"+
        "discard;\n" +
        "}else {\n" +
        "material.alpha = color.a  * dis / per / 1.0;\n" +
        "}\n" +
        "return material;\n" +
        "}";
    Cesium.Material._materialCache.addMaterial(Cesium.Material.EllipsoidFadeType, {
        fabric: {
            type: Cesium.Material.EllipsoidFadeType,
            uniforms: {
                color: new Cesium.Color(1.0, 0.0, 0.0, 1),
                time: 0
            },
            source: Cesium.Material.EllipsoidFadeSource
        },
        translucent: function (material) {
            return true;
        }
    });

    viewer.entities.add({
        name: 'EllipsoidFade',
        position: position,
        ellipse: {
            height: 0,
            semiMinorAxis: 30000.0,
            semiMajorAxis: 30000.0,
            material: new Cesium.EllipsoidFadeMaterialProperty(color, duration)
        }
    });
}
/**
 * @desc 通过圆心和半径绘制圆墙
 * @param {Object} options
 * {
    dataSourceName: 'fence',//围栏source名
    type: 'fence',//围栏类型
    name: "ellipWall",//围栏名称
    id:1,//围栏id
    x:113.97012095140055,//围栏经度 +-180~
    y:22.52298214113242,//围栏纬度 +-90~
    // r:50,//围栏半径 默认为50米
    // height: 15//围栏高度 默认为15
    }
 * @returns
 */
export const createEllipWallGraphic = (options={}) => {
    // let dataSource = new Cesium.CustomDataSource(options.dataSourceName || 'dataName');
    // let lineFlowMaterial = new LineFlowMaterial({//动画线材质
    //     color: new Cesium.Color.fromCssColorString("#ff0000"),
    //     duration: 2000, //时长，控制速度
    //     url: fencePng,
    //     axisY: true
    // });
        var ellipse = new Cesium.EllipseOutlineGeometry({
            center : Cesium.Cartesian3.fromDegrees(options.x||113.97012095140055,options.y||22.52298214113242),
            semiMajorAxis : options.r||50,
            semiMinorAxis : options.r||50,
            // granularity:0.5
            // rotation : Cesium.Math.toRadians(60.0)
        });
        var geometry = Cesium.EllipseOutlineGeometry.createGeometry(ellipse);
        var circlepoints =[];
        var values = geometry.attributes.position.values;
        if (!values) return;
        var ilen = values.length / 3; //数组中以笛卡尔坐标进行存储 每3个值一个坐标
        for (var i = 0; i < ilen; i++) {
            var xyz = new Cesium.Cartesian3(values[i * 3], values[i * 3 + 1], values[i * 3 + 2]); //笛卡尔
            circlepoints.push(xyz);
        }
        circlepoints.push(circlepoints[0])
        let maximumHeights=[]
        let minimumHeights=[]
        circlepoints.forEach(item=>{
            maximumHeights.push(options.height||15)
            minimumHeights.push(0)
        })
        //     viewer.entities.add(
        //     {
        //     name: options.name||"fence",
        //     prop: options,
        //     id: `${options.type||"fence"}_${options.id}`,
        //     wall: {
        //         positions: circlepoints,
        //         maximumHeights: maximumHeights,
        //         minimumHeights: minimumHeights,
        //         material: lineFlowMaterial
        //     },
        // });

        var wallInstance = new Cesium.GeometryInstance({
            geometry: Cesium.WallGeometry.fromConstantHeights({
                positions: circlepoints,
                maximumHeight: options.h || 50,
                vertexFormat: Cesium.MaterialAppearance.VERTEX_FORMAT,
            }),
        })
        let image = fencePng, //选择自己的动态材质图片
        color = new Cesium.Color.fromCssColorString('rgba(0, 255, 255, 0.3)'),
        speed = 0,
        source =
        'czm_material czm_getMaterial(czm_materialInput materialInput)\n\
        {\n\
            czm_material material = czm_getDefaultMaterial(materialInput);\n\
            vec2 st = materialInput.st;\n\
            vec4 colorImage = texture2D(image, vec2(fract((st.t - speed*czm_frameNumber*0.005)), st.t));\n\
            vec4 fragColor;\n\
            fragColor.rgb = color.rgb / 1.0;\n\
            fragColor = czm_gammaCorrect(fragColor);\n\
            material.alpha = colorImage.a * color.a;\n\
            material.diffuse = (colorImage.rgb+color.rgb)/2.0;\n\
            material.emission = fragColor.rgb;\n\
            return material;\n\
        }'

        let material = new Cesium.Material({
        fabric: {
            type: 'PolylinePulseLink',
            uniforms: {
            color: color,
            image: image,
            speed: speed,
            },
            source: source,
        },
        translucent: function () {
            return true
        },
        })

        window.viewer.scene.primitives.add(
            new Cesium.Primitive({
                geometryInstances: [wallInstance],
                appearance: new Cesium.MaterialAppearance({
                    material: material,
                }),
            })
        )

}

export const replaceWaterFeature = (feature) =>{
    let model = feature.content._model;
    //model.silhouetteColor = Cesium.Color.RED;
    //model.silhouetteSize = 5;
    // model.appearance = new Cesium.EllipsoidSurfaceAppearance({
    //     aboveGround: true,
    //     material: new Cesium.Material({
    //       fabric: {
    //         type: 'Water',
    //         uniforms: {
    //           normalMap: water,
    //           frequency: 100.0,
    //           animationSpeed: 0.01,
    //           amplitude: 10.0,
    //         },
    //       },
    //     }),
    //     // 水透明化
    //     fragmentShaderSource: 'varying vec3 v_positionMC;\n'
    //     + 'varying vec3 v_positionEC;\n'
    //     + 'varying vec2 v_st;\n'
    //     + 'void main()\n'
    //     + '{\n'
    //     + 'czm_materialInput materialInput;\n'
    //     + 'vec3 normalEC = normalize(czm_normal3D * czm_geodeticSurfaceNormal(v_positionMC, vec3(0.0), vec3(1.0)));\n'
    //     + '#ifdef FACE_FORWARD\n'
    //     + 'normalEC = faceforward(normalEC, vec3(0.0, 0.0, 1.0), -normalEC);\n'
    //     + '#endif\n'
    //     + 'materialInput.s = v_st.s;\n'
    //     + 'materialInput.st = v_st;\n'
    //     + 'materialInput.str = vec3(v_st, 0.0);\n'
    //     + 'materialInput.normalEC = normalEC;\n'
    //     + 'materialInput.tangentToEyeMatrix = czm_eastNorthUpToEyeCoordinates(v_positionMC, materialInput.normalEC);\n'
    //     + 'vec3 positionToEyeEC = -v_positionEC;\n'
    //     + 'materialInput.positionToEyeEC = positionToEyeEC;\n'
    //     + 'czm_material material = czm_getMaterial(materialInput);\n'
    //     + '#ifdef FLAT\n'
    //     + 'gl_FragColor = vec4(material.diffuse + material.emission, material.alpha);\n'
    //     + '#else\n'
    //     + 'gl_FragColor = czm_phong(normalize(positionToEyeEC), material, czm_lightDirectionEC);\n'
    //     + 'gl_FragColor.a=0.85;\n'
    //     + '#endif\n'
    //     + '}\n',
    //   })

    if (
        model &&
        model._sourcePrograms &&
        model._rendererResources
    ) {
        Object.keys(model._sourcePrograms).forEach((key) => {
        let program = model._sourcePrograms[key];
        let fragmentShader =//片段着色器
            model._rendererResources.sourceShaders[
            program.fragmentShader
            ];
        model._rendererResources.sourceShaders[
            program.fragmentShader
        ] =
                    // 水透明化
         'varying vec3 v_positionMC;\n'
        + 'varying vec3 v_positionEC;\n'
        + 'varying vec2 v_st;\n'
        + 'void main()\n'
        + '{\n'
        + 'czm_materialInput materialInput;\n'
        + 'vec3 normalEC = normalize(czm_normal3D * czm_geodeticSurfaceNormal(v_positionMC, vec3(0.0), vec3(1.0)));\n'
        + '#ifdef FACE_FORWARD\n'
        + 'normalEC = faceforward(normalEC, vec3(0.0, 0.0, 1.0), -normalEC);\n'
        + '#endif\n'
        + 'materialInput.s = v_st.s;\n'
        + 'materialInput.st = v_st;\n'
        + 'materialInput.str = vec3(v_st, 0.0);\n'
        + 'materialInput.normalEC = normalEC;\n'
        + 'materialInput.tangentToEyeMatrix = czm_eastNorthUpToEyeCoordinates(v_positionMC, materialInput.normalEC);\n'
        + 'vec3 positionToEyeEC = -v_positionEC;\n'
        + 'materialInput.positionToEyeEC = positionToEyeEC;\n'
        + 'czm_material material = czm_getMaterial(materialInput);\n'
        + '#ifdef FLAT\n'
        + 'gl_FragColor = vec4(material.diffuse + material.emission, material.alpha);\n'
        + '#else\n'
        + 'gl_FragColor = czm_phong(normalize(positionToEyeEC), material, czm_lightDirectionEC);\n'
        + 'gl_FragColor.a=0.85;\n'
        + '#endif\n'
        + '}\n'
        +`czm_material czm_getMaterial(czm_materialInput materialInput)
            {
                czm_material material = czm_getDefaultMaterial(materialInput);

                float time = czm_frameNumber * animationSpeed_2;

                // fade is a function of the distance from the fragment and the frequency_1 of the waves
                float fade = max(1.0, (length(materialInput.positionToEyeEC) / 10000000000.0) * frequency_1 * fadeFactor_8);

                float specularMapValue = texture2D(specularMap_6, materialInput.st).r;

                // note: not using directional motion at this time, just set the angle to 0.0;
                vec4 noise = czm_getWaterNoise(normalMap_0, materialInput.st * frequency_1, time, 0.0);
                vec3 normalTangentSpace = noise.xyz * vec3(1.0, 1.0, (1.0 / amplitude_3));

                // fade out the normal perturbation as we move further from the water surface
                normalTangentSpace.xy /= fade;

                // attempt to fade out the normal perturbation as we approach non water areas (low specular map value)
                normalTangentSpace = mix(vec3(0.0, 0.0, 50.0), normalTangentSpace, specularMapValue);

                normalTangentSpace = normalize(normalTangentSpace);

                // get ratios for alignment of the new normal vector with a vector perpendicular to the tangent plane
                float tsPerturbationRatio = clamp(dot(normalTangentSpace, vec3(0.0, 0.0, 1.0)), 0.0, 1.0);

                // fade out water effect as specular map value decreases
                material.alpha = mix(blendColor_5.a, baseWaterColor_4.a, specularMapValue) * specularMapValue;

                // base color is a blend of the water and non-water color based on the value from the specular map
                // may need a uniform blend factor to better control this
                material.diffuse = mix(blendColor_5.rgb, baseWaterColor_4.rgb, specularMapValue);

                // diffuse highlights are based on how perturbed the normal is
                material.diffuse += (0.1 * tsPerturbationRatio);

                material.diffuse = material.diffuse;

                material.normal = normalize(materialInput.tangentToEyeMatrix * normalTangentSpace);

                material.specular = specularIntensity_7;
                material.shininess = 10.0;

                return material;
            }`
        });
        model._shouldRegenerateShaders = true;
    }
}

export const  addFlyintLine = (positions) => {
    const primitive = new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
            geometry: new Cesium.PolylineGeometry({
                positions: Cesium.Cartesian3.fromDegreesArrayHeights([positions[0],positions[1],10,positions[0],positions[1],1000]),
                width: 1.0,

                vertexFormat: Cesium.PolylineMaterialAppearance.VERTEX_FORMAT
            })
        }),
        appearance: new Cesium.PolylineMaterialAppearance({
            //material : Cesium.Material.fromType('Color')
            material: new Cesium.Material({
                fabric:{
                    //type: Cesium.Material.PolylineImageTrailType,
                    uniforms: {
                        color: Cesium.Color.CYAN,
                        speed: 10 * Math.random(),
                        percent: 0.1,
                        gradient: 0.01
                    },
                    source:`czm_material czm_getMaterial(czm_materialInput materialInput){
                        czm_material material = czm_getDefaultMaterial(materialInput);
                        vec2 st = materialInput.st;
                        float t =fract(czm_frameNumber * speed / 1000.0);
                        t *= (1.0 + percent);
                        float alpha = smoothstep(t- percent, t, st.s) * step(-t, -st.s);
                        alpha += gradient;
                        material.diffuse = color.rgb;
                        material.alpha = alpha;
                        return material;
                      }`
                },
                translucent: function(material) {
                    return true
                }
            })
        })
    });
    viewer.scene.primitives.add(primitive);


}
