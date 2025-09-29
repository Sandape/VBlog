<template>
  <el-container class="home_container">
    <el-header>
      <div class="home_title">iwhale Prompt</div>
      <div class="home_userinfoContainer">
        <span class="home_username">{{currentUserName}}</span>
        <el-button
          type="text"
          class="logout_button"
          @click="handleLogout"
          icon="el-icon-switch-button">
          退出登录
        </el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu
          default-active="0"
          class="el-menu-vertical-demo" style="background-color: #ECECEC" router>
          <template v-for="(item,index) in this.$router.options.routes" v-if="!item.hidden && (item.name !== '用户管理' || isAdmin)">
            <el-submenu :index="index+''" v-if="item.children.length>1" :key="index">
              <template slot="title">
                <i :class="item.iconCls"></i>
                <span>{{item.name}}</span>
              </template>
              <el-menu-item v-for="child in item.children" v-if="!child.hidden" :index="child.path" :key="child.path">
                {{child.name}}
              </el-menu-item>
            </el-submenu>
            <template v-else>
              <el-menu-item :index="item.children[0].path">
                <i :class="item.children[0].iconCls"></i>
                <span slot="title">{{item.children[0].name}}</span>
              </el-menu-item>
            </template>
          </template>
        </el-menu>
      </el-aside>
      <el-container>
        <el-main>
          <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-text="this.$router.currentRoute.name"></el-breadcrumb-item>
          </el-breadcrumb>
          <keep-alive>
            <router-view v-if="this.$route.meta.keepAlive"></router-view>
          </keep-alive>
          <router-view v-if="!this.$route.meta.keepAlive"></router-view>
        </el-main>
      </el-container>
    </el-container>
  </el-container>
</template>
<script>
  import {getRequest} from '../utils/api'
  export default{
    methods: {
      handleLogout(){
        var _this = this;
        this.$confirm('注销登录吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(function () {
          getRequest("/logout")
          _this.currentUserName = '游客';
          _this.$router.replace({path: '/'});
        }, function () {
          //取消
        })
      }
    },
    mounted: function () {
      // this.$alert('为了确保所有的小伙伴都能看到完整的数据演示，数据库只开放了查询权限和部分字段的更新权限，其他权限都不具备，完整权限的演示需要大家在自己本地部署后，换一个正常的数据库用户后即可查看，这点请大家悉知!', '友情提示', {
      //   confirmButtonText: '确定',
      //   callback: action => {
      //   }
      // });
      var _this = this;
      getRequest("/currentUserName").then(function (msg) {
        _this.currentUserName = msg.data;
      }, function (msg) {
        _this.currentUserName = '游客';
      });

      // 检查用户是否为管理员
      getRequest("/isAdmin").then(function (msg) {
        _this.isAdmin = msg.data;
      }, function (msg) {
        _this.isAdmin = false;
      });
    },
    data(){
      return {
        currentUserName: '',
        isAdmin: false
      }
    }
  }
</script>
<style>
  .home_container {
    height: 100%;
    position: absolute;
    top: 0px;
    left: 0px;
    width: 100%;
  }

  .home_container .el-header {
    background-color: #20a0ff;
    color: #333;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 80px !important;
    min-height: 80px !important;
    max-height: 80px !important;
  }

  .el-aside {
    background-color: #ECECEC;
  }

  .el-main {
    background-color: #fff;
    color: #000;
    text-align: center;
  }

  .home_title {
    color: #fff;
    font-size: 22px;
    display: inline;
  }

  .home_username {
    color: #fff;
    font-size: 16px;
    font-weight: 500;
    margin-right: 16px;
  }

  .logout_button {
    color: #fff !important;
    font-size: 14px;
    padding: 10px 20px !important;
    border: 1px solid #d73527 !important;
    border-radius: 4px !important;
    transition: all 0.3s ease !important;
    background-color: #d73527 !important;
    text-align: center !important;
    display: inline-flex !important;
    align-items: center !important;
    justify-content: center !important;
    min-height: 36px !important;
  }

  .logout_button:hover {
    background-color: #c22e21 !important;
    border-color: #c22e21 !important;
    transform: translateY(-1px) !important;
  }

  .home_userinfoContainer {
    display: flex;
    align-items: center;
    margin-right: 20px;
  }
</style>
