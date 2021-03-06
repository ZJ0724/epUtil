import Vue from "vue/dist/vue.esm.js";

export default {
    error(message) {
        Vue.prototype.$pop({
            type: "danger",
            message: message
        });
    },

    info(message) {
        Vue.prototype.$pop({
            type: "info",
            message: message
        });
    },

    success(message) {
        Vue.prototype.$pop({
            type: "success",
            message: message
        });
    },

    popup(message) {
        Vue.prototype.$notify({
            title: "提示",
            type: "success",
            message: message,
            duration: 0
        });
    },

    confirm(message) {
        return new Promise((s) => {
            Vue.prototype.$confirm(message, "提示", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning"
            }).then(() => {
                s();
            }).catch(() => {});
        });
    }
};