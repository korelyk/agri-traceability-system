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
                meta: { title: '系统概览', icon: 'Odometer' }
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
                meta: { title: '产品注册', hidden: true }
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
                meta: { title: '添加溯源记录', hidden: true }
            },
            {
                path: 'trace/detail/:productId',
                name: 'TraceDetail',
                component: () => import('../views/TraceDetail.vue'),
                meta: { title: '溯源详情', hidden: true }
            },
            {
                path: 'blockchain',
                name: 'Blockchain',
                component: () => import('../views/Blockchain.vue'),
                meta: { title: '区块链浏览器', icon: 'Link' }
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

// 路由守卫
router.beforeEach((to, from, next) => {
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
