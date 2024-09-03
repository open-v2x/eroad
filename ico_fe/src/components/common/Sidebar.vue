<template>
    <div class="sidebar">
        <el-menu class="sidebar-el-menu" :default-active="onRoutes" :collapse="collapse" background-color="#324157"
            text-color="#bfcbd9" active-text-color unique-opened :default-openeds="defaultOpeneds" router>
            <template v-for="item in items2">
                <template v-if="item.subs">
                    <el-submenu :index="item.uri" :key="item.uri">
                        <template slot="title">
                            <i :class="item.icon"></i><span slot="title">{{ item.name }}</span>
                        </template>
                        <template v-for="subItem in item.subs">
                            <el-submenu v-if="subItem.subs" :index="subItem.uri" :key="subItem.uri">
                                <template slot="title">{{ subItem.name }}</template>
                                <el-menu-item v-for="(threeItem,i) in subItem.subs" :key="i" :index="threeItem.uri">
                                    {{ threeItem.name }}
                                </el-menu-item>
                            </el-submenu>
                            <el-menu-item v-else :index="subItem.uri" :key="subItem.uri">
                                {{ subItem.name }}
                            </el-menu-item>
                        </template>
                    </el-submenu>
                </template>
                <template v-else>
                    <el-menu-item :index="item.uri" :key="item.uri">
                        <i :class="item.icon"></i><span slot="title">{{ item.name }}</span>
                    </el-menu-item>
                </template>
            </template>
        </el-menu>
    </div>
</template>

<script>
    // import { getMenu } from '@/components/common/api'
    import bus from '@/assets/js/bus';
    export default {
        data() {
            return {
                collapse: false,
                defaultOpeneds: ['tenant_management'],
                items: [],
                items2: [
                    {
                        icon: 'el-icon-lx-home',
                        uri: 'dashboard',
                        name: '系统首页'
                    },
                    {
                        icon: 'el-icon-lx-calendar',
                        uri: 'tenant_management',
                        name: '基础设置',
                        subs: [
                            {
                                uri: '/tenantList',
                                name: '租户管理'
                            },
                            {
                                uri: '/orgFirmManagement',
                                name: '厂商组织管理'
                            },
                            {
                                uri: '/roleManagement',
                                name: '角色管理'
                            }
                        ]
                    }
                ]
            }
        },
        computed:{
            onRoutes(){
                let path = this.$route.path;
                let idx = path.substr(1).indexOf('/') + 1
                return idx > 0 ? path.substr(0, idx) : path;
            }
        },
        created(){
            // getMenu().then(res => {
            //     let _data = res.data.data
            //     let newData = []
            //     _data.forEach(ele => {
            //         if (ele.pid == 0) {
            //             newData.push(ele)
            //         } else {
            //             newData.forEach(x => {
            //                 if (ele.pid == x.id) {
            //                     if (!x.subs) {
            //                         x.subs = []
            //                     }
            //                     // 是父级目录时后台没返回uri这个字段，这里手动赋值就是为了不想看到vue的warning
            //                     x.uri = x.uri || '' 
            //                     x.subs.push(ele)
            //                 }
            //             })
            //         }
            //     });
            //     this.items = newData
            // })
            // 通过 Event Bus 进行组件间通信，来折叠侧边栏
            bus.$on('collapse', msg => {
                this.collapse = msg;
            })
        }
    }
</script>

<style scoped>
    .sidebar{
        display: block;
        position: absolute;
        left: 0;
        top: 80px;
        bottom:0;
        overflow-y: scroll;
    }
    .sidebar::-webkit-scrollbar{
        width: 0;
    }
    .sidebar-el-menu:not(.el-menu--collapse){
        width: 250px;
    }
    .sidebar > ul {
        height:100%;
    }
</style>
