import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '系统总览', icon: 'Odometer' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('../views/Products.vue'),
        meta: { title: '产品管理', icon: 'Goods' }
      },
      {
        path: 'products/register',
        name: 'ProductRegister',
        component: () => import('../views/ProductRegister.vue'),
        meta: { title: '产品建档', hidden: true }
      },
      {
        path: 'trace',
        name: 'Trace',
        component: () => import('../views/Trace.vue'),
        meta: { title: '溯源查询', icon: 'Search' }
      },
      {
        path: 'trace/add',
        name: 'TraceAdd',
        component: () => import('../views/TraceAdd.vue'),
        meta: { title: '新增溯源记录', hidden: true }
      },
      {
        path: 'trace/detail/:productId',
        name: 'TraceDetail',
        component: () => import('../views/TraceDetail.vue'),
        meta: { title: '溯源详情', hidden: true }
      },
      {
        path: 'verify/transaction/:transactionId',
        name: 'SignatureDetail',
        component: () => import('../views/SignatureDetail.vue'),
        meta: { title: '签名验签详情', hidden: true }
      },
      {
        path: 'blockchain',
        name: 'Blockchain',
        component: () => import('../views/Blockchain.vue'),
        meta: { title: '区块链浏览器', icon: 'Link' }
      },
      {
        path: 'supervision',
        name: 'Supervision',
        component: () => import('../views/Supervision.vue'),
        meta: { title: '质量监管', icon: 'DataAnalysis' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/Users.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/Statistics.vue'),
        meta: { title: '统计分析', icon: 'TrendCharts' }
      }
    ]
  },
  {
    path: '/public-trace',
    name: 'PublicTraceSearch',
    component: () => import('../views/PublicTrace.vue'),
    meta: { public: true, title: '产品溯源查询' }
  },
  {
    path: '/public-trace/:productId',
    name: 'PublicTrace',
    component: () => import('../views/PublicTrace.vue'),
    meta: { public: true, title: '产品溯源查询' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const hash = window.location.hash || ''
  if (hash.startsWith('#/public-trace')) {
    const raw = hash.slice(1)
    const [legacyPath, queryString = ''] = raw.split('?')
    if (legacyPath.startsWith('/public-trace/')) {
      return next({ path: legacyPath, replace: true })
    }
    const params = new URLSearchParams(queryString)
    const productId = params.get('productId')
    if (productId) {
      return next({ path: `/public-trace/${productId}`, replace: true })
    }
  }

  const token = localStorage.getItem('token')
  if (to.meta.public) {
    next()
  } else if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
