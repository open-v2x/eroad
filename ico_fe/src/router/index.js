import { createRouter, createWebHashHistory } from 'vue-router';

export default createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            redirect: '/roadPerception',
            meta: {
                keepAlive: false
            },
            beforeEnter: (to, from, next) => {
               next({ path: '/admin' })
            }
        },
        {
            path: '/login/:expire*',
            component: () => import('../components/pages/LoginNew.vue'),
            meta: {
                keepAlive: false
            }
        },
       // 后台管理路由配置优化
        {
          path: '/admin',
          component: () => import('../components/pages/Admin/Layout/index.vue'),
          redirect: '/admin/device',  // 重定向到 /admin/index
          children: [
            // 默认子路由
            {
              path: 'index',
              name: 'AdminIndex',
              component: () => import('../components/pages/Admin/index.vue'),
            },
            // 设备列表
            {
              path: 'device',
              name: 'DeviceList',
              component: () => import('../components/pages/Admin/Device/index.vue'),
            },
            // 系统管理
            {
              path: 'system',
              component: () => import('../components/pages/Admin/System/index.vue'),
              children: [
                // 字典管理
                {
                  path: 'dict',
                  name: 'Dict',
                  component: () => import('../components/pages/Admin/System/Dict/index.vue'),
                },
                // 字典详情页
                {
                  path: 'dict/:id',  // 添加动态参数 :id
                  name: 'DictData',
                  component: () => import('../components/pages/Admin/System/Dict/data.vue'),
                },
                // 操作日志
                {
                  path: 'operation',
                  name: 'OperationLog',
                  component: () => import('../components/pages/Admin/System/LogFile/Operation/index.vue'),
                },
                // 告警日志
                {
                  path: 'alarm',
                  name: 'AlarmLog',
                  component: () => import('../components/pages/Admin/System/LogFile/Alarm/index.vue'),
                }
              ]
            }
          ]
        },
        // 综合态势
        {
            path: '/roadPerception',
            component: () => import('../components/pages/RoadPerception/Index.vue'),
            meta: {
                keepAlive: false
            },
            beforeEnter: (to, from, next) => {
              next()
            }
        },
        // 指标分析
        {
            path: '/tables',
            component: () => import('../components/pages/Tables/Table.vue'),
            meta: {
                keepAlive: false
            },
            beforeEnter: (to, from, next) => {
              next()
            }
        }
    ]
})
