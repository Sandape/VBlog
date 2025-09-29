<style type="text/css">
  .blog_table_footer {
    display: flex;
    box-sizing: content-box;
    padding-top: 10px;
    padding-bottom: 0px;
    margin-bottom: 0px;
    justify-content: space-between;
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

  /* 标题列样式 */
  .article-title-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .article-icon {
    color: #20a0ff;
    font-size: 16px;
  }

  .article-title-text {
    color: #303133;
    font-weight: 500;
    transition: color 0.2s ease;
    cursor: pointer;
  }

  .article-title-text:hover {
    color: #20a0ff;
  }

  /* 表格斑马纹样式增强 */
  .el-table--striped .el-table__body tr.el-table__row--striped td {
    background-color: #fafafa;
  }

  .el-table--striped .el-table__body tr.el-table__row--striped:hover td {
    background-color: #f0f8ff;
  }

  /* 搜索框样式 */
  .search-container {
    display: flex;
    justify-content: flex-start;
    margin-bottom: 15px;
  }

  .search-input {
    width: 400px;
  }

  /* 响应式设计 */
  @media (max-width: 768px) {
    .search-container {
      flex-direction: column;
      gap: 10px;
    }

    .search-input {
      width: 100%;
    }

    .el-table th,
    .el-table td {
      padding: 8px 0;
    }

    .article-title-cell {
      gap: 6px;
    }

    .article-icon {
      font-size: 14px;
    }

    .article-title-text {
      font-size: 14px;
    }
  }
</style>
<template>
  <div>
    <div class="search-container">
      <el-input
        placeholder="通过标题搜索该分类下的博客..."
        prefix-icon="el-icon-search"
        v-model="keywords"
        class="search-input"
        size="mini"
        @keyup.enter.native="searchClick">
      </el-input>
      <el-button type="primary" icon="el-icon-search" size="mini" style="margin-left: 8px" @click="searchClick">搜索
      </el-button>
    </div>
    <el-table
      ref="multipleTable"
      :data="articles"
      tooltip-effect="dark"
      style="width: 100%"
      border
      stripe
      @selection-change="handleSelectionChange"
      v-loading="loading">
      <el-table-column
        type="selection"
        width="35" align="left" v-if="showEdit || showDelete">
      </el-table-column>
      <el-table-column
        label="标题"
        min-width="300" align="left">
        <template slot-scope="scope">
          <div class="article-title-cell">
            <i class="el-icon-document article-icon"></i>
            <span class="article-title-text" @click="itemClick(scope.row)">{{ scope.row.title}}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        label="最近编辑时间"
        width="150"
        align="center">
        <template slot-scope="scope">{{ scope.row.editTime | formatDateTime}}</template>
      </el-table-column>
      <el-table-column
        prop="nickname"
        label="作者"
        width="100"
        align="center">
      </el-table-column>
      <el-table-column
        prop="cateName"
        label="所属分类"
        width="120"
        align="center">
      </el-table-column>
      <el-table-column label="操作" align="left" v-if="showEdit || showDelete">
        <template slot-scope="scope">
          <el-button
            size="mini"
            @click="handleEdit(scope.$index, scope.row)" v-if="showEdit">编辑
          </el-button>
          <el-button
            size="mini"
            @click="handleRestore(scope.$index, scope.row)" v-if="showRestore">还原
          </el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.$index, scope.row)" v-if="showDelete">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="blog_table_footer">
      <el-button type="danger" size="mini" style="margin: 0px;" v-show="this.articles.length>0 && showDelete"
                 :disabled="this.selItems.length==0" @click="deleteMany">批量删除
      </el-button>
      <span></span>
      <el-pagination
        background
        :page-size="pageSize"
        layout="prev, pager, next"
        :total="totalCount" @current-change="currentChange" v-show="this.articles.length>0">
      </el-pagination>
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
        if (!row || !row.id) {
          this.$message.error('PromptID不存在')
          return
        }
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
            const data = resp.data;
            // 适配新的返回格式：PageResultDTO
            if (data.records && data.totalCount !== undefined) {
              _this.articles = data.records;
              _this.totalCount = data.totalCount;
            } else {
              // 兼容旧的返回格式
              _this.articles = data.articles;
              _this.totalCount = data.totalCount;
            }
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
