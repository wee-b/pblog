// @/apis/user.js
import http from '@/utils/http.js';

const UserApi = {
  passwordLogin: (params) => {
    return http({
      url: '/user/passwordLogin',
      method: 'POST',
      data: params
    });
  },


  emailCodeLogin: (params) => {
    const reponse = http({
      url: '/user/emailLoginOrRegister',
      method: 'POST',
      data: params
    });
    return reponse;
  },

  logout: () => {
    return http({
      url: '/user/logout',
      method: 'POST',
    });
  },

  updateInfo: (params) => {
    return http({
      url: '/user/updateInfo',
      method: 'PUT',
      data: params
    });
  },

  updateEmail: (params) => {
    return http({
      url: '/user/updateEmail',
      method: 'PUT',
      data: params
    });
  },

  forgetPassword: (newPassword) => {
    return http({
      url: '/user/forgetPassword',
      method: 'PUT',
      params: { newPassword }
    });
  },

  deleteAccount: (token) => {
    return http({
      url: '/user/deleteAccount',
      method: 'DELETE',
      headers: { token }
    });
  },

  getUserInfo: (token) => {
    return http({
      url: '/user/getUserInfo',
      method: 'GET',
      headers: { token }
    });
  },
  getEmail: () => {
    return http({
      url: '/user/getEmail',
      method: 'GET',
    });
  },

  getUserInfoByUserName: (username) => {
    return http({
      url: '/user/getUserInfoByUserName',
      method: 'GET',
      params:{'username':username}
    });
  }
};

export default UserApi;