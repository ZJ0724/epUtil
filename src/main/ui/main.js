import baseRouter from "./common/routerCommon.js";

(function () {

    // 设置路由监听
    window.onhashchange = function () {
        baseRouter.load();
    }

    // 页面初始化判断路由
    baseRouter.load();

})();