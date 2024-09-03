<template>
    <div class="wrapper">
        <v-head></v-head>
        <!-- 【管理查看用户】权限不显示sidebar -->
        <v-sidebar v-if="role!='ROLE_USER_MANAGER_LOOK_OVER'"></v-sidebar>
        <div class="content-box" :class="{'content-collapse':collapse, 'zero-left': zeroLeft}">
            <!-- 【管理查看用户】权限不显示tags -->
            <v-tags v-if="role!='ROLE_USER_MANAGER_LOOK_OVER'"></v-tags>
            <div class="content">
                <transition name="move" mode="out-in">
                    <keep-alive :include="keepAliveList">
                        <router-view></router-view>
                    </keep-alive>
                </transition>
            </div>
        </div>
    </div>
</template>

<script>
    import vHead from './Header.vue';
    import vSidebar from './Sidebar.vue';
    import vTags from './Tags.vue';
    import bus from '@/assets/js/bus';
    export default {
        data(){
            return {
                tagsList: [],
                keepAliveList: [ // 需要keep-alive的页面
                    'AuthorityManagement',
                    'workbench_list',
                    'invitation_info',
                    'undistributed_guest_list',
                    'invitation_code_list',
                    'comprehensive_guest_management',
                    'invitation_company',
                    'file_list',
                    'statistic_analysis',
                    'notice_list',
                    'repeat_guest_list',
                    'dictionary_list',
                    'contact_person_list',
                    'log_list',
                    'dictionary_content_list',
                    'niti_guest_list'
                ],
                collapse: false,
                role: '',
                zeroLeft: false
            }
        },
        components:{
            vHead, vSidebar, vTags
        },
        created(){
            this.role = localStorage.getItem('hd_role');
            // [管理查看用户]sidebar要收起来
            if (this.role == 'ROLE_USER_MANAGER_LOOK_OVER') {
                this.collapse = true
                this.zeroLeft = true
            }
            bus.$on('collapse', msg => {
                this.collapse = msg;
            })

            // 只有在标签页列表里的页面才使用keep-alive，即关闭标签之后就不保存到内存中了。
            bus.$on('tags', msg => {
                let arr = [];
                for(let i = 0, len = msg.length; i < len; i ++){
                    msg[i].name && arr.push(msg[i].name);
                }
                this.tagsList = arr;
            })
            bus.$on('goLogin',res => {
                if (this.role === 'ROLE_USER_MANAGER_LOOK_OVER') {
                    this.$router.push('/user_login')
                } else {
                    this.$router.push('/login')
                }
            })
        }
    }
</script>
<style scoped>
    .content-box.zero-left {
        left: 0;
        padding-bottom: 4px;
    }
</style>


