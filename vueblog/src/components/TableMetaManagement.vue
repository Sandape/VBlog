<template>
  <div class="table-meta-management">
    <!-- 项目选择阶段 -->
    <div v-if="!selectedProject" class="project-selection">
      <div class="selection-header">
        <h2>选择项目</h2>
        <p>请选择一个项目来管理其表元数据</p>
      </div>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-input
            placeholder="搜索项目名称..."
            v-model="searchKeyword"
            @input="filterProjects"
            prefix-icon="el-icon-search"
            style="margin-bottom: 20px;">
          </el-input>
        </el-col>
      </el-row>

      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="project in filteredProjects" :key="project.id">
          <el-card
            class="project-card"
            :body-style="{ padding: '20px' }"
            @click.native="selectProject(project)"
            shadow="hover">
            <div class="project-info">
              <h3>{{ project.projectName }}</h3>
              <p class="project-code">编码: {{ project.projectCode }}</p>
              <p class="project-owner">拥有者: {{ project.ownerNickname }}</p>
              <div class="project-role">
                <el-tag :type="project.userRole === 1 ? 'danger' : 'primary'" size="mini">
                  {{ project.userRole === 1 ? '拥有者' : '成员' }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div v-if="filteredProjects.length === 0 && !loading" class="no-projects">
        <el-empty description="没有找到项目">
          <el-button type="primary" @click="$router.push('/createProject')">创建项目</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 表元管理阶段 -->
    <div v-if="selectedProject" class="table-meta-management-content">
      <div class="management-header">
        <el-button @click="backToSelection" icon="el-icon-arrow-left" type="text">返回项目选择</el-button>
        <h2>{{ selectedProject.projectName }} - 表元管理</h2>
        <div class="project-info">
          <el-tag :type="selectedProject.userRole === 1 ? 'danger' : 'primary'" size="small">
            {{ selectedProject.userRole === 1 ? '拥有者' : '成员' }}
          </el-tag>
        </div>
      </div>

      <el-row :gutter="20">
        <el-col :span="18">
          <el-card class="table-meta-card">
            <div slot="header" class="card-header">
              <span>表元列表</span>
              <el-button type="primary" @click="showAddDialog" icon="el-icon-plus">添加表元</el-button>
            </div>

            <el-table :data="tableMetaList" style="width: 100%" v-loading="tableLoading">
              <el-table-column prop="tableName" label="表名" width="200">
                <template slot-scope="scope">
                  <el-tag type="info">{{ scope.row.tableName }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="字段信息" width="300">
                <template slot-scope="scope">
                  <div v-if="scope.row.colum && scope.row.colum.length > 0">
                    <div v-for="(column, index) in scope.row.colum.slice(0, 3)" :key="index" class="column-item">
                      <el-tag size="mini" type="success">{{ column.columName }}</el-tag>
                      <span class="column-type">{{ column.columType }}</span>
                      <span class="column-desc" v-if="column.columdesc">{{ column.columdesc }}</span>
                    </div>
                    <div v-if="scope.row.colum.length > 3" class="more-columns">
                      <el-tag size="mini" type="info">... 还有{{ scope.row.colum.length - 3 }}个字段</el-tag>
                    </div>
                  </div>
                  <div v-else class="no-columns">
                    <el-tag size="mini" type="warning">暂无字段信息</el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="sqlContent" label="SQL语句">
                <template slot-scope="scope">
                  <div class="sql-content">
                    <pre>{{ scope.row.sqlContent }}</pre>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template slot-scope="scope">
                  <el-button size="mini" @click="showEditDialog(scope.row)" icon="el-icon-edit">编辑</el-button>
                  <el-button
                    v-if="selectedProject.userRole === 1"
                    size="mini"
                    type="danger"
                    @click="deleteTableMeta(scope.row.tableName)"
                    icon="el-icon-delete">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="info-card">
            <div slot="header">
              <span>项目信息</span>
            </div>
            <div class="project-details">
              <p><strong>项目名称:</strong> {{ selectedProject.projectName }}</p>
              <p><strong>项目编码:</strong> {{ selectedProject.projectCode }}</p>
              <p><strong>拥有者:</strong> {{ selectedProject.ownerNickname }}</p>
              <p><strong>成员数量:</strong> {{ selectedProject.memberCount }}</p>
              <p><strong>表元数量:</strong> {{ tableMetaList.length }}</p>
            </div>
          </el-card>

          <el-card class="help-card" style="margin-top: 20px;">
            <div slot="header">
              <span>使用说明</span>
            </div>
            <div class="help-content">
              <p><i class="el-icon-info"></i> 支持DDL和DQL语句</p>
              <p><i class="el-icon-info"></i> 系统会自动解析表名</p>
              <p><i class="el-icon-warning"></i> 仅项目拥有者可删除表元</p>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 添加/编辑表元对话框 -->
      <el-dialog
        :title="dialogType === 'add' ? '添加表元' : '编辑表元'"
        :visible.sync="dialogVisible"
        width="70%"
        :close-on-click-modal="false">
        <el-form :model="tableMetaForm" :rules="formRules" ref="tableMetaForm">
          <el-form-item label="SQL语句" prop="sql" :label-width="formLabelWidth">
            <el-input
              type="textarea"
              :rows="8"
              v-model="tableMetaForm.sql"
              placeholder="请输入SQL语句，支持DDL和DQL语句">
            </el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTableMeta" :loading="submitLoading">
            {{ dialogType === 'add' ? '添加' : '保存' }}
          </el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getRequest, addSqlTable, getProjectSqlTables, updateSqlTable, deleteSqlTable } from '../utils/api'

export default {
  name: 'TableMetaManagement',
  data() {
    return {
      loading: false,
      tableLoading: false,
      submitLoading: false,
      searchKeyword: '',
      selectedProject: null,
      allProjects: [],
      filteredProjects: [],
      tableMetaList: [],
      dialogVisible: false,
      dialogType: 'add', // 'add' 或 'edit'
      currentTableName: '',
      tableMetaForm: {
        sql: ''
      },
      formRules: {
        sql: [
          { required: true, message: '请输入SQL语句', trigger: 'blur' }
        ]
      },
      formLabelWidth: '80px'
    }
  },
  mounted() {
    this.loadProjects()
  },
  methods: {
    loadProjects() {
      this.loading = true
      getRequest('/project/list').then(resp => {
        if (resp && resp.status === 200 && resp.data && resp.data.status === 'success') {
          this.allProjects = resp.data.obj || []
          this.filteredProjects = [...this.allProjects]

          // 检查路由参数，如果有projectId则直接选择对应项目
          if (this.$route.query.projectId) {
            const projectId = parseInt(this.$route.query.projectId)
            const project = this.allProjects.find(p => p.id === projectId)
            if (project) {
              this.selectProject(project)
            }
          }
        } else {
          this.allProjects = []
          this.filteredProjects = []
          if (resp.data && resp.data.msg) {
            this.$message.error(resp.data.msg)
          }
        }
      }).catch(err => {
        console.error('加载项目列表失败:', err)
        this.$message.error('加载项目列表失败')
        this.allProjects = []
        this.filteredProjects = []
      }).finally(() => {
        this.loading = false
      })
    },

    filterProjects() {
      if (!this.searchKeyword.trim()) {
        this.filteredProjects = [...this.allProjects]
      } else {
        const keyword = this.searchKeyword.toLowerCase()
        this.filteredProjects = this.allProjects.filter(project =>
          project.projectName.toLowerCase().includes(keyword)
        )
      }
    },

    selectProject(project) {
      this.selectedProject = project
      this.loadTableMeta()
    },

    backToSelection() {
      this.selectedProject = null
      this.tableMetaList = []
    },

    loadTableMeta() {
      this.tableLoading = true
      getProjectSqlTables(this.selectedProject.id).then(resp => {
        if (resp && resp.status === 200 && resp.data && resp.data.status === 'success') {
          // 将对象转换为数组格式
          const tableMetaMap = resp.data.obj || {}
          this.tableMetaList = Object.keys(tableMetaMap).map(tableName => {
            const tableMeta = tableMetaMap[tableName]
            return {
              tableName: tableName,
              name: tableMeta.name,
              colum: tableMeta.colum,
              entityPath: tableMeta.entityPath,
              originalSql: tableMeta.originalSql,
              sqlContent: tableMeta.originalSql // 保持向后兼容
            }
          })
        } else {
          this.tableMetaList = []
          if (resp.data && resp.data.msg) {
            this.$message.error(resp.data.msg)
          }
        }
      }).catch(err => {
        console.error('加载表元数据失败:', err)
        this.$message.error('加载表元数据失败')
        this.tableMetaList = []
      }).finally(() => {
        this.tableLoading = false
      })
    },

    showAddDialog() {
      this.dialogType = 'add'
      this.currentTableName = ''
      this.tableMetaForm.sql = ''
      this.dialogVisible = true
    },

    showEditDialog(row) {
      this.dialogType = 'edit'
      this.currentTableName = row.tableName
      this.tableMetaForm.sql = row.originalSql || row.sqlContent
      this.dialogVisible = true
    },

    submitTableMeta() {
      this.$refs.tableMetaForm.validate(valid => {
        if (!valid) {
          return false
        }

        this.submitLoading = true

        if (this.dialogType === 'add') {
          // 添加表元
          addSqlTable(this.selectedProject.id, this.tableMetaForm.sql).then(resp => {
            if (resp && resp.status === 200 && resp.data && resp.data.status === 'success') {
              this.$message.success('表元添加成功')
              this.dialogVisible = false
              this.loadTableMeta()
            } else {
              this.$message.error((resp.data && resp.data.msg) || '添加失败')
            }
          }).catch(err => {
            console.error('添加表元失败:', err)
            this.$message.error('添加表元失败')
          }).finally(() => {
            this.submitLoading = false
          })
        } else {
          // 编辑表元
          updateSqlTable(this.selectedProject.id, this.currentTableName, this.tableMetaForm.sql).then(resp => {
            if (resp && resp.status === 200 && resp.data && resp.data.status === 'success') {
              this.$message.success('表元编辑成功')
              this.dialogVisible = false
              this.loadTableMeta()
            } else {
              this.$message.error((resp.data && resp.data.msg) || '编辑失败')
            }
          }).catch(err => {
            console.error('编辑表元失败:', err)
            this.$message.error('编辑表元失败')
          }).finally(() => {
            this.submitLoading = false
          })
        }
      })
    },

    deleteTableMeta(tableName) {
      this.$confirm(`确定要删除表元 "${tableName}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteSqlTable(this.selectedProject.id, tableName).then(resp => {
          if (resp && resp.status === 200 && resp.data && resp.data.status === 'success') {
            this.$message.success('表元删除成功')
            this.loadTableMeta()
          } else {
            this.$message.error((resp.data && resp.data.msg) || '删除失败')
          }
        }).catch(err => {
          console.error('删除表元失败:', err)
          this.$message.error('删除表元失败')
        })
      }).catch(() => {
        // 用户取消删除
      })
    }
  }
}
</script>

<style scoped>
.table-meta-management {
  padding: 20px;
}

.selection-header {
  text-align: center;
  margin-bottom: 30px;
}

.selection-header h2 {
  color: #409eff;
  margin-bottom: 10px;
}

.project-card {
  cursor: pointer;
  transition: all 0.3s;
}

.project-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.project-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.project-code, .project-owner {
  margin: 5px 0;
  color: #909399;
  font-size: 14px;
}

.management-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.management-header h2 {
  flex: 1;
  margin: 0 20px;
  color: #409eff;
}

.table-meta-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.column-item {
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.column-type {
  color: #409eff;
  font-size: 12px;
  font-weight: 500;
}

.column-desc {
  color: #909399;
  font-size: 11px;
  margin-left: 4px;
}

.more-columns, .no-columns {
  margin-top: 4px;
}

.sql-content pre {
  background-color: #f6f8fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #24292e;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  max-height: 150px;
  overflow-y: auto;
}

.info-card, .help-card {
  margin-bottom: 20px;
}

.project-details p {
  margin: 8px 0;
  font-size: 14px;
}

.help-content p {
  margin: 8px 0;
  font-size: 13px;
  color: #606266;
}

.help-content i {
  margin-right: 8px;
  color: #e6a23c;
}

.no-projects {
  text-align: center;
  margin-top: 50px;
}
</style>
