<template>
  <div>
    <div class="project-header">
      <h2>我的项目</h2>
      <el-button type="primary" @click="refreshProjects">刷新</el-button>
    </div>
    
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="参与的项目" name="joined">
        <el-table :data="joinedProjects" style="width: 100%" v-loading="loading">
          <el-table-column prop="projectName" label="项目名称" width="200"></el-table-column>
          <el-table-column prop="projectCode" label="项目编码" width="120"></el-table-column>
          <el-table-column prop="ownerNickname" label="项目拥有者" width="120"></el-table-column>
          <el-table-column prop="memberCount" label="成员数量" width="100"></el-table-column>
          <el-table-column label="我的角色" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.userRole === 1 ? 'danger' : 'primary'">
                {{ scope.row.userRole === 1 ? '拥有者' : '成员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250">
            <template slot-scope="scope">
              <el-button size="mini" @click="viewProject(scope.row)">查看详情</el-button>
              <el-button size="mini" type="success" @click="manageTableMeta(scope.row)">表元管理</el-button>
              <el-button
                v-if="scope.row.userRole === 2"
                size="mini"
                type="warning"
                @click="leaveProject(scope.row)">
                退出项目
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="我拥有的项目" name="owned">
        <el-table :data="ownedProjects" style="width: 100%" v-loading="loading">
          <el-table-column prop="projectName" label="项目名称" width="200"></el-table-column>
          <el-table-column prop="projectCode" label="项目编码" width="120">
            <template slot-scope="scope">
              <el-tag type="success">{{ scope.row.projectCode }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="memberCount" label="成员数量" width="100"></el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template slot-scope="scope">
              <el-button size="mini" @click="viewProject(scope.row)">查看详情</el-button>
              <el-button size="mini" type="success" @click="manageTableMeta(scope.row)">表元管理</el-button>
              <el-button size="mini" type="primary" @click="editProject(scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="deleteProject(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 项目详情对话框 -->
    <el-dialog title="项目详情" :visible.sync="detailDialogVisible" width="70%">
      <div v-if="currentProject">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="120px">
              <el-form-item label="项目名称:">
                <span>{{ currentProject.projectName }}</span>
              </el-form-item>
              <el-form-item label="项目编码:">
                <el-tag type="success" size="medium">{{ currentProject.projectCode }}</el-tag>
                <el-button size="mini" @click="copyProjectCode" style="margin-left: 10px;">复制编码</el-button>
              </el-form-item>
              <el-form-item label="项目拥有者:">
                <span>{{ currentProject.ownerNickname }}</span>
              </el-form-item>
              <el-form-item label="成员数量:">
                <span>{{ currentProject.memberCount }}</span>
              </el-form-item>
              <el-form-item label="创建时间:">
                <span>{{ formatDate(currentProject.createTime) }}</span>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="120px">
              <el-form-item label="示例Mapper:">
                <span>{{ currentProject.exampleMapperPath || '未设置' }}</span>
              </el-form-item>
              <el-form-item label="示例实体类:">
                <span>{{ currentProject.exampleEntityPath || '未设置' }}</span>
              </el-form-item>
              <el-form-item label="示例接口:">
                <span>{{ currentProject.exampleInterfacePath || '未设置' }}</span>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <el-divider>开发规范</el-divider>
        <div class="spec-content">
          <pre v-if="currentProject.developmentSpec">{{ currentProject.developmentSpec }}</pre>
          <span v-else style="color: #999;">暂无开发规范</span>
        </div>

        <!-- 仅项目拥有者可见的敏感信息 -->
        <div v-if="currentProject.userRole === 1">
          <el-divider>配置信息（仅拥有者可见）</el-divider>
          <el-form label-width="120px">
            <el-form-item label="API Key:">
              <span>{{ currentProject.apiKey || '未设置' }}</span>
            </el-form-item>
            <el-form-item label="API URL:">
              <span>{{ currentProject.apiUrl || '未设置' }}</span>
            </el-form-item>
            <el-form-item label="Model Name:">
              <span>{{ currentProject.modelName || '未设置' }}</span>
            </el-form-item>
          </el-form>
        </div>

        <el-divider>项目成员</el-divider>
        <el-table :data="projectMembers" style="width: 100%">
          <el-table-column prop="nickname" label="用户昵称" width="150"></el-table-column>
          <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
          <el-table-column label="角色" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.role === 1 ? 'danger' : 'primary'">
                {{ scope.row.role === 1 ? '拥有者' : '成员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="joinTime" label="加入时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.joinTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" v-if="currentProject.userRole === 1">
            <template slot-scope="scope">
              <el-button 
                v-if="scope.row.role === 2" 
                size="mini" 
                type="danger" 
                @click="removeMember(scope.row)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRequest, postRequest, deleteRequest } from '../utils/api'

export default {
  name: 'ProjectList',
  data() {
    return {
      activeTab: 'joined',
      joinedProjects: [],
      ownedProjects: [],
      loading: false,
      detailDialogVisible: false,
      currentProject: null,
      projectMembers: []
    }
  },
  mounted() {
    this.loadProjects()
  },
  methods: {
    handleTabClick(tab) {
      if (tab.name === 'joined' && this.joinedProjects.length === 0) {
        this.loadJoinedProjects()
      } else if (tab.name === 'owned' && this.ownedProjects.length === 0) {
        this.loadOwnedProjects()
      }
    },
    
    loadProjects() {
      this.loadJoinedProjects()
    },
    
    loadJoinedProjects() {
      this.loading = true
      getRequest('/project/my-projects').then(resp => {
        this.loading = false
        if (resp.data.status === 'success') {
          this.joinedProjects = resp.data.obj || []
        } else {
          this.$message.error(resp.data.msg)
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('加载项目列表失败')
      })
    },
    
    loadOwnedProjects() {
      this.loading = true
      getRequest('/project/owned-projects').then(resp => {
        this.loading = false
        if (resp.data.status === 'success') {
          this.ownedProjects = resp.data.obj || []
        } else {
          this.$message.error(resp.data.msg)
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('加载项目列表失败')
      })
    },
    
    refreshProjects() {
      if (this.activeTab === 'joined') {
        this.loadJoinedProjects()
      } else {
        this.loadOwnedProjects()
      }
    },
    
    viewProject(project) {
      this.currentProject = null
      this.projectMembers = []
      
      // 获取项目详情
      getRequest(`/project/detail/${project.id}`).then(resp => {
        if (resp.data.status === 'success') {
          this.currentProject = resp.data.obj
          this.detailDialogVisible = true
          this.loadProjectMembers(project.id)
        } else {
          this.$message.error(resp.data.msg)
        }
      }).catch(() => {
        this.$message.error('获取项目详情失败')
      })
    },
    
    loadProjectMembers(projectId) {
      getRequest(`/project/members/${projectId}`).then(resp => {
        if (resp.data.status === 'success') {
          this.projectMembers = resp.data.obj || []
        }
      }).catch(() => {
        console.log('获取项目成员失败')
      })
    },
    
    editProject(project) {
      this.$router.push({
        path: '/editProject',
        query: { id: project.id }
      })
    },
    
    deleteProject(project) {
      this.$confirm('确定要删除这个项目吗？删除后无法恢复！', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteRequest(`/project/delete/${project.id}`).then(resp => {
          if (resp.data.status === 'success') {
            this.$message.success('项目删除成功')
            this.loadOwnedProjects()
          } else {
            this.$message.error(resp.data.msg)
          }
        }).catch(() => {
          this.$message.error('删除项目失败')
        })
      })
    },
    
    leaveProject(project) {
      this.$confirm('确定要退出这个项目吗？', '确认退出', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        postRequest(`/project/leave/${project.id}`).then(resp => {
          if (resp.data.status === 'success') {
            this.$message.success('退出项目成功')
            this.loadJoinedProjects()
          } else {
            this.$message.error(resp.data.msg)
          }
        }).catch(() => {
          this.$message.error('退出项目失败')
        })
      })
    },
    
    removeMember(member) {
      this.$confirm(`确定要移除成员 ${member.nickname} 吗？`, '确认移除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteRequest(`/project/remove-member?projectId=${this.currentProject.id}&memberId=${member.userId}`).then(resp => {
          if (resp.data.status === 'success') {
            this.$message.success('移除成员成功')
            this.loadProjectMembers(this.currentProject.id)
          } else {
            this.$message.error(resp.data.msg)
          }
        }).catch(() => {
          this.$message.error('移除成员失败')
        })
      })
    },
    
    copyProjectCode() {
      const code = this.currentProject.projectCode
      if (navigator.clipboard) {
        navigator.clipboard.writeText(code).then(() => {
          this.$message.success('项目编码已复制到剪贴板')
        })
      } else {
        // 兼容旧浏览器
        const textArea = document.createElement('textarea')
        textArea.value = code
        document.body.appendChild(textArea)
        textArea.select()
        document.execCommand('copy')
        document.body.removeChild(textArea)
        this.$message.success('项目编码已复制到剪贴板')
      }
    },
    
    manageTableMeta(project) {
      // 跳转到表元管理页面，传递项目信息
      this.$router.push({
        path: '/tableMetaManagement',
        query: {
          projectId: project.id,
          projectName: project.projectName
        }
      })
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.spec-content {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.spec-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
