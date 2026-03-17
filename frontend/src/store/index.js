import { createStore } from 'vuex'
import request from '../utils/request'

export default createStore({
  state: {
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: localStorage.getItem('token') || null,
    statistics: null,
    blockchainInfo: null
  },

  getters: {
    isLoggedIn: (state) => !!state.token,
    currentUser: (state) => state.user,
    isAdmin: (state) => state.user && state.user.role === 'ADMIN'
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
    async login({ commit }, credentials) {
      try {
        const result = await request.post('/users/login', credentials)
        if (result.success) {
          commit('SET_USER', result.data.user)
          commit('SET_TOKEN', result.data.token)
          return { success: true }
        }
        return { success: false, message: result.message }
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '登录失败' }
      }
    },

    async register(_, userData) {
      try {
        return await request.post('/users/register', userData)
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '注册失败' }
      }
    },

    logout({ commit }) {
      commit('CLEAR_AUTH')
    },

    async fetchStatistics({ commit }) {
      try {
        const result = await request.get('/statistics')
        if (result.success) {
          commit('SET_STATISTICS', result.data)
        }
        return result
      } catch (error) {
        return { success: false }
      }
    },

    async fetchBlockchainInfo({ commit }) {
      try {
        const result = await request.get('/blockchain/statistics')
        if (result.success) {
          commit('SET_BLOCKCHAIN_INFO', result.data)
        }
        return result
      } catch (error) {
        return { success: false }
      }
    },

    async registerProduct(_, productData) {
      try {
        return await request.post('/products/register', productData)
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '注册失败' }
      }
    },

    async addTraceRecord(_, recordData) {
      try {
        return await request.post('/trace/add', recordData)
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '添加失败' }
      }
    },

    async traceProduct(_, productId) {
      try {
        return await request.get(`/trace/${productId}`)
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '查询失败' }
      }
    },

    async fetchUsers() {
      try {
        return await request.get('/users')
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '获取用户失败' }
      }
    },

    async fetchUsersByType(_, userType) {
      try {
        return await request.get(`/users/type/${userType}`)
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '获取用户失败' }
      }
    },

    async fetchProducts() {
      try {
        return await request.get('/products')
      } catch (error) {
        return { success: false, message: error.response?.data?.message || '获取产品失败' }
      }
    },

    async verifyBlock(_, blockHash) {
      try {
        return await request.get(`/verify/block/${blockHash}`)
      } catch (error) {
        return { success: false }
      }
    },

    async verifyTransaction(_, transactionId) {
      try {
        return await request.get(`/verify/transaction/${transactionId}`)
      } catch (error) {
        return { success: false }
      }
    }
  }
})
