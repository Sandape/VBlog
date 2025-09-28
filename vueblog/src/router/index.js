import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/components/Login'
import Home from '@/components/Home'
import ArticleList from '@/components/ArticleList'
import CateMana from '@/components/CateMana'
import DataCharts from '@/components/DataCharts'
import PostArticle from '@/components/PostArticle'
import UserMana from '@/components/UserMana'
import BlogDetail from '@/components/BlogDetail'
import PublicArticleList from '@/components/PublicArticleList'
import PublicArticleDetail from '@/components/PublicArticleDetail'
import ProjectList from '@/components/ProjectList'
import CreateProject from '@/components/CreateProject'
import JoinProject from '@/components/JoinProject'
import PromptGenerator from '@/components/PromptGenerator'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: '登录',
      hidden: true,
      component: Login
    }, {
      path: '/home',
      name: '',
      component: Home,
      hidden: true
    }, {
      path: '/home',
      component: Home,
      name: 'Prompt管理',
      iconCls: 'fa fa-file-text-o',
      children: [
        {
          path: '/publicArticleList',
          name: '公开Prompt',
          component: PublicArticleList,
          meta: {
            keepAlive: true
          }
        }, {
          path: '/articleList',
          name: 'Prompt列表',
          component: ArticleList,
          meta: {
            keepAlive: true
          }
        }, {
          path: '/postArticle',
          name: '发表Prompt',
          component: PostArticle,
          meta: {
            keepAlive: false
          }
        }, {
          path: '/blogDetail',
          name: '博客详情',
          component: BlogDetail,
          hidden: true,
          meta: {
            keepAlive: false
          }
        }, {
          path: '/editBlog',
          name: '编辑博客',
          component: PostArticle,
          hidden: true,
          meta: {
            keepAlive: false
          }
        }, {
          path: '/promptGenerator',
          name: 'Prompt生成',
          component: PromptGenerator,
          meta: {
            keepAlive: false
          }
        }
      ]
    }, {
      path: '/home',
      component: Home,
      name: '项目管理',
      iconCls: 'fa fa-folder-o',
      children: [
        {
          path: '/projectList',
          name: '项目列表',
          component: ProjectList,
          meta: {
            keepAlive: true
          }
        }, {
          path: '/createProject',
          name: '创建项目',
          component: CreateProject,
          meta: {
            keepAlive: false
          }
        }, {
          path: '/joinProject',
          name: '加入项目',
          component: JoinProject,
          meta: {
            keepAlive: false
          }
        }, {
          path: '/editProject',
          name: '编辑项目',
          component: CreateProject,
          hidden: true,
          meta: {
            keepAlive: false
          }
        }
      ]
    }, {
      path: '/home',
      component: Home,
      name: '用户管理',
      children: [
        {
          path: '/user',
          iconCls: 'fa fa-user-o',
          name: '用户管理',
          component: UserMana
        }
      ]
    }, {
      path: '/home',
      component: Home,
      name: '专题管理',
      children: [
        {
          path: '/cateMana',
          iconCls: 'fa fa-reorder',
          name: '专题管理',
          component: CateMana
        }
      ]
    }, {
      path: '/home',
      component: Home,
      name: '数据统计',
      iconCls: 'fa fa-bar-chart',
      children: [
        {
          path: '/charts',
          iconCls: 'fa fa-bar-chart',
          name: '数据统计',
          component: DataCharts
        }
      ]
    }
  ]
})
