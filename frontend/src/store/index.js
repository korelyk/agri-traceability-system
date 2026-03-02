import { createStore } from 'vuex'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

export default createStore({
    state: {
        user: JSON.parse(localStorage.getItem('user') || 'null'),
        token: localStorage.getItem('token') || null,
        statistics: null,
        blockchainInfo: null
    },
    
    getters: {
        isLoggedIn: state => !!state.token,
        currentUser: state => state.user,
        isAdmin: state => state.user && state.user.role === 'ADMIN'
    },
    
    mutations: {
        SET_USER(state, user) {
            state.user = user
            localStorage.setItem('user', JSON.stringify(user))
        },
        SET_TOKEN(state, token) {
            state.token = token
            localStorage.setItem('token', token)
        },
        CLEAR_AUTH(state) {
            state.user = null
            state.token = null
            localStorage.removeItem('user')
            localStorage.removeItem('token')
        },
        SET_STATISTICS(state, statistics) {
            state.statistics = statistics
        },
        SET_BLOCKCHAIN_INFO(state, info) {
            state.blockchainInfo = info
        }
    },
    
    actions: {
        // 用户登录
        async login({ commit }, credentials) {
            try {
                const response = await axios.post(`${API_BASE_URL}/users/login`, credentials)
                if (response.data.success) {
                    commit('SET_USER', response.data.data)
                    commit('SET_TOKEN', 'mock-token-' + Date.now())
                    return { success: true }
                }
                return { success: false, message: response.data.message }
            } catch (error) {
                return { success: false, message: error.response?.data?.message || '登录失败' }
            }
        },
        
        // 用户注册
        async register(_, userData) {
            try {
                const response = await axios.post(`${API_BASE_URL}/users/register`, userData)
                return response.data
            } catch (error) {
                return { success: false, message: error.response?.data?.message || '注册失败' }
            }
        },
        
        // 退出登录
        logout({ commit }) {
            commit('CLEAR_AUTH')
        },
        
        // 获取统计信息
        async fetchStatistics({ commit }) {
            try {
                const response = await axios.get(`${API_BASE_URL}/statistics`)
                if (response.data.success) {
                    commit('SET_STATISTICS', response.data.data)
                }
                return response.data
            } catch (error) {
                return { success: false }
            }
        },
        
        // 获取区块链统计
        async fetchBlockchainInfo({ commit }) {
            try {
                const response = await axios.get(`${API_BASE_URL}/blockchain/statistics`)
                if (response.data.success) {
                    commit('SET_BLOCKCHAIN_INFO', response.data.data)
                }
                return response.data
            } catch (error) {
                return { success: false }
            }
        },
        
        // 注册产品
        async registerProduct(_, productData) {
            try {
                const response = await axios.post(`${API_BASE_URL}/products/register`, productData)
                return response.data
            } catch (error) {
                return { success: false, message: error.response?.data?.message || '注册失败' }
            }
        },
        
        // 添加溯源记录
        async addTraceRecord(_, recordData) {
            try {
                const response = await axios.post(`${API_BASE_URL}/trace/add`, recordData)
                return response.data
            } catch (error) {
                return { success: false, message: error.response?.data?.message || '添加失败' }
            }
        },
        
        // 查询溯源信息
        async traceProduct(_, productId) {
            try {
                const response = await axios.get(`${API_BASE_URL}/trace/${productId}`)
                return response.data
            } catch (error) {
                return { success: false, message: error.response?.data?.message || '查询失败' }
            }
        },
        
        // 获取所有用户
        async fetchUsers() {
            try {
                const response = await axios.get(`${API_BASE_URL}/users`)
                return response.data
            } catch (error) {
                return { success: false }
            }
        },
        
        // 验证区块
        async verifyBlock(_, blockHash) {
            try {
                const response = await axios.get(`${API_BASE_URL}/verify/block/${blockHash}`)
                return response.data
            } catch (error) {
                return { success: false }
            }
        },
        
        // 验证交易
        async verifyTransaction(_, transactionId) {
            try {
                const response = await axios.get(`${API_BASE_URL}/verify/transaction/${transactionId}`)
                return response.data
            } catch (error) {
                return { success: false }
            }
        }
    }
})
