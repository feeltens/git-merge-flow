/**
 * 通用认证相关JavaScript
 * 用于所有页面的登录检查和用户菜单
 */

// axios拦截器配置
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('satoken');
    if (token) {
        config.headers['satoken'] = token;
    }
    return config;
});

axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.clear();
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

/**
 * 检查登录状态
 * @returns {boolean} 是否已登录
 */
function checkLogin() {
    const token = localStorage.getItem('satoken');
    if (!token) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

/**
 * 获取当前用户信息
 * @returns {Object} 用户信息
 */
function getCurrentUser() {
    return {
        username: localStorage.getItem('username') || '',
        nickname: localStorage.getItem('nickname') || localStorage.getItem('operator') || '',
        isAdmin: localStorage.getItem('isAdmin') === 'true'
    };
}

/**
 * 退出登录
 */
function logout() {
    axios.post('/auth/logout').finally(() => {
        localStorage.clear();
        window.location.href = '/login';
    });
}

/**
 * 获取操作人
 * @returns {string} 操作人名称
 */
function getOperator() {
    return localStorage.getItem('operator') || localStorage.getItem('nickname') || localStorage.getItem('username') || '';
}