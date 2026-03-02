import axios from 'axios';

// Create axios instance
const service = axios.create({
    baseURL: 'http://38.55.34.56:8080', // the backend runs at port 8080 of the server
    timeout: 50000,
});

// Since uni-app interceptors handle requests slightly differently in mini-programs, we use a custom adapter for uni.request if needed.
// However, axios usually works fine with standard H5. For uni-app native/mini-program we use an adapter.
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
    error => {
        return Promise.reject(error);
    }
);

service.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.success === false) {
            uni.showToast({
                title: res.message || 'Error',
                icon: 'none',
                duration: 2000
            });
            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            return res;
        }
    },
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
