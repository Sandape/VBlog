<template>
  <div class="usage-stats">
    <div class="main-layout">
      <!-- 左侧：使用记录表格 -->
      <div class="left-section">
        <div class="log-section">
          <h3 class="section-title">
            <i class="el-icon-s-promotion"></i>
            使用记录
          </h3>
          
          <!-- 搜索框 -->
          <div class="search-section">
            <el-input
              v-model="searchUsername"
              placeholder="输入用户名搜索"
              style="width: 200px; margin-right: 10px;"
              @keyup.enter="searchUsageLogs"
            >
              <el-button slot="append" icon="el-icon-search" @click="searchUsageLogs"></el-button>
            </el-input>
            <el-button @click="loadAllUsageLogs" type="primary" plain>显示全部</el-button>
          </div>
          
          <!-- 使用记录表格 -->
          <div class="table-container">
            <el-table
              :data="usageLogs"
              v-loading="loading"
              style="width: 100%"
              :height="400"
            >
              <el-table-column prop="userNickname" label="用户" width="100"></el-table-column>
              <el-table-column prop="projectName" label="项目" width="120"></el-table-column>
              <el-table-column prop="apiName" label="接口名" width="150" show-overflow-tooltip></el-table-column>
              <el-table-column prop="apiPath" label="接口路径" width="200" show-overflow-tooltip></el-table-column>
              <el-table-column prop="createTime" label="使用时间" width="160">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" @click="viewDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              @current-change="handlePageChange"
              :current-page="currentPage"
              :page-size="pageSize"
              :total="totalCount"
              layout="total, prev, pager, next"
            >
            </el-pagination>
          </div>
        </div>
      </div>
      
      <!-- 右侧：图表区域 -->
      <div class="right-section">
        <!-- 按用户统计使用次数 -->
        <div class="chart-section">
          <h3 class="section-title">
            <i class="el-icon-data-line"></i>
            用户使用统计
          </h3>
          <div id="usageChart" style="width: 100%; height: 300px;"></div>
        </div>
      </div>
    </div>

    <!-- 使用记录详情对话框 -->
    <el-dialog
      title="使用记录详情"
      :visible.sync="detailDialogVisible"
      width="70%"
      :close-on-click-modal="false"
    >
      <div v-if="currentLog">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="100px">
              <el-form-item label="用户:">
                <span>{{ currentLog.userNickname }}</span>
              </el-form-item>
              <el-form-item label="项目:">
                <span>{{ currentLog.projectName }}</span>
              </el-form-item>
              <el-form-item label="接口名:">
                <span>{{ currentLog.apiName }}</span>
              </el-form-item>
              <el-form-item label="接口路径:">
                <el-tag type="info">{{ currentLog.apiPath }}</el-tag>
              </el-form-item>
              <el-form-item label="使用时间:">
                <span>{{ formatDate(currentLog.createTime) }}</span>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="100px">
              <el-form-item label="接口描述:">
                <div class="description-text">{{ currentLog.apiDesc || '无描述' }}</div>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <el-divider>AI生成的Prompt</el-divider>
        <div class="prompt-content">
          <pre v-if="currentLog.finalPrompt">{{ currentLog.finalPrompt }}</pre>
          <span v-else style="color: #999;">暂无Prompt内容</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getUsageLogs, getUsageStatsByUser, searchUsageLogs } from '../utils/api'

export default {
  name: 'UsageStats',
  data() {
    return {
      // 使用记录相关
      usageLogs: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalCount: 0,
      searchUsername: '',
      
      // 详情对话框
      detailDialogVisible: false,
      currentLog: null,
      
      // 图表相关
      usageOptions: {
        title: {
          text: '用户使用次数统计',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: [],
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: '使用次数'
        },
        series: [{
          data: [],
          type: 'bar',
          itemStyle: {
            color: '#67C23A'
          },
          barWidth: '60%'
        }]
      }
    }
  },
  mounted() {
    this.loadUsageLogs()
    this.loadUsageStats()
  },
  methods: {
    // 加载使用记录
    loadUsageLogs() {
      this.loading = true
      getUsageLogs(this.currentPage, this.pageSize).then(resp => {
        this.loading = false
        if (resp && resp.data) {
          this.usageLogs = resp.data.usageLogs || []
          this.totalCount = resp.data.totalCount || 0
        }
      }).catch(() => {
        this.loading = false
      })
    },
    
    // 搜索使用记录
    searchUsageLogs() {
      if (!this.searchUsername.trim()) {
        this.loadUsageLogs()
        return
      }
      
      this.loading = true
      searchUsageLogs(this.searchUsername, this.currentPage, this.pageSize).then(resp => {
        this.loading = false
        if (resp && resp.data) {
          this.usageLogs = resp.data.usageLogs || []
          this.totalCount = resp.data.totalCount || 0
        }
      }).catch(() => {
        this.loading = false
      })
    },
    
    // 加载所有使用记录
    loadAllUsageLogs() {
      this.searchUsername = ''
      this.currentPage = 1
      this.loadUsageLogs()
    },
    
    // 加载使用统计
    loadUsageStats() {
      getUsageStatsByUser().then(resp => {
        if (resp && resp.data) {
          this.usageOptions.xAxis.data = resp.data.usernames || []
          this.usageOptions.series[0].data = resp.data.usageCounts || []
          
          // 更新图表
          this.$nextTick(() => {
            const chart = echarts.init(document.getElementById('usageChart'))
            chart.setOption(this.usageOptions)
          })
        }
      })
    },
    
    // 分页处理
    handlePageChange(page) {
      this.currentPage = page
      if (this.searchUsername.trim()) {
        this.searchUsageLogs()
      } else {
        this.loadUsageLogs()
      }
    },
    
    // 查看详情
    viewDetail(log) {
      this.currentLog = log
      this.detailDialogVisible = true
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return d.getFullYear() + '-' + 
             String(d.getMonth() + 1).padStart(2, '0') + '-' + 
             String(d.getDate()).padStart(2, '0') + ' ' +
             String(d.getHours()).padStart(2, '0') + ':' + 
             String(d.getMinutes()).padStart(2, '0') + ':' + 
             String(d.getSeconds()).padStart(2, '0')
    }
  }
}
</script>

<style scoped>
.usage-stats {
  width: 100%;
}

.main-layout {
  display: flex;
  gap: 20px;
  min-height: 500px;
}

.left-section {
  flex: 2;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.right-section {
  flex: 1;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.log-section {
  height: 100%;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #67C23A;
  padding-bottom: 10px;
}

.section-title i {
  color: #67C23A;
  font-size: 20px;
}

.search-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.table-container {
  margin-bottom: 20px;
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.chart-section {
  height: 100%;
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.description-text {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  color: #606266;
}

.prompt-content {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.prompt-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #2c3e50;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-layout {
    flex-direction: column;
  }
  
  .right-section {
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .left-section,
  .right-section {
    padding: 16px;
  }
  
  .section-title {
    font-size: 16px;
  }
  
  .search-section {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .search-section .el-input {
    width: 100% !important;
  }
}
</style>
