<template>
  <button
      class="like-btn"
      :class="{ liked: isLiked }"
      @click="handleLike"
      :disabled="isLoading"
  >
    <span v-if="isLoading" class="loading-icon">⏳</span>
    <span v-else class="like-icon">{{ isLiked ? '❤️' : '♡' }}</span>
    <span class="like-count">{{ likeCount }}</span>
  </button>
</template>

<script>
// 引入业务依赖
import { likeUnlike, isLiked, getLikeCount } from '@/apis/like.js';
import { checkLogin } from '@/utils/loginManager.js';
import { ElMessage } from "element-plus";

export default {
  name: 'LikeButton',
  props: {
    // 核心参数（必传，支持数字/字符串类型）
    targetId: {
      type: [Number, String],
      required: true,
      validator: (val) => val !== '' && val !== null && val !== undefined
    },
    targetType: {
      type: [Number, String],
      required: true,
      validator: (val) => val !== '' && val !== null && val !== undefined
    }
  },
  data() {
    return {
      isLiked: false,       // 当前用户是否点赞
      likeCount: 0,         // 总点赞数
      isLoading: false,     // 操作加载状态
      initLoaded: false     // 标记初始化查询是否完成
    };
  },
  watch: {
    // 监听核心参数变化，参数完整时执行初始化
    targetId: {
      handler() {
        this.initLikeData();
      },
      immediate: true
    },
    targetType: {
      handler() {
        this.initLikeData();
      },
      immediate: true
    }
  },
  methods: {
    /**
     * 初始化点赞数据：先查总数，再查是否点赞
     * 确保仅在参数完整且未初始化时执行
     */
    async initLikeData() {
      // 防重复初始化 + 参数完整性校验
      if (this.initLoaded || !this.targetId || !this.targetType) return;

      this.isLoading = true;
      try {
        // 并行请求：查询点赞总数 + 当前用户点赞状态
        const [countRes, likedRes] = await Promise.all([
          this.getLikeCountApi(),
          this.getIsLikedApi()
        ]);

        // 更新本地状态
        this.likeCount = countRes || 0;
        this.isLiked = likedRes || false;
        this.initLoaded = true; // 标记初始化完成
      } catch (error) {
        console.error('初始化点赞数据失败:', error);
        ElMessage.error('加载点赞状态失败，请刷新重试');
      } finally {
        this.isLoading = false;
      }
    },

    /**
     * 调用接口查询点赞总数
     */
    async getLikeCountApi() {
      try {
        const res = await getLikeCount(this.targetId, this.targetType);
        // 假设接口返回 { code: 200, data: 10 } 格式
        return res.data.code === 200 ? res.data.data : 0;
      } catch (error) {
        throw new Error(`查询点赞数失败: ${error.message}`);
      }
    },

    /**
     * 调用接口查询当前用户是否点赞
     */
    async getIsLikedApi() {
      try {
        const loginStatus = checkLogin();
        if (!loginStatus) {
          return false;
        }
        const res = await isLiked(this.targetId, this.targetType);
        // 假设接口返回 { code: 200, data: true } 格式
        return res.data.code === 200 ? res.data.data : false;
      } catch (error) {
        throw new Error(`查询点赞状态失败: ${error.message}`);
      }
    },

    /**
     * 处理点赞/取消点赞点击事件
     */
    async handleLike() {
      // 防重复点击 + 初始化未完成禁止操作
      if (this.isLoading || !this.initLoaded) return;

      // 1. 登录校验
      const loginStatus = checkLogin();
      if (!loginStatus) {
        ElMessage.warning('请先登录后再进行点赞操作');
        // 可扩展：跳转到登录页
        // this.$router.push('/login');
        return;
      }

      this.isLoading = true;
      try {
        // 2. 构造操作参数（operateType: 1=点赞，0=取消点赞）
        const operateType = this.isLiked ? '0' : '1';
        const requestParams = {
          targetId: this.targetId,
          targetType: this.targetType,
          operateType: operateType
        };

        // 3. 调用点赞/取消点赞接口
        const res = await likeUnlike(requestParams);
        if (res.data.code === 200) {
          // 4. 本地更新状态（不触发重新查询）
          this.isLiked = !this.isLiked;
          this.likeCount = this.isLiked
              ? this.likeCount + 1
              : Math.max(0, this.likeCount - 1);

          // 5. 提示操作成功
          ElMessage.success(this.isLiked ? '点赞成功' : '取消点赞成功');
        } else {
          ElMessage.error(res.data.message || '操作失败，请重试');
        }
      } catch (error) {
        console.error('点赞/取消点赞失败:', error);
        ElMessage.error('网络异常，操作失败');
      } finally {
        this.isLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: none;
  border-radius: 16px;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.like-btn:hover {
  background: #eee;
}

.like-btn.liked {
  color: #ff4d4f;
  background: #fff0f0;
}

.like-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.loading-icon {
  font-size: 14px;
}

.like-icon {
  font-size: 16px;
}

.like-count {
  font-size: 14px;
  margin-left: 2px;
}
</style>