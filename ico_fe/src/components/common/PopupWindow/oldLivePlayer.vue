<template>
  <div class="lp">
    <Jessibuca

        :playUrl="videoUrl[0]"
        >
        <!-- <iframe style="width:100%;height:100%" src="http://192.168.137.101:8081/#/play/wasm/ws%3A%2F%2F192.168.137.1%3A80%2Frtp%2F34020000001320000003_003.flv"></iframe> -->
    </Jessibuca>
  </div>
</template>
<script>
import LivePlayer from '@liveqing/liveplayer-v3' // vue3
import Jessibuca from './Jessibuca.vue'
import * as echartSetSize from '../../../assets/js/echartsSetSize'

export default {
    props:['videoUrl'],
    components: {
    Jessibuca
},
    data(){
        return{
            height: echartSetSize.setSize(290),
            width: echartSetSize.setSize(500),
        }
    },
    mounted(){
    },
    methods:{


    }
}
</script>
<style scoped>
    .lp{
        width: 500px;
        height: 290px;
    }
</style>
