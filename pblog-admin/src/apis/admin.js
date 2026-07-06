// @/apis/user.js
import http from '@/utils/http.js';

const AdminApi = {
    passwordLogin: (params) => {
        return http({
            url: '/admin/passwordLogin',
            method: 'POST',
            data: params
        });
    },


    register: (params) => {
        const reponse = http({
            url: '/admin/register',
            method: 'POST',
            data: params
        });
        return reponse;
    },

    logout: () => {
        return http({
            url: '/admin/logout',
            method: 'POST',
        });
    },

    enable: (params) => {
        return http({
            url: '/admin/status',
            method: 'POST',
            data: params
        });
    },

    UserPageQuery: (params) => {
        return http({
            url: '/admin/UserPageQuery',
            method: 'POST',
            data: params
        });
    },



    updateInfo: (params) => {
        return http({
            url: '/admin/updateInfo',
            method: 'PUT',
            data: params
        });
    },






};

export default AdminApi;