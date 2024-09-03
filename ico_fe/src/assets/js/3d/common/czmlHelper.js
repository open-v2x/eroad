


class czmlHelper {
  scene = viewer.scene;
  clock = viewer.clock;

  constructor(czml){
    var dataSourcePromise = Cesium.CzmlDataSource.load(
      czml
    );
    viewer.clock.shouldAnimate = false;
    viewer.dataSources.add(dataSourcePromise).then((dataSource) => {
      if (viewer.scene.clampToHeightSupported) {
        //viewer.clock.shouldAnimate = true;
        //for(var i=0;i<dataSource.entities.values.length;i++){
          this.entity = dataSource.entities.values[0];

          this.entity.orientation =new Cesium.VelocityOrientationProperty(this.entity.position);

          this.entity.position.setInterpolationOptions({
            interpolationDegree: 0,
            interpolationAlgorithm: Cesium.HermitePolynomialApproximation,
          });

          //var position = this.entity.position.getValue(viewer.clock.currentTime);
          //this.entity.position = viewer.scene.clampToHeight(position,[this.entity]);
          //this.entity.show = false;
          //this.arr.push(entity);
          //var entity = dataSource.entities.getById("ambulance");

          //this.entity.show = false;

      } else {
        window.alert("This browser does not support clampToHeight.");
      }
    });

  }

  start = (b) => {
    if(b){
      this.entity.show = false;;
      //viewer.trackedEntity = this.entity;
    }

    //this.entity.orientation =new Cesium.VelocityOrientationProperty(this.entity.position);
    viewer.clock.shouldAnimate = true;
    var objectsToExclude = [this.entity];
    this.positionProperty = this.entity.position;//放在postrender里变为常量，放外边为变量
    //viewer.clock.onTick.addEventListener(() =>{
    viewer.scene.postRender.addEventListener(() => {
      var position = this.positionProperty.getValue(viewer.clock.currentTime);
      this.entity.position = viewer.scene.clampToHeight(position, objectsToExclude);


      let ori  = this.entity.orientation.getValue(viewer.clock.currentTime);//获取偏向角
      let center  = this.entity.position.getValue(viewer.clock.currentTime);//获取位置

      if(b && ori && center){
        let transform = Cesium.Matrix4.fromRotationTranslation(Cesium.Matrix3.fromQuaternion(ori), center);//将偏向角转为3*3矩阵，利用实时点位转为4*4矩阵
        viewer.camera.lookAtTransform(transform, new Cesium.Cartesian3(-100, 0, 2))//将相机向后面放一点
      }

    });
  }
  end(){
    viewer.trackedEntity =undefined;
    viewer.clock.shouldAnimate = false;
    viewer.entities.remove(this.entity);
  }
}

export default czmlHelper;


