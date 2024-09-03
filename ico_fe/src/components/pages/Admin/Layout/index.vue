<template>
  <!-- 使用 ConfigProvider 设置全局配置 -->
  <el-config-provider :size="defaultSize">
    <el-container style="height: 100vh;">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div>数字道路智能运营中心</div>
        <el-button type="primary" @click="logout">返回大屏</el-button>
      </el-header>

      <!-- 底部主内容区 -->
      <el-container>
        <!-- 侧边栏 -->
        <el-aside class="sidebar">
          <el-scrollbar style="height: 100%;">
            <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleSelect">
              <el-menu-item index="/admin/device"><el-icon><location /></el-icon> 设备管理 </el-menu-item>
              <el-sub-menu index="1">
                <template #title>
                  <el-icon><Setting /></el-icon>
                  <span>系统管理</span>
                </template>
                <el-menu-item index="/admin/system/dict">字典管理</el-menu-item>
                <el-sub-menu index="/admin/system/operation">
                  <template #title>日志管理</template>
                  <el-menu-item index="/admin/system/operation">操作日志</el-menu-item>
                  <el-menu-item index="/admin/system/alarm">告警日志</el-menu-item>
                </el-sub-menu>
              </el-sub-menu>
              
            </el-menu>
          </el-scrollbar>
        </el-aside>

        <!-- 主要内容 -->
        <el-main class="main-content">
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<script>
export default {
  name: 'AdminLayout',
  data() {
    return {
      activeMenu: this.$route.path, // 默认激活当前路由路径
      defaultSize: 'default' // 设置默认大小（可选值：medium / small / mini）'large' | 'default' | 'small'
    };
  },
  watch: {
    // 监听路由变化
    $route(newRoute) {
      this.activeMenu = newRoute.path;
    }
  },
  methods: {
    handleSelect(key, keyPath) {
      this.$router.push(key); // 使用 vue-router 进行导航
    },
    logout() {
      this.$router.push({ path: '/' });
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 20px;
  background-color: #409EFF;
  color: white;
  font-size: 20px;
}

.sidebar {
  width: 200px;
  background-color: #fff;
  height: calc(100vh - 64px); /* 减去 header 的高度 */
  overflow: hidden;
}

.main-content {
  padding: 20px;
  background-color: #f0f2f5;
}

.el-menu-vertical-demo {
  border-right: none;
}
</style>
