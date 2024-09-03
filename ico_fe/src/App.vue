<template>
    <div>
      <keep-alive>
          <router-view v-if="$route.meta.keepAlive"></router-view>
      </keep-alive>
      <router-view v-if="!$route.meta.keepAlive"></router-view>
    </div>
</template>
<script>
export default {
  provide() {
    return {
      reload: this.reload
    }
  },
  data() {
    return {
      // client: Stomp.client(user_center.mq_server),
      isReload: true
      // isRouterAlive: true
    }
  },
  mounted() {
    window.addEventListener('resize',this.emitResize)
  },
  unmounted() {
    window.removeEventListener('resize',this.emitResize)
  },
  methods: {
    emitResize() {
      this.$bus.emit('resize')
    }
  }
}
</script>
<style>
  @import "./assets/css/main.css";
  @import "./assets/css/color-dark.css";     /*深色主题*/
  /*@import "./assets/css/theme-green/color-green.css";   浅绿色主题*/
  #app {
    font-family: 'SimHei';
  }
</style>
