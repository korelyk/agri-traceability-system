import axios from 'axios';

const API_BASE_URL = uni.getStorageSync('apiBaseUrl') || 'https://trace2026.yyy999.my';

const service = axios.create({
    baseURL: API_BASE_URL,
    timeout: 50000,
});

axios.defaults.adapter = function (config) {
    return new Promise((resolve, reject) => {
        uni.request({
            url: config.baseURL + config.url,
            method: config.method.toUpperCase(),
            data: config.data || config.params,
            header: config.headers,
            success: (res) => {
                resolve({
                    data: res.data,
                    status: res.statusCode,
                    statusText: 'OK',
                    headers: res.header,
                    config: config
                });
            },
            fail: (err) => {
                reject(err);
            }
        });
    });
};

service.interceptors.request.use(
    config => {
        const token = uni.getStorageSync('token');
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        return config;
    },
    error => Promise.reject(error)
);

service.interceptors.response.use(
    response => response.data,
    error => {
        uni.showToast({
            title: error.message || 'Request failed',
            icon: 'none',
            duration: 2000
        });
        return Promise.reject(error);
    }
);

export default service;
