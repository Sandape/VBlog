<template>
  <div class="usage-records-container">
    <div class="title-section">
      <h2>使用记录</h2>
    </div>

    <DataTable
      :data="records"
      :columns="columns"
      :actions="actions"
      :loading="loading"
      :show-pagination="true"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="totalCount"
      :search-placeholder="'输入用户名搜索'"
      :search-fields="['username', 'nickname', 'projectName', 'apiName']"
      :default-sort="{ prop: 'createTime', order: 'descending' }"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
      @search="handleSearch"
      @action="handleAction"
      @row-click="showDetail"
    />

    <!-- 详情对话框 -->
    <el-dialog
      title="使用记录详情"
      :visible.sync="detailVisible"
      width="90%"
      top="5vh"
      :before-close="closeDetail"
      class="usage-detail-dialog"
    >
      <div v-if="currentRecord" class="detail-content">
        <!-- 基本信息卡片 -->
        <el-card class="info-card" shadow="never">
          <div slot="header" class="card-header">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <el-row :gutter="24">
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">用户信息</div>
                <div class="info-value">
                  <el-tag type="primary" size="small">{{ currentRecord.userNickname }}</el-tag>
                  <span class="username-text">({{ currentRecord.username }})</span>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">项目名称</div>
                <div class="info-value">
                  <el-tag type="success" size="small">{{ currentRecord.projectName }}</el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">创建时间</div>
                <div class="info-value">{{ formatDate(currentRecord.createTime) }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 接口信息卡片 -->
        <el-card class="info-card" shadow="never">
          <div slot="header" class="card-header">
            <i class="el-icon-setting"></i>
            <span>接口信息</span>
          </div>
          <el-row :gutter="24">
            <el-col :span="12">
              <div class="info-item">
                <div class="info-label">接口名称</div>
                <div class="info-value">{{ currentRecord.apiName }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <div class="info-label">接口路径</div>
                <div class="info-value">
                  <el-input
                    v-model="currentRecord.apiPath"
                    readonly
                    size="small"
                    class="path-input"
                  ></el-input>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="24" v-if="currentRecord.apiDesc">
            <el-col :span="24">
              <div class="info-item">
                <div class="info-label">接口描述</div>
                <div class="info-value description">{{ currentRecord.apiDesc }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 请求信息卡片 -->
        <el-card class="info-card" shadow="never" v-if="currentRecord.apiRequest">
          <div slot="header" class="card-header">
            <i class="el-icon-upload"></i>
            <span>请求信息</span>
          </div>
          <pre class="json-code-block">{{ formatJson(currentRecord.apiRequest) }}</pre>
        </el-card>

        <!-- 响应信息卡片 -->
        <el-card class="info-card" shadow="never" v-if="currentRecord.apiResponse">
          <div slot="header" class="card-header">
            <i class="el-icon-download"></i>
            <span>响应信息</span>
          </div>
          <pre class="json-code-block">{{ formatJson(currentRecord.apiResponse) }}</pre>
        </el-card>

        <!-- 生成的Prompt卡片 -->
        <el-card class="info-card" shadow="never" v-if="currentRecord.finalPrompt">
          <div slot="header" class="card-header">
            <i class="el-icon-document"></i>
            <span>生成的Prompt</span>
          </div>
          <pre class="prompt-code-block">{{ currentRecord.finalPrompt }}</pre>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import DataTable from './DataTable.vue'

export default {
  name: 'UsageRecords',
  components: {
    DataTable
  },
  data() {
    return {
      records: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalCount: 0,
      searchUsername: '',
      detailVisible: false,
      currentRecord: null,
      columns: [
        {
          prop: 'username',
          label: '用户名',
          width: 120,
          sortable: true
        },
        {
          prop: 'nickname',
          label: '昵称',
          width: 120,
          sortable: true
        },
        {
          prop: 'projectName',
          label: '项目名称',
          width: 150,
          sortable: true
        },
        {
          prop: 'apiName',
          label: '接口名称',
          width: 200,
          sortable: true
        },
        {
          prop: 'apiPath',
          label: '接口路径',
          width: 200,
          sortable: true
        },
        {
          prop: 'createTime',
          label: '创建时间',
          width: 180,
          sortable: true,
          type: 'datetime'
        }
      ],
      actions: [
        {
          key: 'detail',
          label: '查看详情',
          type: 'text',
          icon: 'el-icon-view'
        }
      ]
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    loadRecords() {
      this.loading = true
      const params = {
        page: this.currentPage,
        pageSize: this.pageSize
      }
      if (this.searchUsername.trim()) {
        params.username = this.searchUsername
      }

      axios.get('/statistics/usage/records', { params }).then(resp => {
        this.loading = false
        if (resp && resp.data && resp.data.status === 'success') {
          const data = resp.data.obj
          this.records = data.records || []
          this.totalCount = data.totalCount || 0
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('获取使用记录失败')
      })
    },

    handlePageChange(page) {
      this.currentPage = page
      this.loadRecords()
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadRecords()
    },

    handleSearch(searchText) {
      this.searchUsername = searchText || ''
      this.currentPage = 1
      this.loadRecords()
    },

    handleAction(actionKey, row, index) {
      if (actionKey === 'detail') {
        this.showDetail(row)
      }
    },

    showDetail(row) {
      // 获取详细信息
      axios.get(`/statistics/usage/records/${row.id}`).then(resp => {
        if (resp && resp.data && resp.data.status === 'success') {
          this.currentRecord = resp.data.obj
          this.detailVisible = true
        } else {
          this.$message.error('获取详情失败')
        }
      }).catch(() => {
        this.$message.error('获取详情失败')
      })
    },

    closeDetail() {
      this.detailVisible = false
      this.currentRecord = null
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    },

    formatJson(jsonStr) {
      if (!jsonStr) return '无'
      try {
        const parsed = JSON.parse(jsonStr)
        return JSON.stringify(parsed, null, 2)
      } catch (error) {
        return jsonStr
      }
    }
  }
}
</script>

<style scoped>
.usage-records-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.title-section {
  margin-bottom: 20px;
}

.title-section h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
  text-align: center;
}

/* 对话框样式 */
.usage-detail-dialog .el-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.usage-detail-dialog .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin: 0;
  padding: 20px 24px;
}

.usage-detail-dialog .el-dialog__title {
  color: white;
  font-weight: 600;
}

.usage-detail-dialog .el-dialog__headerbtn {
  top: 16px;
  right: 20px;
}

.usage-detail-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white;
  font-size: 20px;
}

.usage-detail-dialog .el-dialog__body {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

/* 卡片样式 */
.info-card {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.info-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.card-header i {
  margin-right: 8px;
  color: #67C23A;
}

/* 信息项样式 */
.info-item {
  margin-bottom: 16px;
}

.info-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 6px;
}

.info-value {
  font-size: 14px;
  color: #303133;
  display: flex;
  align-items: center;
}

.username-text {
  margin-left: 8px;
  color: #909399;
}

.description {
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 输入框样式 */
.path-input {
  width: 100%;
}

.path-input >>> .el-input__inner {
  background-color: #f8f9fa;
  border-color: #e9ecef;
  color: #495057;
}

/* 代码块样式 */
.json-code-block, .prompt-code-block {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 16px;
  margin: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #495057;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow-y: auto;
  overflow-x: auto;
}

.prompt-code-block {
  background-color: #f0f9ff;
  border-color: #b3e5fc;
  color: #01579b;
}

/* 滚动条样式 */
.json-code-block::-webkit-scrollbar,
.prompt-code-block::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.json-code-block::-webkit-scrollbar-track,
.prompt-code-block::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.json-code-block::-webkit-scrollbar-thumb,
.prompt-code-block::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.json-code-block::-webkit-scrollbar-thumb:hover,
.prompt-code-block::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 表格行样式 */
.el-table tbody tr {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.el-table tbody tr:hover {
  background-color: #f5f7fa;
}
</style>


