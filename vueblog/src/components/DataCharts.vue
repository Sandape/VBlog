<template>
  <div class="data-statistics-container">
    <div class="main-layout">
      <!-- 左侧：登录日志表格 -->
      <div class="left-section">
        <div class="log-section">
          <h3 class="section-title">登录记录</h3>
          
          <!-- 搜索框 -->
          <div class="search-section">
            <el-input
              v-model="searchUsername"
              placeholder="输入用户名搜索"
              style="width: 200px; margin-right: 10px;"
              @keyup.enter="searchLoginLogs"
            >
              <el-button slot="append" icon="el-icon-search" @click="searchLoginLogs"></el-button>
            </el-input>
            <el-button @click="loadAllLoginLogs" type="primary" plain>显示全部</el-button>
          </div>
          
          <!-- 登录日志表格 -->
          <div class="table-container">
            <el-table
              :data="loginLogs"
              v-loading="loading"
              style="width: 100%"
              :height="400"
            >
              <el-table-column prop="username" label="用户名" width="120"></el-table-column>
              <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
              <el-table-column prop="ipAddress" label="IP地址" width="130"></el-table-column>
              <el-table-column prop="loginStatus" label="状态" width="80">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.loginStatus === 'SUCCESS' ? 'success' : 'danger'">
                    {{ scope.row.loginStatus === 'SUCCESS' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="loginTime" label="登录时间" width="180">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.loginTime) }}
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
        <!-- 按用户统计登录次数 -->
        <div class="chart-section">
          <h3 class="section-title">用户登录统计</h3>
          <div id="loginChart" style="width: 100%; height: 300px;"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'DataCharts',
  data() {
    return {
      // 登录日志相关
      loginLogs: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalCount: 0,
      searchUsername: '',
      
      // 图表相关
      loginOptions: {
        title: {
          text: '用户登录次数统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: []
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: [],
          type: 'bar',
          itemStyle: {
            color: '#409EFF'
          }
        }]
      }
    }
  },
  mounted() {
    this.loadLoginLogs()
    this.loadLoginStats()
  },
  methods: {
    // 加载登录日志
    loadLoginLogs() {
      this.loading = true
      axios.get('/article/loginLogs', {
        params: {
          page: this.currentPage,
          count: this.pageSize
        }
      }).then(resp => {
        this.loading = false
        if (resp && resp.data) {
          this.loginLogs = resp.data.loginLogs || []
          this.totalCount = resp.data.totalCount || 0
        }
      }).catch(() => {
        this.loading = false
      })
    },
    
    // 搜索登录日志
    searchLoginLogs() {
      if (!this.searchUsername.trim()) {
        this.loadLoginLogs()
        return
      }
      
      this.loading = true
      axios.get('/article/searchLoginLogs', {
        params: {
          username: this.searchUsername,
          page: this.currentPage,
          count: this.pageSize
        }
      }).then(resp => {
        this.loading = false
        if (resp && resp.data) {
          this.loginLogs = resp.data.loginLogs || []
          this.totalCount = resp.data.totalCount || 0
        }
      }).catch(() => {
        this.loading = false
      })
    },
    
    // 加载所有登录日志
    loadAllLoginLogs() {
      this.searchUsername = ''
      this.currentPage = 1
      this.loadLoginLogs()
    },
    
    // 加载登录统计
    loadLoginStats() {
      axios.get('/article/loginStatsByUser').then(resp => {
        if (resp && resp.data) {
          this.loginOptions.xAxis.data = resp.data.usernames || []
          this.loginOptions.series[0].data = resp.data.loginCounts || []
          
          // 更新图表
          this.$nextTick(() => {
            const chart = echarts.init(document.getElementById('loginChart'))
            chart.setOption(this.loginOptions)
          })
        }
      })
    },
    
    // 分页处理
    handlePageChange(page) {
      this.currentPage = page
      if (this.searchUsername.trim()) {
        this.searchLoginLogs()
      } else {
        this.loadLoginLogs()
      }
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
.data-statistics-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.main-layout {
  display: flex;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.left-section {
  flex: 2;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.right-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.log-section {
  height: 100%;
}

.section-title {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 10px;
}

.search-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.chart-section {
  height: 100%;
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
</style>
