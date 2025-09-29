<template>
  <div class="login-stats-container">
    <div class="header-section">
      <h2>登录统计</h2>
    </div>

    <div class="stats-content">
      <div class="chart-section">
        <h3>用户登录次数统计</h3>
        <div id="loginChart" style="width: 100%; height: 400px;"></div>
      </div>

      <div class="table-section">
        <h3>详细统计数据</h3>
        <el-table
          :data="statsData"
          v-loading="loading"
          style="width: 100%"
        >
          <el-table-column prop="username" label="用户名" width="120"></el-table-column>
          <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
          <el-table-column prop="loginCount" label="登录次数" width="100" sortable></el-table-column>
          <el-table-column prop="lastLoginTime" label="最后登录时间" width="180"></el-table-column>
          <el-table-column prop="lastLoginIp" label="最后登录IP" width="130"></el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'LoginStats',
  data() {
    return {
      statsData: [],
      loading: false,
      chart: null
    }
  },
  mounted() {
    this.loadStats()
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
    }
  },
  methods: {
    loadStats() {
      this.loading = true
      axios.get('/statistics/login/stats').then(resp => {
        this.loading = false
        if (resp && resp.data && resp.data.status === 'success') {
          this.statsData = resp.data.obj || []
          this.renderChart()
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('获取登录统计失败')
      })
    },

    renderChart() {
      this.$nextTick(() => {
        const chartDom = document.getElementById('loginChart')
        if (!chartDom) return

        this.chart = echarts.init(chartDom)
        
        const usernames = this.statsData.map(item => item.username)
        const loginCounts = this.statsData.map(item => item.loginCount)

        const option = {
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
            data: usernames,
            axisLabel: {
              rotate: 45
            }
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            data: loginCounts,
            type: 'bar',
            itemStyle: {
              color: '#409EFF'
            }
          }]
        }

        this.chart.setOption(option)
      })
    }
  }
}
</script>

<style scoped>
.login-stats-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-section, .table-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.chart-section h3, .table-section h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 10px;
}
</style>


