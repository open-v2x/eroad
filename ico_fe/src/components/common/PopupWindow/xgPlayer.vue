<template>
    <div id="mse" :style="{
      width: width + 'px',
      height: height + 'px',
    }">
        <!-- <iframe style="width:100%;height:100%" src="http://192.168.137.101:8081/#/play/wasm/ws%3A%2F%2F192.168.137.1%3A80%2Frtp%2F34020000001320000003_003.flv"></iframe> -->
    </div>

</template>
<script>
import * as Player from "xgplayer"
import * as echartSetSize from '../../../assets/js/echartsSetSize'

export default {
    data(){
        return{
            height: echartSetSize.setSize(290),
            width: echartSetSize.setSize(500),
            val:this.$parent.$parent.propertity
        }
    },
    mounted(){
        console.log('播放地址',this.$parent.$parent.propertity);
        let player = new Player({
            id: 'mse',
            url: this.val.url,
            autoplay: true,
            cors:true,
            controls: true,
            ignores:['time']
        });
    },
    methods:{


    }
}
</script>
<style scoped>
    #mse{
        z-index: 1;
    }
</style>
