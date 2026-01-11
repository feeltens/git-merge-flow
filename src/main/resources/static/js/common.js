/**
 * 公共 JS - 全局配置
 * @author feeltens
 * @date 2026-01-11
 */

// axios 全局拦截器配置
(function() {
    if (typeof axios !== 'undefined') {
        // 请求拦截器 - 添加 token
        axios.interceptors.request.use(function(config) {
            var token = localStorage.getItem('satoken');
            if (token) {
                config.headers['satoken'] = token;
            }
            return config;
        });

        // 响应拦截器 - 处理 401 未登录
        axios.interceptors.response.use(
            function(response) { return response; },
            function(error) {
                if (error.response && error.response.status === 401) {
                    localStorage.clear();
                    // 使用 Element UI 提示（如果可用）
                    if (typeof Vue !== 'undefined' && Vue.prototype.$message) {
                        Vue.prototype.$message.warning('登录已过期，请重新登录');
                    }
                    setTimeout(function() {
                        window.location.href = '/login';
                    }, 500);
                }
                return Promise.reject(error);
            }
        );
    }

    // jQuery Ajax 全局错误处理（如果使用 jQuery）
    if (typeof $ !== 'undefined' && $.ajaxError) {
        $(document).ajaxError(function(event, jqXHR) {
            if (jqXHR.status === 401) {
                localStorage.clear();
                window.location.href = '/login';
            }
        });
    }
})();