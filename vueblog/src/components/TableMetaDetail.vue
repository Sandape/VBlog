<template>
  <div class="table-meta-detail">
    <!-- 返回按钮 -->
    <div class="back-header">
      <el-button @click="goBack" icon="el-icon-arrow-left" type="text">返回表元列表</el-button>
      <h2>{{ tableName }} - 表元详情</h2>
    </div>

    <!-- 表元信息卡片 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="detail-card" v-loading="loading">
          <div slot="header" class="card-header">
            <span>基本信息</span>
            <el-button type="primary" @click="showEditDialog" icon="el-icon-edit">编辑</el-button>
          </div>

          <div class="basic-info">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="info-item">
                  <label>表名：</label>
                  <span>{{ tableMetaData.name || '暂无' }}</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-item">
                  <label>Entity路径：</label>
                  <el-tag v-if="tableMetaData.entityPath" type="success">{{ tableMetaData.entityPath }}</el-tag>
                  <el-tag v-else type="warning">未配置</el-tag>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-item">
                  <label>AI解析状态：</label>
                  <el-tag :type="getParseStatusType(tableMetaData.parseStatus)" size="small">
                    <i :class="getParseStatusIcon(tableMetaData.parseStatus)"></i>
                    {{ getParseStatusText(tableMetaData.parseStatus) }}
                  </el-tag>
                </div>
              </el-col>
            </el-row>

            <el-row v-if="tableMetaData.parseError" :gutter="20" style="margin-top: 20px;">
              <el-col :span="24">
                <div class="info-item">
                  <label>解析错误：</label>
                  <span class="error-text">{{ tableMetaData.parseError }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字段信息卡片 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="detail-card">
          <div slot="header">
            <span>字段信息</span>
          </div>

          <el-table :data="tableMetaData.colum || []" style="width: 100%">
            <el-table-column prop="columName" label="字段名" width="200">
              <template slot-scope="scope">
                <el-tag type="primary" size="small">{{ scope.row.columName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="columType" label="字段类型" width="150">
              <template slot-scope="scope">
                <el-tag type="info" size="small">{{ scope.row.columType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="columdesc" label="字段描述">
              <template slot-scope="scope">
                <span>{{ scope.row.columdesc || '暂无描述' }}</span>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="!tableMetaData.colum || tableMetaData.colum.length === 0" class="no-data">
            <el-empty description="暂无字段信息"></el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- SQL语句卡片 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="detail-card">
          <div slot="header">
            <span>原始SQL语句</span>
          </div>

          <div class="sql-content">
            <pre>{{ tableMetaData.originalSql || '暂无SQL语句' }}</pre>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑对话框 -->
    <el-dialog
      title="编辑表元"
      :visible.sync="editDialogVisible"
      width="70%"
      :close-on-click-modal="false">
      <el-form :model="editForm" :rules="formRules" ref="editForm">
        <el-form-item label="SQL语句" prop="sql" :label-width="formLabelWidth">
          <el-input
            type="textarea"
            :rows="8"
            v-model="editForm.sql"
            placeholder="请输入SQL语句，支持DDL和DQL语句">
          </el-input>
        </el-form-item>
        <el-form-item label="Entity路径" :label-width="formLabelWidth">
          <el-input
            v-model="editForm.entityPath"
            placeholder="请输入Entity类路径（可选）">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitLoading">
          保存
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProjectSqlTables, updateSqlTable } from '../utils/api'

export default {
  name: 'TableMetaDetail',
  data() {
    return {
      loading: false,
      editDialogVisible: false,
      submitLoading: false,
      tableName: '',
      projectId: null,
      tableMetaData: {},
      editForm: {
        sql: '',
        entityPath: ''
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
    this.tableName = this.$route.params.tableName
    this.projectId = this.$route.params.projectId
    this.loadTableMetaDetail()
  },
  methods: {
    goBack() {
      // 返回到表元管理页面，传递项目ID参数
      this.$router.push({
        name: '表元管理',
        query: {
          projectId: this.projectId
        }
      })
    },

    loadTableMetaDetail() {
      this.loading = true
      getProjectSqlTables(this.projectId).then(resp => {
        if (resp && resp.status === 200 && resp.data && resp.data.status === 'success') {
          const tableMetaMap = resp.data.obj || {}
          this.tableMetaData = tableMetaMap[this.tableName] || {}
        } else {
          this.$message.error((resp.data && resp.data.msg) || '加载失败')
        }
      }).catch(err => {
        console.error('加载表元详情失败:', err)
        this.$message.error('加载表元详情失败')
      }).finally(() => {
        this.loading = false
      })
    },

    showEditDialog() {
      this.editForm.sql = this.tableMetaData.originalSql || ''
      this.editForm.entityPath = this.tableMetaData.entityPath || ''
      this.editDialogVisible = true
    },

    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (!valid) {
          return false
        }

        this.submitLoading = true

        updateSqlTable(this.projectId, this.tableName, this.editForm.sql, this.editForm.entityPath).then(resp => {
          if (resp && resp.status === 200 && resp.data && resp.data.status === 'success') {
            this.$message.success('表元编辑成功')
            this.editDialogVisible = false
            this.loadTableMetaDetail()
          } else {
            this.$message.error((resp.data && resp.data.msg) || '编辑失败')
          }
        }).catch(err => {
          console.error('编辑表元失败:', err)
          this.$message.error('编辑表元失败')
        }).finally(() => {
          this.submitLoading = false
        })
      })
    },

    getParseStatusType(status) {
      switch (status) {
        case 'COMPLETED':
          return 'success'
        case 'FAILED':
          return 'danger'
        case 'PARSING':
          return 'warning'
        default:
          return 'info'
      }
    },

    getParseStatusIcon(status) {
      switch (status) {
        case 'COMPLETED':
          return 'el-icon-check'
        case 'FAILED':
          return 'el-icon-close'
        case 'PARSING':
          return 'el-icon-loading'
        default:
          return 'el-icon-time'
      }
    },

    getParseStatusText(status) {
      switch (status) {
        case 'COMPLETED':
          return '解析完成'
        case 'FAILED':
          return '解析失败'
        case 'PARSING':
          return '解析中'
        case 'PENDING':
          return '等待解析'
        default:
          return '未知状态'
      }
    }
  }
}
</script>

<style scoped>
.table-meta-detail {
  padding: 20px;
}

.back-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-header h2 {
  flex: 1;
  margin: 0 20px;
  color: #409eff;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.basic-info .info-item {
  margin-bottom: 10px;
}

.basic-info .info-item label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.basic-info .error-text {
  color: #f56c6c;
}

.sql-content pre {
  background-color: #f6f8fa;
  padding: 15px;
  border-radius: 4px;
  font-size: 13px;
  color: #24292e;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  max-height: 300px;
  overflow-y: auto;
}

.no-data {
  text-align: center;
  padding: 40px 0;
}
</style>
