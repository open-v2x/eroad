
import Vue, { createApp } from 'vue'
import popupWindow from "../../../../components/common/PopupWindow/popupWindow";
import mark from '../../../img/bounceMarker.png';


class BounceMarker {
  constructor(viewer, [lng, lat, height],opts = {
    image: mark,
    bounceHeight: 100,
    increment: 0.05,
  }, propertity = {}) {
    this.viewer = viewer;
    this.lng = Number(lng);
    this.lat = Number(lat);
    this.height = height;
    this.propertity = propertity;
    this.opts = opts;
    this.add();
  }

  add() {
    const e = this;
    const i = this.height + this.opts.bounceHeight;
    let t = 0;
    let s = 0;
    this.bounceMarker = this.viewer.entities.add({
      position: new Cesium.CallbackProperty(() => {
        s += e.opts.increment;
        t += s;
        if (t > e.opts.bounceHeight) {
          t = e.opts.bounceHeight;
          s *= -1;
          s *= 0.55;
        }
        return Cesium.Cartesian3.fromDegrees(e.lng, e.lat, i - t);
      }, false),
      billboard: {
        image: this.opts.image,
      },
      propertity:this.propertity,
      lng:this.lng,
      lat:this.lat
    });
    this.bounceMarker.bounce = function () {
      e.bounce();
    };

    this.bounceMarker.showPop = function(){
      var node = document.createElement("div");
      node.className = "fixposition";
      e.viewer.container.appendChild(node);
      //app.$destroy();
      var app = new Vue(popupWindow);

      //app.use(ElementPlus);
      //app.isclose = false;
      let vm = app.$mount(node);
      vm.$nextTick(function () {
        //var position = Cesium.Cartesian3.fromDegrees(point._lng, point._lat);
            //var m_cartographic = Cesium.Cartographic.fromCartesian(position);
            //let height = window.viewer.scene.globe.getHeight(m_cartographic); //todo 不知能否获得地表附着物的高度
        if(e&&e.lng&& e.lat){

          vm.$data.position = Cesium.Cartesian3.fromDegrees(e.lng, e.lat, 0);
          //vm.$data.titlex = "测试的标题";
          //vm.$data.innerValue = JSON.stringify(
          //that.featureContent.FeatureContent
          //);

          vm.$data.viewCom = e.propertity['viewCom'];//'messagePop';
          vm.$data.propertity = e.propertity;//{'displayTime':'17-09-13/9:52','name':'张三','phone':'1234567910','content':'王五昨天在王博广场东头打架,把人打进医院'}
        }
      });
    }

  }

  bounce() {
    this.remove();
    this.add();
  }

  remove() {
    this.viewer.entities.remove(this.bounceMarker);
  }


}
function initEvent(viewer) {
  new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas).setInputAction((i) => {
    const t = viewer.scene.pick(i.position);
    if(t && t.id && t.id.showPop()){
      t.id.showPop(t.id);
      t && t.id && t.id.bounce && t.id.bounce() ;
    }

    //弹窗

  }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
}


function showPop2(viewer,point){

  var node = document.createElement("div");
  node.className = "fixposition";
  viewer.container.appendChild(node);
  //app.$destroy();
  var app = new Vue(popupWindow);

  //app.use(ElementPlus);
  //app.isclose = false;
  let vm = app.$mount(node);
  vm.$nextTick(function () {
    //var position = Cesium.Cartesian3.fromDegrees(point._lng, point._lat);
        //var m_cartographic = Cesium.Cartographic.fromCartesian(position);
        //let height = window.viewer.scene.globe.getHeight(m_cartographic); //todo 不知能否获得地表附着物的高度
    if(point&&point._lng&& point._lat){

      vm.$data.position = Cesium.Cartesian3.fromDegrees(point._lng, point._lat, 0);
      //vm.$data.titlex = "测试的标题";
      //vm.$data.innerValue = JSON.stringify(
      //that.featureContent.FeatureContent
      //);

      vm.$data.viewCom = point._propertity['弹窗'];//'messagePop';
      vm.$data.propertity = point._propertity;//{'displayTime':'17-09-13/9:52','name':'张三','phone':'1234567910','content':'王五昨天在王博广场东头打架,把人打进医院'}
    }
  });

}


export default BounceMarker;
export { initEvent };
