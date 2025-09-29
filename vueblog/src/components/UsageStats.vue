<template>
  <div class="usage-stats-container">
    <div class="header-section">
      <h2>使用统计</h2>
    </div>

    <div class="stats-content">
      <div class="chart-section">
        <h3>用户使用次数统计</h3>
        <div id="usageChart" style="width: 100%; height: 400px;"></div>
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
          <el-table-column prop="usageCount" label="使用次数" width="100" sortable></el-table-column>
          <el-table-column prop="lastUsageTime" label="最后使用时间" width="180"></el-table-column>
          <el-table-column prop="mostUsedProject" label="最常用项目" width="200"></el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'UsageStats',
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
      axios.get('/statistics/usage/stats').then(resp => {
        this.loading = false
        if (resp && resp.data && resp.data.status === 'success') {
          this.statsData = resp.data.obj || []
          this.renderChart()
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('获取使用统计失败')
      })
    },

    renderChart() {
      this.$nextTick(() => {
        const chartDom = document.getElementById('usageChart')
        if (!chartDom) return

        this.chart = echarts.init(chartDom)
        
        const usernames = this.statsData.map(item => item.username)
        const usageCounts = this.statsData.map(item => item.usageCount)

        const option = {
          title: {
            text: '用户使用次数统计',
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
            data: usageCounts,
            type: 'bar',
            itemStyle: {
              color: '#67C23A'
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
.usage-stats-container {
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
  border-bottom: 2px solid #67C23A;
  padding-bottom: 10px;
}
</style>

