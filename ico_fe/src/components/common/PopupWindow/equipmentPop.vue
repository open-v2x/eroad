<template>
    <div :style="{
      width: width + 'px',
      height: height + 'px',
    }">
        <div class="scroll_top">
            <span class="scroll_title">{{val.设备名称}}</span>
            <!--<span class="scroll_level scroll_level01">紧急</span>-->
            <span class="scroll_timer">{{val.设备编号}}</span>
            <a class="localize"></a>
        </div>
        <div class="msg_cage">
            <a class="localize_title">位置:{{val.位置}}</a>
        </div>
        <div class="msg_cage">
            <a class="localize_title">运行状态:{{val.运行状态}}</a>
        </div>
        <div class="msg_cage">
            <p class="localize_msg">{{val.详情}}</p>
        </div>
    </div>
</template>
<script>

export default {
    data(){
        return{
            width:300,
            height:150,
            val:this.$parent.$parent.$parent.propertity
        }
    },
    mounted(){
        this.$parent.$parent.$parent.propertity
        this.val;
    }
}
</script>
<style scoped>
.message_scroll{
    border: rgba(19,25,47,0) 1px solid;
    background-color: rgba(19,25,47,0);
    height: 100px;
    cursor: pointer;
    margin-bottom: 6px;
}

.scroll_top{
    height: 25px;
}
.scroll_title{
    float: left;
    background-image: url("../../../assets/img/popup/pushmessage_class.png");
    background-repeat: no-repeat;
    width: 150px;
    line-height: 25px;
    color: white;
    font-size: 14px;
    text-align: center;
}
.scroll_level{
    float: left;
    background-repeat: no-repeat;
    line-height: 25px;
    width: 56px;
    background-position-y:3px;
    color: white;
    font-size: 12px;
    text-align: center;
    margin-left: 8px;
}
.scroll_level01{
    background-image:url("../../../assets/img/popup/pushmessage_level01.png");
}
.scroll_level02{
    background-image:url("../../../assets/img/popup/pushmessage_level02.png");
}
.scroll_level03{
    background-image:url("../../../assets/img/popup/pushmessage_level03.png");
}
.localize{
    display: block;
    line-height: 25px;
    margin-left: 8px;
    background-image: url("../../../assets/img/popup/pushmessage_localize_01.png");
    background-repeat: no-repeat;
    background-position-y:3px;
    width: 14px;
    height: 25px;
    float: left;
    cursor:hand;
}
.localize:hover{
    background-image: url("../../../assets/img/popup/pushmessage_localize_02.png");
}
.scroll_timer{
    color: #FFFF00;
    font-size: 16px;
    line-height: 25px;
    text-align: right;
    display: block;
    float: left;
    margin-left: 8px;
    cursor:hand;
}
.msg_cage{
    padding-left: 10px;
    padding-right: 6px;
    height: 18px;
    overflow: hidden;
    margin-top: 8px;
}
.msg_cage1{
    padding-left: 10px;
    padding-right: 6px;
    height: 30px;
    overflow: hidden;
    margin-top: 8px;
}
.localize_title{
    color: #FFFF00;
}
.localize_msg{
    font-size: 14px;
    color: white;
    word-break:normal !important;display:block;white-space:pre-wrap !important;
    width: 100%;
}
</style>