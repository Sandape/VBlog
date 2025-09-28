<template>
  <div class="blog-table-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-bar">
        <div class="search-input-wrapper">
          <i class="el-icon-search search-icon"></i>
          <el-input
            placeholder="搜索Prompt标题..."
            v-model="keywords"
            size="medium"
            @keyup.enter.native="searchClick">
          </el-input>
        </div>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="medium"
          class="search-btn"
          @click="searchClick">
          搜索
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-table
        ref="multipleTable"
        :data="articles"
        tooltip-effect="dark"
        class="custom-table"
        max-height="500"
        @selection-change="handleSelectionChange"
        v-loading="loading">

        <!-- 选择列 -->
        <el-table-column
          type="selection"
          width="50"
          align="center"
          v-if="showEdit || showDelete">
        </el-table-column>

        <!-- 标题列 -->
        <el-table-column label="标题" min-width="300" align="left">
          <template slot-scope="scope">
            <div class="title-cell" @click="itemClick(scope.row)">
              <i class="el-icon-document-copy title-icon"></i>
              <span class="title-text">{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 编辑时间列 -->
        <el-table-column label="编辑时间" width="200" align="center">
          <template slot-scope="scope">
            <div class="time-cell">
              <i class="el-icon-time"></i>
              <span>{{ scope.row.editTime | formatDateTime }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 作者列 -->
        <el-table-column label="作者" width="120" align="center">
          <template slot-scope="scope">
            <div class="author-cell">
              <i class="el-icon-user"></i>
              <span>{{ scope.row.nickname }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 分类列 -->
        <el-table-column label="分类" width="140" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.cateName" size="small" class="category-tag">
              {{ scope.row.cateName }}
            </el-tag>
            <span v-else class="no-category">未分类</span>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="200" align="center" v-if="showEdit || showDelete">
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button
                size="mini"
                type="primary"
                icon="el-icon-edit"
                @click="handleEdit(scope.$index, scope.row)"
                v-if="showEdit">
                编辑
              </el-button>
              <el-button
                size="mini"
                type="success"
                icon="el-icon-refresh"
                @click="handleRestore(scope.$index, scope.row)"
                v-if="showRestore">
                还原
              </el-button>
              <el-button
                size="mini"
                type="danger"
                icon="el-icon-delete"
                @click="handleDelete(scope.$index, scope.row)"
                v-if="showDelete">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 底部操作栏 -->
    <div class="table-footer" v-if="articles.length > 0">
      <div class="footer-left">
        <el-button
          type="danger"
          size="small"
          icon="el-icon-delete"
          v-show="showDelete"
          :disabled="selItems.length === 0"
          @click="deleteMany">
          批量删除 ({{ selItems.length }})
        </el-button>
      </div>

      <div class="footer-right">
        <el-pagination
          background
          :page-size="pageSize"
          layout="prev, pager, next, jumper"
          :total="totalCount"
          :current-page="currentPage"
          @current-change="currentChange">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
  import {putRequest} from '../utils/api'
  import {getRequest} from '../utils/api'
//  import Vue from 'vue'
//  var bus = new Vue()

  export default{
    data() {
      return {
        articles: [],
        selItems: [],
        loading: false,
        currentPage: 1,
        totalCount: -1,
        pageSize: 6,
        keywords: '',
        dustbinData: []
      }
    },
    mounted: function () {
      var _this = this;
      this.loading = true;
      this.loadBlogs(1, this.pageSize);
      var _this = this;
      window.bus.$on('blogTableReload', function () {
        _this.loading = true;
        _this.loadBlogs(_this.currentPage, _this.pageSize);
      })
    },
    methods: {
      searchClick(){
        this.loadBlogs(1, this.pageSize);
      },
      itemClick(row){
        this.$router.push({path: '/blogDetail', query: {aid: row.id}})
      },
      deleteMany(){
        var selItems = this.selItems;
        for (var i = 0; i < selItems.length; i++) {
          this.dustbinData.push(selItems[i].id)
        }
        this.deleteToDustBin(selItems[0].state)
      },
      //翻页
      currentChange(currentPage){
        this.currentPage = currentPage;
        this.loading = true;
        this.loadBlogs(currentPage, this.pageSize);
      },
      loadBlogs(page, count){
        var _this = this;
        var url = '';
        if (this.state == -2) {
          url = "/admin/article/all" + "?page=" + page + "&count=" + count + "&keywords=" + this.keywords;
        } else {
          url = "/article/all?state=" + this.state + "&page=" + page + "&count=" + count + "&keywords=" + this.keywords;
        }
        getRequest(url).then(resp=> {
          _this.loading = false;
          if (resp.status == 200) {
            _this.articles = resp.data.articles;
            _this.totalCount = resp.data.totalCount;
          } else {
            _this.$message({type: 'error', message: '数据加载失败!'});
          }
        }, resp=> {
          _this.loading = false;
          if (resp.response.status == 403) {
            _this.$message({type: 'error', message: resp.response.data});
          } else {
            _this.$message({type: 'error', message: '数据加载失败!'});
          }
        }).catch(resp=> {
          //压根没见到服务器
          _this.loading = false;
          _this.$message({type: 'error', message: '数据加载失败!'});
        })
      },
      handleSelectionChange(val) {
        this.selItems = val;
      },
      handleEdit(index, row) {
        this.$router.push({path: '/editBlog', query: {from: this.activeName,id:row.id}});
      },
      handleDelete(index, row) {
        this.dustbinData.push(row.id);
        this.deleteToDustBin(row.state);
      },
      handleRestore(index, row) {
        let _this = this;
        this.$confirm('将该文件还原到原处，是否继续？','提示',{
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        } ).then(() => {
          _this.loading = true;
          putRequest('/article/restore', {articleId: row.id}).then(resp=> {
            if (resp.status == 200) {
              var data = resp.data;
              _this.$message({type: data.status, message: data.msg});
              if (data.status == 'success') {
                window.bus.$emit('blogTableReload')//通过选项卡都重新加载数据
              }
            } else {
              _this.$message({type: 'error', message: '还原失败!'});
            }
            _this.loading = false;
          });
        }).catch(() => {
          _this.$message({
            type: 'info',
            message: '已取消还原'
          });
        });
      },
      deleteToDustBin(state){
        var _this = this;
        this.$confirm(state != 2 ? '将该文件放入回收站，是否继续?' : '永久删除该文件, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          _this.loading = true;
          var url = '';
          if (_this.state == -2) {
            url = "/admin/article/dustbin";
          } else {
            url = "/article/dustbin";
          }
          putRequest(url, {aids: _this.dustbinData, state: state}).then(resp=> {
            if (resp.status == 200) {
              var data = resp.data;
              _this.$message({type: data.status, message: data.msg});
              if (data.status == 'success') {
                window.bus.$emit('blogTableReload')//通过选项卡都重新加载数据
              }
            } else {
              _this.$message({type: 'error', message: '删除失败!'});
            }
            _this.loading = false;
            _this.dustbinData = []
          }, resp=> {
            _this.loading = false;
            _this.$message({type: 'error', message: '删除失败!'});
            _this.dustbinData = []
          });
        }).catch(() => {
          _this.$message({
            type: 'info',
            message: '已取消删除'
          });
          _this.dustbinData = []
        });
      }
    },
    props: ['state', 'showEdit', 'showDelete', 'activeName', 'showRestore']
  }
</script>

<style scoped>
/* 容器样式 */
.blog-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* 搜索区域 */
.search-section {
  padding: 24px 24px 0 24px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 600px;
}

.search-input-wrapper {
  position: relative;
  flex: 1;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #909399;
  z-index: 1;
}

.search-input-wrapper :deep(.el-input__inner) {
  padding-left: 44px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  transition: all 0.2s ease;
}

.search-input-wrapper :deep(.el-input__inner):focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.search-btn {
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 500;
  height: 40px;
  transition: all 0.2s ease;
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 表格区域 */
.table-section {
  padding: 0 24px;
}

/* 自定义表格样式 */
.custom-table {
  border-radius: 0;
  border: none;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
  border-bottom: 2px solid #e4e7ed;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 0;
}

:deep(.el-table tr) {
  transition: background-color 0.2s ease;
}

:deep(.el-table tr:hover > td) {
  background-color: #f8f9ff;
}

:deep(.el-table__row--striped) {
  background-color: #fafafa;
}

:deep(.el-table__row--striped:hover > td) {
  background-color: #f5f7ff;
}

/* 表格单元格样式 */
.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.title-cell:hover {
  background-color: #e8f4ff;
  color: #409eff;
}

.title-icon {
  color: #409eff;
  font-size: 16px;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.time-cell,
.author-cell {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 6px;
  background-color: #f8f9fa;
  color: #606266;
  font-size: 13px;
  white-space: nowrap;
}

.category-tag {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1976d2;
  border: none;
  border-radius: 12px;
  font-weight: 500;
}

.no-category {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

/* 操作按钮组 */
.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.action-buttons .el-button {
  border-radius: 6px;
  font-weight: 500;
}

/* 底部操作栏 */
.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #f8f9fa;
  border-top: 1px solid #e4e7ed;
}

.footer-left {
  display: flex;
  align-items: center;
}

.footer-right {
  display: flex;
  align-items: center;
}

/* 分页样式 */
:deep(.el-pagination) {
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background-color: #409eff;
  border-radius: 6px;
}

:deep(.el-pagination.is-background .el-pager li:not(.disabled):hover) {
  color: #409eff;
}

:deep(.el-pagination.is-background .btn-next:not(.disabled):hover,
.el-pagination.is-background .btn-prev:not(.disabled):hover) {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .search-section,
  .table-section {
    padding: 0 16px;
  }

  .table-footer {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .search-section,
  .table-section {
    padding: 0 12px;
  }

  .search-bar {
    flex-direction: column;
    gap: 16px;
  }

  .search-input-wrapper {
    width: 100%;
  }

  .table-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
    padding: 12px;
  }

  .footer-left,
  .footer-right {
    justify-content: center;
  }

  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 10px 0;
  }

  .search-btn {
    padding: 10px 20px;
    height: 36px;
  }
}

@media (max-width: 480px) {
  .search-section,
  .table-section {
    padding: 0 8px;
  }

  .table-footer {
    padding: 8px;
  }

  .title-cell {
    flex-direction: column;
    gap: 4px;
    text-align: center;
    padding: 6px 8px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }

  .search-btn {
    padding: 8px 16px;
    height: 32px;
    font-size: 12px;
  }
}
</style>
