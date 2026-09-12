<template>
  <div class="personal-info-container">
    <!-- ================= 1. 展示区域 ================= -->
    <!-- 头部区域：大头像与基本操作 -->
    <div class="profile-header">
      <div class="avatar-box" @click="openEditDialog">
        <el-avatar :size="100" :src="userInfo.avatarUrl " class="user-avatar" />
        <!-- 悬浮显示的编辑提示 -->
        <div class="avatar-hover-mask">
          <el-icon><CameraFilled /></el-icon>
        </div>
      </div>

      <div class="header-info">
        <div class="name-row">
          <h2 class="nickname">{{ userInfo.nickname }}</h2>
          <el-tag size="small" effect="plain" round class="uid-tag">UID: {{ userInfo.username }}</el-tag>
        </div>
        <p class="bio">{{ userInfo.bio || '这个人很懒，什么都没有写...' }}</p>
      </div>

      <el-button type="primary" plain round class="edit-base-btn" @click="openEditDialog">
        <el-icon class="mr-1"><Edit /></el-icon> 编辑资料
      </el-button>
    </div>

    <el-divider border-style="dashed" />

    <!-- 详细信息列表 -->
    <div class="info-list">
      <!-- 账号 (只读) -->
      <div class="info-item">
        <div class="label">
          <el-icon><User /></el-icon> 登录账号
        </div>
        <div class="value">{{ userInfo.username }}</div>
        <div class="action-placeholder"></div> <!-- 占位，保持对齐 -->
      </div>

      <!-- 邮箱 (带换绑按钮) -->
      <div class="info-item">
        <div class="label">
          <el-icon><Message /></el-icon> 绑定邮箱
        </div>
        <div class="value">{{ email || '未绑定邮箱' }}</div>
        <div class="action">
          <el-button link type="primary" @click="handleChangeEmail">
            换绑邮箱
          </el-button>
        </div>
      </div>

      <!-- 密码 (隐藏值，带重置按钮) -->
      <div class="info-item">
        <div class="label">
          <el-icon><Lock /></el-icon> 登录密码
        </div>
        <div class="value password-mask">
          ●●●●●●●●●●
          <span class="safe-tip">已设置</span>
        </div>
        <div class="action">
          <el-button link type="danger" @click="handleResetPassword">
            修改密码
          </el-button>
        </div>
      </div>
    </div>

    <!-- ================= 2. 编辑弹窗 ================= -->
    <el-dialog
        v-model="dialogVisible"
        title="编辑个人资料"
        width="500px"
        :close-on-click-modal="false"
        class="profile-dialog"
        align-center
    >
      <el-form
          ref="formRef"
          :model="editForm"
          :rules="rules"
          label-width="80px"
          label-position="left"
      >
        <!-- 头像上传区域 -->
        <div class="dialog-avatar-wrapper">
          <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              :on-change="handleAvatarChange"
          >
            <div v-if="editForm.avatarUrl" class="avatar-preview">
              <img :src="editForm.avatarUrl" class="avatar" />
              <div class="edit-mask"><el-icon><Plus /></el-icon></div>
            </div>
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="avatar-tip">点击头像更换图片（支持jpg/png格式，大小不超过5MB）</div>
        </div>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
        </el-form-item>

        <el-form-item label="个人简介" prop="bio">
          <el-input
              v-model="editForm.bio"
              type="textarea"
              :rows="3"
              placeholder="介绍一下你自己吧..."
              maxlength="100"
              show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSaveProfile">
            保存修改
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
        v-model="passwordDialogVisible"
        title="修改登录密码"
        width="460px"
        :close-on-click-modal="false"
        align-center
        class="profile-dialog"
    >
      <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="90px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              autocomplete="new-password"
              show-password
              placeholder="请输入6到64位新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              autocomplete="new-password"
              show-password
              placeholder="请再次输入新密码"
              @keyup.enter="handleChangePassword"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            :loading="passwordSaving"
            @click="handleChangePassword"
        >
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { CameraFilled, Edit, User, Message, Lock, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getUserInfo,
  checkLogin,
  logout,
  saveUserInfoJson,
  showLogin
} from '@/utils/loginManager.js'

import UserApi from '@/apis/user/user.js'
import { uploadAvatar, uploadImage } from '@/apis/file.js'

// --- 1. 核心数据 ---
interface UserInfo {
  avatarUrl: string
  nickname: string
  username: string
  bio: string | null
}

const email = ref('')
const userInfo = ref<UserInfo>({
  avatarUrl: '',
  nickname: 'CodeMaster',
  username: '1234567890',
  bio: '保持热爱，奔赴山海。全栈开发练习生。',
})

// --- 2. 弹窗相关逻辑 ---
const isLoading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<any>(null)
const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref<any>(null)

// 编辑表单数据
const localImage = ref<File | null>(null)
const editForm = reactive({
  avatarUrl: '',
  nickname: '',
  bio: '',
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

// 表单校验规则
const rules = {
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
  ],
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 64, message: '新密码长度必须为6到64位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: ['blur', 'change'] }
  ]
}

// 打开弹窗
const openEditDialog = () => {
  // 数据回显
  editForm.avatarUrl = userInfo.value.avatarUrl
  editForm.nickname = userInfo.value.nickname
  editForm.bio = userInfo.value.bio || ''

  dialogVisible.value = true
}

// --- 业务逻辑 ---
// 处理头像选择（纯前端预览，不上传服务器）
const handleAvatarChange = (uploadFile: any) => {
  const file = uploadFile.raw
  if (!file) return

  // 校验文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片格式的文件（jpg/png）');
    return;
  }

  // 校验文件大小（5MB限制）
  const maxSize = 5 * 1024 * 1024;
  if (file.size > maxSize) {
    ElMessage.error(`${file.name} 大小超过5MB，请压缩后上传`);
    return;
  }

  // 释放旧的预览 URL（避免内存泄漏）
  if (editForm.avatarUrl && editForm.avatarUrl.startsWith('blob:')) {
    URL.revokeObjectURL(editForm.avatarUrl);
  }

  localImage.value = file;
  // 创建新的预览URL
  editForm.avatarUrl = URL.createObjectURL(file);
}

// 上传头像到服务器
const uploadCoverImg = async (file: File) => {
  try {
    // 校验文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.warning(`${file.name} 不是图片格式，跳过上传`);
      return null;
    }

    // 校验文件大小（5MB限制）
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      ElMessage.error(`${file.name} 大小超过5MB，请压缩后上传`);
      return null;
    }

    // 上传图片
    const res = await uploadAvatar(file);
    // {code:200,data:url,message:"操作成功"}
    if (res.code === 200 && res.data?.url) {
      // 释放临时URL
      if (editForm.avatarUrl && editForm.avatarUrl.startsWith('blob:')) {
        URL.revokeObjectURL(editForm.avatarUrl);
      }
      return res.data.url;
    } else {
      ElMessage.error(`${file.name} 上传失败：${res.message || '接口返回异常'}`);
      return null;
    }
  } catch (error) {
    console.error('图片上传失败:', error);
    ElMessage.error('图片上传失败，请检查网络或联系管理员');
    return null;
  }
};

// 保存修改
// TODO 修改了头像上传bug
const handleSaveProfile = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    const valid = await formRef.value.validate()
    if (!valid) return

    saving.value = true

    const updateData: {
      nickname: string;
      bio: string;
    } = {
      nickname: editForm.nickname,
      bio: editForm.bio
    }

    // 如果有新头像，先上传并获取真实URL
    let newAvatarUrl = editForm.avatarUrl // 默认用原来的URL
    if (localImage.value) {
      // 上传头像的同时会修改user表
      const uploadRes = await uploadCoverImg(localImage.value)
      if (uploadRes) {
        newAvatarUrl = uploadRes // 替换为服务器返回的真实URL
        editForm.avatarUrl = newAvatarUrl // 同步更新表单里的URL
      } else {
        ElMessage.error("头像上传失败")
        saving.value = false
        return // 上传失败则终止提交
      }
    }

    // 调用更新用户信息接口
    const res = await UserApi.updateInfo(updateData)
    if (res.data && res.data.code === 200) {
      // 更新本地数据（使用真实URL）
      userInfo.value.nickname = editForm.nickname
      userInfo.value.bio = editForm.bio
      userInfo.value.avatarUrl = newAvatarUrl // 关键：用真实URL更新
      // 保存到本地
      saveUserInfoJson(res.data.data.userInfoJson)
      ElMessage.success('个人资料修改成功')
      dialogVisible.value = false
      // 清空本地图片缓存
      localImage.value = null
    } else {
      ElMessage.error(res.data.message || '修改失败，请重试')
    }
  } catch (error) {
    ElMessage.error('保存失败，请检查网络或联系管理员')
  } finally {
    saving.value = false
  }
}

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  isLoading.value = true
  try {
    if (!checkLogin()) {
      ElMessage.error("请先登录")
      return
    }

    // 从本地缓存获取用户信息
    const localUserInfo = getUserInfo()
    if (localUserInfo) {
      userInfo.value = {
        ...userInfo.value,
        ...localUserInfo
      }
    }

    // 获取最新的邮箱信息
    const res = await UserApi.getEmail()
    if (res.data && res.data.code === 200) {
      email.value = res.data.data
    } else {
      ElMessage.warning("获取邮箱失败")
    }
  } catch (e) {
    ElMessage.error("获取用户信息失败")
  } finally {
    isLoading.value = false
  }
}


// TODO 换绑邮箱
const handleChangeEmail = () => {
  // 这里可以跳转到邮箱换绑页面或打开邮箱换绑弹窗
  ElMessage.warning('跳转到邮箱验证流程...')
}

const handleResetPassword = () => {
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
  requestAnimationFrame(() => passwordFormRef.value?.clearValidate())
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value || passwordSaving.value) return
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordSaving.value = true
  try {
    const res = await UserApi.changePassword({
      newPassword: passwordForm.newPassword
    })
    if (res.data?.code !== 200) {
      ElMessage.error(res.data?.message || '密码修改失败')
      return
    }

    passwordDialogVisible.value = false
    logout()
    ElMessage.success('密码修改成功，请重新登录')
    setTimeout(() => showLogin(), 300)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '密码修改失败，请重试')
  } finally {
    passwordSaving.value = false
  }
}

// 组件挂载时加载用户信息
onMounted(() => {
  loadUserInfo()
})

// 组件卸载时清理资源，防止内存泄漏
onUnmounted(() => {
  // 释放头像预览的blob URL
  if (editForm.avatarUrl && editForm.avatarUrl.startsWith('blob:')) {
    URL.revokeObjectURL(editForm.avatarUrl);
  }
  // 清空本地图片引用
  localImage.value = null;
})
</script>

<style scoped lang="scss">
.personal-info-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

/* 头部样式 */
.profile-header {
  display: flex;
  align-items: center;
  padding-bottom: 20px;
  flex-wrap: wrap;
  gap: 20px;

  .avatar-box {
    position: relative;
    width: 100px;
    height: 100px;
    cursor: pointer;
    border-radius: 50%;
    border: 4px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);

    .user-avatar {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .avatar-hover-mask {
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 24px;
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover .avatar-hover-mask {
      opacity: 1;
    }
  }

  .header-info {
    flex: 1;
    min-width: 200px;

    .name-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      .nickname {
        margin: 0;
        font-size: 24px;
        font-weight: 700;
        color: #303133;
      }

      .uid-tag {
        font-family: monospace;
      }
    }

    .bio {
      margin: 0;
      color: #909399;
      font-size: 14px;
      line-height: 1.5;
    }
  }

  .edit-base-btn {
    white-space: nowrap;
  }
}

/* 列表样式 */
.info-list {
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  transition: all 0.3s;
  border: 1px solid transparent;
  flex-wrap: wrap;
  gap: 10px;

  &:hover {
    background: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    border-color: #ebeef5;
  }

  .label {
    min-width: 120px;
    color: #606266;
    display: flex;
    align-items: center;
    font-size: 15px;

    .el-icon {
      margin-right: 8px;
      font-size: 18px;
      color: #909399;
    }
  }

  .value {
    flex: 1;
    color: #303133;
    font-size: 15px;
    font-weight: 500;

    &.password-mask {
      letter-spacing: 2px;
      color: #909399;
      font-size: 12px;
      display: flex;
      align-items: center;

      .safe-tip {
        margin-left: 10px;
        font-size: 12px;
        color: #67c23a;
        background: #f0f9eb;
        padding: 2px 6px;
        border-radius: 4px;
        letter-spacing: 0;
      }
    }
  }

  .action {
    margin-left: 20px;
    white-space: nowrap;
  }

  .action-placeholder {
    width: 0;
  }
}

/* --- 弹窗样式 --- */
.dialog-avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 25px;

  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 50%;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
      width: 100px;
      height: 100px;

      &:hover {
        border-color: var(--el-color-primary);
      }
    }
  }

  .avatar-preview {
    width: 100%;
    height: 100%;
    position: relative;

    .avatar {
      width: 100%;
      height: 100%;
      display: block;
      object-fit: cover;
      border-radius: 50%;
    }

    .edit-mask {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0,0,0,0.4);
      display: flex;
      justify-content: center;
      align-items: center;
      color: #fff;
      font-size: 20px;
      opacity: 0;
      transition: opacity 0.3s;
      border-radius: 50%;
    }

    &:hover .edit-mask {
      opacity: 1;
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    text-align: center;
    line-height: 100px;
  }

  .avatar-tip {
    margin-top: 10px;
    font-size: 12px;
    color: #909399;
  }
}

.mr-1 {
  margin-right: 4px;
}

// 响应式调整
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: flex-start;

    .header-info {
      width: 100%;
      margin-bottom: 16px;
    }

    .edit-base-btn {
      align-self: flex-end;
    }
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;

    .label {
      min-width: auto;
      width: 100%;
      margin-bottom: 8px;
    }

    .value {
      width: 100%;
      margin-bottom: 8px;
    }

    .action {
      margin-left: 0;
      align-self: flex-end;
    }
  }
}
</style>

<style scoped lang="scss">
.personal-info-container {
  max-width: 980px;
  margin: 0 auto;
  padding: 0;
  color: var(--geo-navy, #1a1a2e);
}

.profile-header {
  display: grid;
  grid-template-columns: 116px minmax(0, 1fr) auto;
  padding: 28px;
  align-items: center;
  gap: 24px;
  background: var(--geo-sky-light, #dff5ff);
  border: 2px solid var(--geo-navy, #1a1a2e);
  box-shadow: 7px 7px 0 var(--geo-navy, #1a1a2e);

  .avatar-box {
    width: 104px;
    height: 104px;
    border: 3px solid var(--geo-navy, #1a1a2e);
    border-radius: 0;
    box-shadow: 6px 6px 0 var(--geo-gold, #ffd54f);
    transform: rotate(-2deg);
    overflow: hidden;

    .user-avatar { border-radius: 0; transform: rotate(2deg) scale(1.05); }
    .avatar-hover-mask { border-radius: 0; background: rgba(26, 26, 46, .72); }
  }

  .header-info {
    min-width: 0;

    .name-row {
      flex-wrap: wrap;
      gap: 10px;

      .nickname { color: var(--geo-navy, #1a1a2e); font-size: clamp(25px, 3vw, 36px); font-weight: 950; letter-spacing: -.04em; }
      .uid-tag { height: auto; padding: 4px 8px; color: var(--geo-navy, #1a1a2e); font-weight: 800; background: #fff; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; }
    }

    .bio { max-width: 520px; color: var(--color-text-secondary, #555); font-size: 14px; line-height: 1.7; }
  }

  .edit-base-btn {
    height: 42px;
    padding: 0 14px;
    color: var(--geo-navy, #1a1a2e);
    font-weight: 900;
    background: #fff;
    border: 2px solid var(--geo-navy, #1a1a2e);
    border-radius: 0;
    box-shadow: 4px 4px 0 var(--geo-coral, #ff6b6b);

    &:hover { color: var(--geo-navy, #1a1a2e); background: var(--geo-gold-light, #fff5c2); transform: translate(-2px, -2px); }
  }
}

:deep(.el-divider) { display: none; }

.info-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  padding: 34px 0 0;
  gap: 18px;
}

.info-item {
  position: relative;
  display: flex;
  min-height: 172px;
  padding: 48px 20px 18px;
  box-sizing: border-box;
  align-items: flex-start;
  flex-direction: column;
  gap: 14px;
  background: #fff;
  border: 2px solid var(--geo-navy, #1a1a2e);
  border-radius: 0;
  box-shadow: 5px 5px 0 var(--geo-navy, #1a1a2e);

  &::before {
    position: absolute;
    top: 0;
    left: 0;
    display: grid;
    width: 38px;
    height: 30px;
    place-items: center;
    color: #fff;
    font-size: 10px;
    font-weight: 950;
    background: var(--geo-navy, #1a1a2e);
    content: 'A';
  }
  &:nth-child(2)::before { color: var(--geo-navy, #1a1a2e); background: var(--geo-gold, #ffd54f); content: 'B'; }
  &:nth-child(3)::before { color: var(--geo-navy, #1a1a2e); background: var(--geo-coral, #ff6b6b); content: 'C'; }
  &:hover { background: var(--geo-gold-light, #fff5c2); border-color: var(--geo-navy, #1a1a2e); box-shadow: 8px 8px 0 var(--geo-navy, #1a1a2e); transform: translate(-3px, -3px); }

  .label {
    width: auto;
    min-width: 0;
    color: var(--geo-navy, #1a1a2e);
    font-size: 12px;
    font-weight: 900;
    letter-spacing: .04em;

    .el-icon { color: var(--geo-navy, #1a1a2e); }
  }

  .value {
    width: 100%;
    overflow: hidden;
    color: var(--geo-navy, #1a1a2e);
    font-size: 15px;
    font-weight: 800;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.password-mask {
      width: 100%;
      color: var(--color-text-muted, #777);
      flex-wrap: wrap;

      .safe-tip { color: var(--geo-navy, #1a1a2e); font-weight: 800; background: var(--geo-sky-light, #dff5ff); border: 1px solid var(--geo-navy, #1a1a2e); border-radius: 0; }
    }
  }

  .action { margin: auto 0 0; align-self: flex-start; }
  .action :deep(.el-button) { height: 30px; padding: 0 9px; color: var(--geo-navy, #1a1a2e); font-size: 12px; font-weight: 900; background: #fff; border: 1px solid var(--geo-navy, #1a1a2e); }
}

.dialog-avatar-wrapper {
  .avatar-uploader :deep(.el-upload) { border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 5px 5px 0 var(--geo-gold, #ffd54f); }
  .avatar-preview .avatar, .avatar-preview .edit-mask { border-radius: 0; }
}

@media (max-width: 1050px) {
  .info-list { grid-template-columns: 1fr; }
  .info-item { min-height: 130px; }
}

@media (max-width: 680px) {
  .profile-header { grid-template-columns: 82px 1fr; padding: 20px; gap: 18px; }
  .profile-header .avatar-box { width: 74px; height: 74px; }
  .profile-header .edit-base-btn { grid-column: 1 / -1; width: 100%; margin-top: 2px; }
  .info-list { padding-top: 24px; }
}
</style>

<style lang="scss">
.profile-dialog {
  border: 3px solid var(--geo-navy, #1a1a2e) !important;
  border-radius: 0 !important;
  box-shadow: 9px 9px 0 var(--geo-navy, #1a1a2e) !important;
}
.profile-dialog .el-dialog__header { padding: 19px 22px; background: var(--geo-gold-light, #fff5c2); border-bottom: 2px solid var(--geo-navy, #1a1a2e); }
.profile-dialog .el-dialog__title { color: var(--geo-navy, #1a1a2e); font-weight: 950; }
.profile-dialog .el-dialog__body { padding: 24px; }
.profile-dialog .el-dialog__footer { padding: 16px 24px 22px; }
.profile-dialog .el-input__wrapper,
.profile-dialog .el-textarea__inner { border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: none; }
.profile-dialog .el-button { color: var(--geo-navy, #1a1a2e); font-weight: 850; background: #fff; border: 2px solid var(--geo-navy, #1a1a2e); border-radius: 0; box-shadow: 3px 3px 0 var(--geo-coral, #ff6b6b); }
.profile-dialog .el-button--primary { background: var(--geo-gold, #ffd54f); box-shadow: 3px 3px 0 var(--geo-navy, #1a1a2e); }
@media (max-width: 560px) { .profile-dialog { width: calc(100% - 24px) !important; } }
</style>
