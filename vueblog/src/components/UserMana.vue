<template>
  <div>
    <div class="user-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="refreshUsers">刷新</el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-input
        placeholder="请输入用户名进行搜索..."
        prefix-icon="el-icon-search"
        v-model="keywords"
        size="small"
        style="width: 300px; margin-right: 10px"
        @keyup.enter="searchClick">
      </el-input>
      <el-button type="primary" icon="el-icon-search" size="small" @click="searchClick">搜索</el-button>
    </div>

    <!-- 用户列表表格 -->
    <el-table :data="users" style="width: 100%" border stripe v-loading="loading">
      <el-table-column label="用户名" min-width="120">
        <template slot-scope="scope">
          <div class="username-cell">
            <i class="el-icon-user user-icon"></i>
            <span class="username-text">{{ scope.row.username }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>

      <el-table-column prop="email" label="邮箱" min-width="200">
        <template slot-scope="scope">
          <div class="email-cell">
            <i class="el-icon-message email-icon"></i>
            <span>{{ scope.row.email }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="regTime" label="注册时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.regTime) }}
        </template>
      </el-table-column>

      <el-table-column label="用户状态" width="120" align="center">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.enabled"
            active-color="#13ce66"
            inactive-color="#ff4949"
            @change="enabledChange(scope.row.enabled, scope.row.id, scope.$index)">
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column label="用户角色" min-width="200">
        <template slot-scope="scope">
          <div class="roles-cell">
            <el-tag
              v-for="role in scope.row.roles"
              :key="role.id"
              size="mini"
              style="margin-right: 5px; margin-bottom: 2px"
              :type="role.id === 1 ? 'danger' : 'success'">
              {{ role.name }}
            </el-tag>
            <el-popover
              placement="bottom"
              title="修改角色"
              width="250"
              :ref="`rolePopover-${scope.$index}`"
              @hide="saveRoles(scope.row.id, scope.$index)"
              trigger="click"
              v-loading="eploading[scope.$index]">
              <div style="max-height: 200px; overflow-y: auto;">
                <el-checkbox-group v-model="roles" size="mini">
                  <el-checkbox
                    v-for="role in allRoles"
                    :key="role.id"
                    :label="role.id"
                    style="display: block; margin-bottom: 8px;">
                    {{ role.name }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>
              <div style="text-align: right; margin-top: 10px;">
                <el-button size="mini" @click="closeRolePopover(scope.$index)">取消</el-button>
                <el-button type="primary" size="mini" @click="confirmRoleChange(scope.row.id, scope.$index)">确定</el-button>
              </div>
              <el-button
                type="text"
                icon="el-icon-edit"
                size="mini"
                slot="reference"
                @click="showRole(scope.row.roles, scope.row.id, scope.$index)"
                style="margin-left: 5px;">
              </el-button>
            </el-popover>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100" align="center">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="danger"
            icon="el-icon-delete"
            @click="deleteUser(scope.row.id)"
            style="margin-left: 5px;">
          </el-button>
        </template>
      </el-table-column>
    </el-table>

  </div>
</template>
<script>
  import {getRequest} from '../utils/api'
  import {putRequest} from '../utils/api'
  import {deleteRequest} from '../utils/api'
  export default{
    mounted: function () {
      this.loadUserList();
    },
    methods: {
      saveRoles(id, index){
        // 这个方法现在只用于初始化角色选择，不直接保存
        // 实际保存逻辑移到confirmRoleChange方法中
      },
      showRole(aRoles, id, index){
        this.cpRoles = aRoles;
        this.roles = [];
        this.loadRoles(index);
        for (var i = 0; i < aRoles.length; i++) {
          this.roles.push(aRoles[i].id);
        }
      },
      deleteUser(id){
        var _this = this;
        this.$confirm('删除该用户, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          _this.loading = true;
          deleteRequest("/admin/user/" + id).then(resp=> {
            if (resp.status == 200 && resp.data.status == 'success') {
              _this.$message({type: 'success', message: '删除成功!'})
              _this.loadUserList();
              return;
            }
            _this.loading = false;
            _this.$message({type: 'error', message: '删除失败!'})
          }, resp=> {
            _this.loading = false;
            _this.$message({type: 'error', message: '删除失败!'})
          });
        }).catch(() => {
          _this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
      },
      enabledChange(enabled, id, index){
        var _this = this;
        _this.eploading.splice(index, 1, true)
        putRequest("/admin/user/enabled", {enabled: enabled, uid: id}).then(resp=> {
          if (resp.status != 200) {
            _this.$message({type: 'error', message: '更新失败!'})
            _this.loadOneUserById(id, index);
            return;
          }
          _this.eploading.splice(index, 1, false)
          _this.$message({type: 'success', message: '更新成功!'})
        }, resp=> {
          _this.eploading.splice(index, 1, false)
          _this.$message({type: 'error', message: '更新失败!'})
          _this.loadOneUserById(id, index);
        });
      },
      loadRoles(index){
        var _this = this;
        _this.eploading.splice(index, 1, true)
        getRequest("/admin/roles").then(resp=> {
          _this.eploading.splice(index, 1, false)
          if (resp.status == 200) {
            _this.allRoles = resp.data;
          } else {
            _this.$message({type: 'error', message: '数据加载失败!'});
          }
        }, resp=> {
          _this.eploading.splice(index, 1, false)
          if (resp.response.status == 403) {
            var data = resp.response.data;
            _this.$message({type: 'error', message: data});
          }
        });
      },
      loadOneUserById(id, index){
        var _this = this;
        getRequest("/admin/user/" + id).then(resp=> {
          _this.eploading.splice(index, 1, false)
          if (resp.status == 200) {
            _this.users.splice(index, 1, resp.data);
          } else {
            _this.$message({type: 'error', message: '数据加载失败!'});
          }
        }, resp=> {
          _this.eploading.splice(index, 1, false)
          if (resp.response.status == 403) {
            var data = resp.response.data;
            _this.$message({type: 'error', message: data});
          }
        });
      },
      loadUserList(){
        var _this = this;
        getRequest("/admin/user?nickname="+this.keywords).then(resp=> {
          _this.loading = false;
          if (resp.status == 200) {
            console.log('用户数据:', resp.data);
            _this.users = resp.data;
            _this.total = resp.data.length;
          } else {
            _this.$message({type: 'error', message: '数据加载失败!'});
          }
        }, resp=> {
          _this.loading = false;
          if (resp.response.status == 403) {
            var data = resp.response.data;
            _this.$message({type: 'error', message: data});
          }
        });
      },
      searchClick(){
        this.loading = true;
        this.loadUserList();
      },
      // 刷新用户列表
      refreshUsers(){
        this.keywords = '';
        this.loadUserList();
      },
      // 关闭角色选择弹窗
      closeRolePopover(index){
        this.$refs[`rolePopover-${index}`][0].doClose();
      },
      // 确认角色更改
      confirmRoleChange(id, index){
        var selRoles = this.roles;
        if (this.cpRoles.length == selRoles.length) {
          for (var i = 0; i < this.cpRoles.length; i++) {
            for (var j = 0; j < selRoles.length; j++) {
              if (this.cpRoles[i].id == selRoles[j]) {
                selRoles.splice(j, 1);
                break;
              }
            }
          }
          if (selRoles.length == 0) {
            this.closeRolePopover(index);
            return;
          }
        }
        var _this = this;
        _this.eploading.splice(index, 1, true);
        putRequest("/admin/user/role", {rids: this.roles, id: id}).then(resp=> {
          if (resp.status == 200 && resp.data.status == 'success') {
            _this.$message({type: resp.data.status, message: resp.data.msg});
            _this.closeRolePopover(index);
            _this.loadOneUserById(id, index);
          } else {
            _this.eploading.splice(index, 1, false);
            _this.$message({type: 'error', message: '更新失败!'});
          }
        }, resp=> {
          _this.eploading.splice(index, 1, false);
          if (resp.response.status == 403) {
            var data = resp.response.data;
            _this.$message({type: 'error', message: data});
          }
        });
      }
    },
    data(){
      return {
        loading: false,
        eploading: [],
        keywords: '',
        users: [],
        allRoles: [],
        roles: [],
        cpRoles: [],
        total: 0
      }
    }
  }
</script>

<style scoped>
.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-section {
  margin-bottom: 20px;
}

/* 自定义表格样式 */
.el-table {
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
  border-bottom: 1px solid #e4e7ed;
  padding: 12px 0;
}

.el-table td {
  border-bottom: 1px solid #f0f0f0;
  padding: 10px 0;
  color: #606266;
}

.el-table--border {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.el-table--border th,
.el-table--border td {
  border-right: 1px solid #e4e7ed;
}

.el-table--border th:last-child,
.el-table--border td:last-child {
  border-right: none;
}

/* 行悬停效果 */
.el-table tbody tr:hover {
  background-color: #f5f7fa;
  cursor: pointer;
}


/* 用户名列样式 */
.username-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-icon {
  color: #20a0ff;
  font-size: 16px;
}

.username-text {
  color: #303133;
  font-weight: 500;
  transition: color 0.2s ease;
}

.username-text:hover {
  color: #20a0ff;
}

/* 邮箱列样式 */
.email-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.email-icon {
  color: #67c23a;
  font-size: 14px;
}

/* 角色列样式 */
.roles-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  align-items: center;
}


/* 表格斑马纹样式增强 */
.el-table--striped .el-table__body tr.el-table__row--striped td {
  background-color: #fafafa;
}

.el-table--striped .el-table__body tr.el-table__row--striped:hover td {
  background-color: #f0f8ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .search-section {
    text-align: center;
  }

  .el-table th,
  .el-table td {
    padding: 8px 0;
  }

  .username-cell {
    gap: 6px;
  }

  .user-icon {
    font-size: 14px;
  }

  .username-text {
    font-size: 14px;
  }

}
</style>
