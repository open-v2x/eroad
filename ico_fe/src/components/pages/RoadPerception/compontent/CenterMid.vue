<template>
  <div>
    <div class="left">
      <div class="Image"></div>
    </div>
    <div class="right">
        <div class="top">主要路口</div>
        <div class="bottom">
            <span>166</span>
            <span>个</span>
        </div>
    </div>
  </div>
</template>

<script>
export default {

}
</script>

<style lang="less" scoped>
.left{
    flex: 1;
    .Image{
        height: 51px;
        width: 60px;
        background: url('../../../../assets/img/Frame 42732190.png') no-repeat;
        background-size: 100% 100%;
    }
}
.right{
    flex: 2;
    display: flex;
    flex-direction: column;
}
.top{
    margin-bottom: 6px;
    height: 18px;
    width: 120px;
    padding-left: 6px;
    font-family: 'Noto Sans SC';
    font-style: normal;
    font-weight: 400;
    font-size: 12px;
    line-height: 18px;
    color: #FFFFFF;
    background: url('../../../../assets/img/Frame 427321697.png') no-repeat;
    background-size: 100% 100%;
}
.bottom{
    height: 27px;
    span:first-child{
        font-family: 'Digital Numbers';
        font-style: normal;
        font-weight: 400;
        font-size: 21px;
        line-height: 27px;
        color: #01FFFF;
    }
    span:last-child{
        font-family: 'Noto Sans SC';
        font-style: normal;
        font-weight: 400;
        font-size: 12px;
        line-height: 13.5px;
        color: rgba(255, 255, 255, 0.8);
    }
}
</style>